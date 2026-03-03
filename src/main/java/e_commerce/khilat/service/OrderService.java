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
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

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
import jakarta.transaction.Transactional;

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

//		Order order = orderRepository.findByguestId(guestId);
		
		Order order = orderRepository.findByPaymentId(payment.getId())
				.orElseThrow(() -> new RuntimeException("Payment record not found"));
		
		
		order.setCreatedAt(LocalDateTime.now());
		order.setDtOfOps(DateUtil.dateConverterToLong(order.getCreatedAt()));

		order.setStatus("PENDING");

		order.setTotalAmount(payment.getAmount());

		order = orderRepository.save(order);
		
		


		// 5. Create Order Items & Update Stock
//		for (CartItem cartItem : cartItems) {
//			Product product = cartItem.getProduct();
//			product.setStock(product.getStock() - cartItem.getQuantity());
//			productRepository.save(product);
//
//			OrderItem orderItem = new OrderItem();
//			orderItem.setOrder(order);
//			orderItem.setProduct(product);
//			orderItem.setQuantity(cartItem.getQuantity());
//			orderItem.setPrice(cartItem.getPrice().multiply(new BigDecimal(cartItem.getQuantity())));
//			orderItemRepository.save(orderItem);
//		}
		
		
		// 5. Create Order Items & Update Stock (Refactored for Variants)
		for (CartItem cartItem : cartItems) {
		    // 1. Get the specific variant from the cart item
		    ProductVariant variant = cartItem.getVariant(); 

		    // 2. Decrement stock from the Variant, not the Product
		    int updatedStock = variant.getStock() - cartItem.getQuantity();
		    if (updatedStock < 0) {
		        throw new RuntimeException("Insufficient stock for: " + variant.getProduct().getName() + 
		                                   " (" + variant.getSize() + "/"  + ")");
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
		
		sendEmailtoGuest(guestEmail, guestName);
		

		// 7. Cleanup
		cartItemRepository.deleteAll(cartItems);
		cartRepository.delete(cart);
	}
	
	
	public void sendEmailtoGuest(String guestEmail, String guestName) {
	    try {
	        SimpleMailMessage message = new SimpleMailMessage();
	        
//	        System.out.println("senders email:  " + fromEmail);
	        
	        // 1. Kis email account se mail jayega (aapki application.properties wali email)
	        message.setFrom(fromEmail); 
	        
//	        System.out.println("rcvrs email:  " + guestEmail);
	        // 2. Customer ka email (order entity se fetch kiya hua)
	        message.setTo(guestEmail); 
	        
	        message.setSubject("Order Confirmation - Khilat Store 🎉");	        
	        String emailContent = "Hi " + (guestName != null ? guestName : "Customer") + ",\n\n" +
	        		CommonConstant.EmailMessage;
	                              
	        message.setText(emailContent);

	        mailSender.send(message);
	        
//	        System.out.println("message : " + message);
//	        System.out.println("Confirmation email sent to: " + guestEmail);
	    } catch (Exception e) {
	        // Sirf log karein taaki email fail hone par transaction roll back na ho
	        System.err.println("Error sending email: " + e.getMessage());
	    }
	}
	
	
	
	public OrderDto getOrderDetail(Long orderId) { 

	    Order order = orderRepository.findById(orderId)
	            .orElseThrow(() -> new RuntimeException("Order not found"));

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
	    
//	    List<OrderItemDto> itemDtos = orderItems.stream().map(item -> {
//	        OrderItemDto dto = new OrderItemDto();
//	        
//	        // 1. Get the Variant from the OrderItem
//	        ProductVariant variant = item.getVariant();
//	        // 2. Get the Product from that Variant
//	        Product product = variant.getProduct(); 
//	        
//	        dto.setId(item.getId());
//	        dto.setOrderid(orderId);
//	        
//	        // 3. Map Variant-Specific details
//	        dto.setProductId(product.getId()); // Still useful for linking back to product page
//	        dto.setProductName(product.getName());
//	        dto.setSize(variant.getSize());   // NEW: Show the size bought
//	        
//	        dto.setQuantity(item.getQuantity());
//	        dto.setPrice(item.getPrice()); 
//	        
//	        // 4. Stock Left is now checked on the Variant level
//	        dto.setStockLeft(variant.getStock()); 
//	        
//	        // 5. Map Category (Accessed via Product)
//	        if (product.getCategory() != null) {
//	            dto.setCategoryName(product.getCategory().getName());
//	        }
//
//	        // 6. Map Image
//	        // Logic: Try to get images from the product
//	        if (product.getProductImages() != null && !product.getProductImages().isEmpty()) {
//	            dto.setImageUrl(product.getProductImages().get(0).getImageUrl());
//	        }
//	        
//	        return dto;
//	    }).collect(Collectors.toList());
//	    
//	    response.setItems(itemDtos);
//	    
//	    return response; // Return the populated DTO
	    
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
	
	
	public Page<OrderSummaryDto> getOrderSummariesForAdmin(Pageable pageable, Long date) {
		Page<Order> ordersPage;
		

	    if (date != null) {
	       
	        
	        ordersPage = orderRepository.findByStatusAndDtOfOps("PENDING",date, pageable);
	    } else {
	        ordersPage = orderRepository.findByStatus("PENDING", pageable);
	    }

	    return ordersPage.map(order -> {
	        OrderSummaryDto dto = new OrderSummaryDto();
	        dto.setOrderId(order.getId());
	        dto.setName(order.getName());
	        dto.setPhone(order.getPhone());
	        dto.setAmount(order.getTotalAmount());
	        dto.setOrderStatus(order.getStatus());
	        dto.setCreatedAt(order.getCreatedAt());
	        
	        
	        dto.setEmail(order.getEmail());
	        

	        // Payment check logic (Same as before)
	        Payment payment = paymentRepository
	                .findByOrderId(order.getId())
	                .orElse(null);

	        if (payment != null) {
	            dto.setPaymentStatus(payment.getStatus());
	        } else {
	            dto.setPaymentStatus("PENDING");
	        }

	        return dto;
	    });
	}
	
	public Page<OrderSummaryDto> getDispatchedOrderSummaries(Pageable pageable) {
	    // Sirf DISPATCHED status wale orders fetch karega
	    Page<Order> ordersPage = orderRepository.findByStatus("DISPATCHED", pageable);

	    return ordersPage.map(order -> {
	        OrderSummaryDto dto = new OrderSummaryDto();
	        dto.setOrderId(order.getId());
	        dto.setName(order.getName());
	        dto.setPhone(order.getPhone());
	        dto.setAmount(order.getTotalAmount());
	        dto.setOrderStatus(order.getStatus());
	        dto.setCreatedAt(order.getCreatedAt());

	        // Payment status fetch logic
	        paymentRepository.findByOrderId(order.getId())
	            .ifPresent(p -> dto.setPaymentStatus(p.getStatus()));

	        return dto;
	    });
	}
	
	@Transactional
	public void markOrderAsDispatched(Long orderId) {
	    // 1. Order ko DB se find karein
	    Order order = orderRepository.findById(orderId)
	            .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));
	    
	    
	    OrderSummaryDto orderDto = new OrderSummaryDto();
	    orderDto.setOrderId(order.getId());
	    orderDto.setOrderStatus("DISPATCHED"); // DTO mein status update kiya
	    orderDto.setEmail(order.getEmail());
	    orderDto.setName(order.getName());
	    
	    order.setStatus(orderDto.getOrderStatus());
	    orderRepository.save(order);

//	    // 2. Status update karein
//	    order.setStatus("DISPATCHED");
//	    orderRepository.save(order);
//
//	    // 3. User ko Dispatch ka email bhejein
//	    sendDispatchEmail(order.getEmail(), order.getName(), order.getId());    
	    sendDispatchEmail(orderDto.getEmail(), orderDto.getName(), orderDto.getOrderId());
	}

	private void sendDispatchEmail(String guestEmail, String guestName, Long orderId) {
	    try {
	        SimpleMailMessage message = new SimpleMailMessage();
	        message.setFrom(fromEmail);
	        message.setTo(guestEmail);
	        message.setSubject("Great News! Your Order #" + orderId + " is Dispatched 🚚");

	        // 1. Check for null and use 'guestName' (the parameter)
	        String displayName = (guestName != null) ? guestName : "Customer";

	        
	        
	        String content = String.format(CommonConstant.ORDER_DISPATCH_EMAIL_TEMPLATE, displayName, orderId);

	        // 3. Set the text to the message
	        message.setText(content);

	        mailSender.send(message);
	        System.out.println("Dispatch email sent to: " + guestEmail);
	    } catch (Exception e) {
	        System.err.println("Dispatch email fail hui: " + e.getMessage());
	    }
	}

	

}
