package e_commerce.khilat.controller;

import com.stripe.Stripe;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.net.Webhook;
import e_commerce.khilat.service.OrderService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stripe/webhook")
public class StripeWebhookController {

    private final OrderService orderService;
    
    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    @Value("${stripe.secret.key}") // Matches your properties file key
    private String stripeApiKey;

    public StripeWebhookController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostConstruct
    public void setupStripe() {
        Stripe.apiKey = stripeApiKey;
        // Note: version pinning is internal in SDK 24.10.0
    }

    @PostMapping
    public ResponseEntity<String> handleWebhook(@RequestBody String payload,
                                                 @RequestHeader("Stripe-Signature") String sigHeader) {
    	System.out.println("Inside webhook method");
        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
            System.out.println("Received Event: " + event.getType() + " ID: " + event.getId());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid signature");
        }

        if ("payment_intent.succeeded".equals(event.getType())) {
        	
        	System.out.println("Inside webhook execution");
            
            EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
            PaymentIntent intent = null;

            // 1. Try safe deserialization (works if versions match)
            if (dataObjectDeserializer.getObject().isPresent()) {
                intent = (PaymentIntent) dataObjectDeserializer.getObject().get();
            } 
            // 2. PERMANENT FALLBACK: If versions mismatch (like your 2026 CLI vs 2024 SDK)
            else {
                try {
                    // deserializeUnsafe() forces mapping even if the API versions differ
                    StripeObject stripeObject = dataObjectDeserializer.deserializeUnsafe();
                    if (stripeObject instanceof PaymentIntent) {
                        intent = (PaymentIntent) stripeObject;
                    }
                } catch (Exception e) {
                    System.err.println("Fallback deserialization failed: " + e.getMessage());
                }
            }

            if (intent != null) {
                try {
                    orderService.createOrderAfterPayment(intent);
                    return ResponseEntity.ok("Order Created");
                } catch (Exception e) {
                    e.printStackTrace();
                    // Return 500 so Stripe retries the webhook
                    return ResponseEntity.internalServerError().body("DB Update Failed");
                }
            } else {
                return ResponseEntity.status(422).body("Could not parse PaymentIntent");
            }
        }

        return ResponseEntity.ok("Received");
    }
}