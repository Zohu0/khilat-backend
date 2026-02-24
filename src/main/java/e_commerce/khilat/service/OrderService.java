package e_commerce.khilat.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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
    private  ProductRepo productRepository;

	@Transactional
	public void createOrderAfterPayment(PaymentIntent intent) {
	    // 1. Get Payment record from DB to update its status later
	    Payment payment = paymentRepository.findByTransactionId(intent.getId())
	            .orElseThrow(() -> new RuntimeException("Payment record not found"));

	    if ("SUCCESS".equals(payment.getStatus())) return;

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
}
