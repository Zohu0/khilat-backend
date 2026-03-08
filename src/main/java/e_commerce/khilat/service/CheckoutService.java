package e_commerce.khilat.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;

import e_commerce.khilat.dtomodels.CheckoutRequest;
import e_commerce.khilat.dtomodels.CheckoutResponse;
import e_commerce.khilat.dtomodels.OrderDto;
import e_commerce.khilat.entity.Cart;
import e_commerce.khilat.entity.CartItem;
import e_commerce.khilat.entity.Order;
import e_commerce.khilat.entity.Payment;
import e_commerce.khilat.repository.CartItemRepo;
import e_commerce.khilat.repository.CartRepo;
import e_commerce.khilat.repository.OrderRepo;
import e_commerce.khilat.repository.PaymentRepo;
import e_commerce.khilat.util.Utility;

@Service
public class CheckoutService {
	
	

    private final CartRepo cartRepo;
    
    private final CartItemRepo cartItemRepo;
    
    private final PaymentRepo paymentRepo;
    
    private final StripePaymentService stripePaymentService;
    
    private final OrderRepo orderRepo;

    public CheckoutService(
            CartRepo cartRepo,
            CartItemRepo cartItemRepo,
            PaymentRepo paymentRepo,
            OrderRepo orderRepo,
        StripePaymentService stripePaymentService) {

        this.cartRepo = cartRepo;
        this.cartItemRepo = cartItemRepo;
        this.paymentRepo = paymentRepo;
        this.stripePaymentService = stripePaymentService;
        this.orderRepo =  orderRepo;
        
    }
    
    
    
    @CacheEvict(value = {"orders", "payments"}, allEntries = true)
    public CheckoutResponse createPaymentIntent(CheckoutRequest request) throws StripeException {
    	
    	

        // 1️⃣ Fetch cart
        Cart cart = cartRepo.findByGuestId(request.getGuestId())
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        // 2️⃣ Fetch cart items
        List<CartItem> cartItems = cartItemRepo.findByCart(cart);

        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        // 3️⃣ Calculate total amount (₹ → paise)
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem item : cartItems) {
            totalAmount = totalAmount.add(
                    item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
            );
        }

        long amountInPaise = totalAmount.multiply(BigDecimal.valueOf(100)).longValue();
        

        Long amount = Long.valueOf(amountInPaise);
        String currency = request.getCurrency();
        String guestId = (request.getGuestId() != null) ? request.getGuestId().toString() : "anonymous";
        

        // 4️⃣ Create Stripe PaymentIntent
        PaymentIntent paymentIntent = stripePaymentService.createPaymentIntent(
        	    amount, 
        	    currency, 
        	    guestId
        	);

        // 5️⃣ Save Payment record (CREATED)
        Payment payment = new Payment();
        payment.setGateway("STRIPE");
        payment.setTransactionId(paymentIntent.getId());
        payment.setStatus("CREATED");
        payment.setAmount(totalAmount);
        payment.setCreatedAt(LocalDateTime.now());

        Payment savedPayment = paymentRepo.save(payment);    
        
        Order order = new Order();
        order.setGuestId(request.getGuestId());
        order.setAddress(request.getAddress()); 
        order.setEmail(request.getEmail());
        order.setName(request.getName());
        order.setPhone(request.getPhone());
        order.setTrackingKey(Utility.generateTrackingKey());
        
        order.setPayment(savedPayment);
        
        orderRepo.save(order);
        
        
        
        
        

        // 6️⃣ Response
        CheckoutResponse response = new CheckoutResponse();
        response.setClientSecret(paymentIntent.getClientSecret());
        response.setAmount(amountInPaise);

        return response;
    }
}
