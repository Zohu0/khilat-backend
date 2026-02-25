package e_commerce.khilat.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.stripe.model.PaymentIntent;

import e_commerce.khilat.entity.Cart;
import e_commerce.khilat.entity.CartItem;
import e_commerce.khilat.entity.Order;
import e_commerce.khilat.entity.OrderItem;
import e_commerce.khilat.entity.Payment;
import e_commerce.khilat.entity.Product;
import e_commerce.khilat.repository.CartItemRepo;
import e_commerce.khilat.repository.CartRepo;
import e_commerce.khilat.repository.OrderItemRepo;
import e_commerce.khilat.repository.OrderRepo;
import e_commerce.khilat.repository.PaymentRepo;
import e_commerce.khilat.repository.ProductRepo;
import jakarta.transaction.Transactional;

import e_commerce.khilat.dtomodels.OrderDto;
import e_commerce.khilat.dtomodels.OrderItemDto;
import e_commerce.khilat.dtomodels.PaymentDto;

@Service
public class OrderService {
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

		Order order = orderRepository.findByguestId(guestId);

		order.setCreatedAt(LocalDateTime.now());

		order.setStatus("PENDING");

		order.setTotalAmount(payment.getAmount());

		order = orderRepository.save(order);

		// 5. Create Order Items & Update Stock
		for (CartItem cartItem : cartItems) {
			Product product = cartItem.getProduct();
			product.setStock(product.getStock() - cartItem.getQuantity());
			productRepository.save(product);

			OrderItem orderItem = new OrderItem();
			orderItem.setOrder(order);
			orderItem.setProduct(product);
			orderItem.setQuantity(cartItem.getQuantity());
			orderItem.setPrice(cartItem.getPrice().multiply(new BigDecimal(cartItem.getQuantity())));
			orderItemRepository.save(orderItem);
		}

		// 6. Finalize Payment in DB
		payment.setOrder(order); // Now you link them!
		payment.setStatus("SUCCESS");
		paymentRepository.save(payment);

		// 7. Cleanup
		cartItemRepository.deleteAll(cartItems);
		cartRepository.delete(cart);
	}
	
	
	
	
	public OrderDto getOrderDetail(Long orderId) { // Changed void to OrderDto

	    Order order = orderRepository.findById(orderId)
	            .orElseThrow(() -> new RuntimeException("Order not found"));

	    OrderDto response = new OrderDto();
	    response.setAddress(order.getAddress());
	    response.setCreatedAt(order.getCreatedAt());
	    response.setEmail(order.getEmail());
	    response.setId(order.getId());
	    response.setName(order.getName());
	    response.setPhone(order.getPhone());
	    response.setOrderStatus(order.getStatus());

	    // Fetch Payment
	    Payment payment = paymentRepository.findByOrderId(orderId)
	            .orElseThrow(() -> new RuntimeException("Payment record not found"));
	    
	    PaymentDto pmtDto = new PaymentDto();
	    pmtDto.setId(payment.getId());
	    pmtDto.setAmount(payment.getAmount());
	    pmtDto.setCreatedAt(payment.getCreatedAt());
	    pmtDto.setPaymentstatus(payment.getStatus());
	    response.setPayment(pmtDto);
	    
	    // Fetch and Map Order Items
	    List<OrderItem> orderItems = orderItemRepo.findByOrderId(orderId);
	    
	    List<OrderItemDto> itemDtos = orderItems.stream().map(item -> {
	        OrderItemDto dto = new OrderItemDto();
	        
	        // Map ID fields
	        dto.setId(item.getId()); // The ID of the OrderItem itself
	        dto.setOrderid(orderId);
	        
	        // Map Product details
	        dto.setProductId(item.getProduct().getId());
	        dto.setProductName(item.getProduct().getName());
	        dto.setQuantity(item.getQuantity());
	        dto.setPrice(item.getPrice()); 
	        dto.setStockLeft(item.getProduct().getStock());
	        
	        // Map Category
	        if (item.getProduct().getCategory() != null) {
	            dto.setCategoryName(item.getProduct().getCategory().getName());
	        }

	        // Map Image
	        if (item.getProduct().getProductImages() != null && !item.getProduct().getProductImages().isEmpty()) {
	            dto.setImageUrl(item.getProduct().getProductImages().get(0).getImageUrl());
	        }
	        
	        return dto;
	    }).collect(Collectors.toList());
	    
	    response.setItems(itemDtos);
	    
	    return response; // Return the populated DTO
	}
	
	

}
