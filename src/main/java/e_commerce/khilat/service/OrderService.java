package e_commerce.khilat.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import com.stripe.model.Refund;
import com.stripe.param.RefundCreateParams;


import com.stripe.model.PaymentIntent;

import e_commerce.khilat.entity.Cart;
import e_commerce.khilat.entity.CartItem;
import e_commerce.khilat.entity.Order;
import e_commerce.khilat.entity.OrderItem;
import e_commerce.khilat.entity.Payment;
import e_commerce.khilat.entity.Product;
import e_commerce.khilat.entity.ProductVariant;
import e_commerce.khilat.repository.CartItemRepo;
import e_commerce.khilat.repository.CartRepo;
import e_commerce.khilat.repository.OrderItemRepo;
import e_commerce.khilat.repository.OrderRepo;
import e_commerce.khilat.repository.PaymentRepo;
import e_commerce.khilat.repository.ProductRepo;
import e_commerce.khilat.repository.ProductVariantRepo;
import e_commerce.khilat.util.CommonConstant;
import e_commerce.khilat.util.DateUtil;
import e_commerce.khilat.util.EmailHandler;
import jakarta.transaction.Transactional;
import e_commerce.khilat.dtomodels.CancelOrderDto;
import e_commerce.khilat.dtomodels.OrderDto;
import e_commerce.khilat.dtomodels.OrderItemDto;
import e_commerce.khilat.dtomodels.OrderSummaryDto;
import e_commerce.khilat.dtomodels.PaymentDto;
import e_commerce.khilat.dtomodels.ProductSummaryDto;

import org.springframework.data.domain.Pageable;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage; // Iski bhi zaroorat padegi

@Service
public class OrderService {

	@Autowired
	private JavaMailSender mailSender;

	// application.properties se username uthane ke liye
	@Value("${spring.mail.username}")
	private String fromEmail;

	@Autowired
	private PaymentRepo paymentRepository;
	
	@Autowired
	private TransactionTemplate transactionTemplate;
	@Autowired
	private CartRepo cartRepository;
	@Autowired
	private CartItemRepo cartItemRepository;
	@Autowired
	private OrderRepo orderRepository;
	@Autowired
	private OrderItemRepo orderItemRepository;
	@Autowired
	private ProductRepo productRepository;
	@Autowired
	private ProductVariantRepo productVariantRepo;

	@Autowired
	private OrderItemRepo orderItemRepo;

	@Autowired
	private EmailHandler emailHandler;

	@Transactional
	public void createOrderAfterPayment(PaymentIntent intent) {
		// 1. Get Payment record from DB to update its status later
		Payment payment = paymentRepository.findByTransactionId(intent.getId())
				.orElseThrow(() -> new RuntimeException("Payment record not found"));

		if ("SUCCESS".equals(payment.getStatus()))
			return;

		// 2. GET GUEST ID FROM STRIPE (Not from Payment table)
		String guestIdStr = intent.getMetadata().get("guestId");
		if (guestIdStr == null) {
			throw new RuntimeException("Guest ID missing from Stripe Metadata");
		}
		UUID guestId = UUID.fromString(guestIdStr);

		// 3. Find Cart
		Cart cart = cartRepository.findByGuestId(guestId)
				.orElseThrow(() -> new RuntimeException("Cart not found for guest"));

		List<CartItem> cartItems = cartItemRepository.findByCart(cart);

		Order order = orderRepository.findByPaymentId(payment.getId())
				.orElseThrow(() -> new RuntimeException("Payment record not found"));

		String trckngKey = order.getTrackingKey();

		order.setCreatedAt(LocalDateTime.now());
		order.setDtOfOps(DateUtil.dateConverterToLong(order.getCreatedAt()));

		order.setStatus("PENDING");

		order.setTotalAmount(payment.getAmount());

		order = orderRepository.save(order);

		// 5. Create Order Items & Update Stock (Refactored for Variants)
		for (CartItem cartItem : cartItems) {
			// 1. Get the specific variant from the cart item
			ProductVariant variant = cartItem.getVariant();

			// 2. Decrement stock from the Variant, not the Product
			int updatedStock = variant.getStock() - cartItem.getQuantity();
			if (updatedStock < 0) {
				throw new RuntimeException("Insufficient stock for: " + variant.getProduct().getName() + " ("
						+ variant.getSize() + "/" + ")");
			}
			variant.setStock(updatedStock);
			productVariantRepo.save(variant); //

			// 3. Create OrderItem pointing to the Variant
			OrderItem orderItem = new OrderItem();
			orderItem.setOrder(order);
			orderItem.setVariant(variant); // Changed from setProduct
			orderItem.setQuantity(cartItem.getQuantity());

			// Note: Use variant price or cartItem price.
			// Usually, price * quantity is handled here.
			orderItem.setPrice(cartItem.getPrice().multiply(new BigDecimal(cartItem.getQuantity())));

			orderItemRepository.save(orderItem);
		}

		// 6. Finalize Payment in DB
		payment.setOrder(order); // Now you link them!
		payment.setStatus(CommonConstant.SUCCESS);
		paymentRepository.save(payment);

		System.out.println("status of pmt : " + payment.getStatus());

		String guestEmail = order.getEmail();
		String guestName = order.getName();

		emailHandler.sendEmailtoGuest(guestEmail, guestName, trckngKey);

		// 7. Cleanup
		cartItemRepository.deleteAll(cartItems);
		cartRepository.delete(cart);
	}

	public OrderDto getOrderDetail(Long orderId) {

		Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));

		OrderDto response = new OrderDto();
		response.setAddress(order.getAddress());
		response.setCreatedAt(order.getCreatedAt());
		response.setEmail(order.getEmail());
		response.setId(order.getId());
		response.setName(order.getName());
		response.setPhone(order.getPhone());
		response.setStatus(order.getStatus());

		// Payment Repository call ki zaroorat nahi agar mapping sahi hai
		Payment payment = order.getPayment();

		if (payment != null) {
			PaymentDto pmtDto = new PaymentDto();
			pmtDto.setId(payment.getId());
			pmtDto.setAmount(payment.getAmount());
			pmtDto.setCreatedAt(payment.getCreatedAt());
			pmtDto.setPaymentstatus(payment.getStatus());
			response.setPayment(pmtDto);
		}

		// Fetch and Map Order Items
		List<OrderItem> orderItems = orderItemRepo.findByOrderId(orderId);

		List<OrderItemDto> itemDtos = orderItems.stream().map(item -> {
			OrderItemDto dto = new OrderItemDto();
			ProductVariant variant = item.getVariant();

			dto.setId(item.getId());
			dto.setOrderid(orderId);
			dto.setQuantity(item.getQuantity());
			dto.setPrice(item.getPrice());

			if (variant != null) {
				dto.setSize(variant.getSize());
				dto.setStockLeft(variant.getStock());

				Product product = variant.getProduct();
				if (product != null) {
					// Only access product methods if product is NOT null
					dto.setProductId(product.getId());
					dto.setProductName(product.getName());

					if (product.getCategory() != null) {
						dto.setCategoryName(product.getCategory().getName());
					}

					if (product.getProductImages() != null && !product.getProductImages().isEmpty()) {
						dto.setImageUrl(product.getProductImages().get(0).getImageUrl());
					}
				} else {
					// Optional: Handle the case where the product is missing
					dto.setProductName("Unknown Product (Deleted)");
				}
			}

			return dto;
		}).collect(Collectors.toList());

		response.setItems(itemDtos);

		return response; // Successfully returns the type OrderDto
	}

	@Cacheable(value = "orders", key = "#status + '-' + #date + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
	public Page<OrderSummaryDto> getOrderSummaries(String status, Long date, Pageable pageable) {

		Page<Order> ordersPage;

		if (date != null) {
			ordersPage = orderRepository.findByStatusAndDtOfOps(status, date, pageable);
		} else {
			ordersPage = orderRepository.findByStatus(status, pageable);
		}

		return ordersPage.map(order -> {
			OrderSummaryDto dto = new OrderSummaryDto();
			dto.setOrderId(order.getId());
			dto.setName(order.getName());
			dto.setPhone(order.getPhone());
			dto.setAmount(order.getTotalAmount());
			dto.setOrderStatus(order.getStatus());
			dto.setCreatedAt(order.getCreatedAt());

			paymentRepository.findByOrderId(order.getId()).ifPresent(p -> dto.setPaymentStatus(p.getStatus()));

			return dto;
		});
	}

	@Transactional
	@Caching(evict = { @CacheEvict(value = "orders", allEntries = true),
			@CacheEvict(value = "orderItems", allEntries = true) })
	public void markOrderAsDispatched(Long orderId) {
		// 1. Order ko DB se find karein
		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

		OrderSummaryDto orderDto = new OrderSummaryDto();
		orderDto.setOrderId(order.getId());
		orderDto.setOrderStatus("DISPATCHED"); // DTO mein status update kiya
		orderDto.setEmail(order.getEmail());
		orderDto.setName(order.getName());
		orderDto.setTrckngKey(order.getTrackingKey());
		
		
		order.setStatus(orderDto.getOrderStatus());
		orderRepository.save(order);

		emailHandler.sendDispatchEmail(orderDto.getEmail(), orderDto.getName(), orderDto.getTrckngKey());
	}


	
	
	
	

	

	public String cancelOrderService(CancelOrderDto request) {
	    // 1. First, Update and COMMIT the status to CANCELLED
	    // Using TransactionTemplate forces this to finish and save completely
	    Boolean updateSuccess = transactionTemplate.execute(status -> {
	        Order order = orderRepository.findByTrackingKey(request.getTrckngKey())
	                .orElseThrow(() -> new RuntimeException("Order Id not found"));

	        if (!order.getStatus().equalsIgnoreCase(CommonConstant.PENDING)) {
	            return false;
	        }

	        order.setStatus(CommonConstant.CANCELLED);
	        orderRepository.save(order);
	        return true; 
	    });

	    if (Boolean.FALSE.equals(updateSuccess)) {
	        return "Order cannot be cancelled.";
	    }

	    // 2. NOW that the DB is 100% committed as CANCELLED, call Stripe
	    try {
	        Order order = orderRepository.findByTrackingKey(request.getTrckngKey()).get();
	        
	        RefundCreateParams params = RefundCreateParams.builder()
	                .setPaymentIntent(order.getPayment().getTransactionId())
	                .build();

	        Refund refund = Refund.create(params);
	        emailHandler.sendCancelEmail(request.getEmail(), request.getName(), request.getTrckngKey());

	        return "Your Order Has Been Cancelled. Refund initiated.";

	    } catch (Exception e) {
	        // NOTE: If Stripe fails here, you might want to revert the status to PENDING 
	        // or log it for manual intervention, as the CANCELLED status is already committed.
	        throw new RuntimeException("Refund failed: " + e.getMessage());
	    }
	}
	
	
	
	@Transactional
	public void updatePaymentStatusToRefunded(String transactionId) {
	    Payment payment = paymentRepository.findByTransactionId(transactionId)
	            .orElseThrow(() -> new RuntimeException("Payment not found"));

	    payment.setStatus(CommonConstant.REFUNDED);

	    Order order = payment.getOrder();
	    if (order != null) {
	        // LOGGING FOR PRODUCTION DEBUGGING
	        
	        if (order.getStatus().trim().equalsIgnoreCase(CommonConstant.CANCELLED.trim())) {
	            order.setStatus(CommonConstant.REFUNDED);
	        }
	    }
	}
}
