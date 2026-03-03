package e_commerce.khilat.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;

import e_commerce.khilat.dtomodels.PaymentRequest;
import e_commerce.khilat.service.StripePaymentService;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin
public class PaymentController {
	
//	 private final StripePaymentService paymentService;
//
//	    public PaymentController(StripePaymentService paymentService) {
//	        this.paymentService = paymentService;
//	    }
//	    
//	    
//	    
//	    
//	    
//	
//	    @PostMapping("/create-intent")
//	    public ResponseEntity<?> createPayment(@RequestBody PaymentRequest request) {
//	        try {
//	            // Pass the guestId from the request to the service
//	            PaymentIntent intent = paymentService.createPaymentIntent(
//	                    request.getAmount(),
//	                    request.getCurrency(),
//	                    request.getGuestId() // Ensure your PaymentRequest DTO has this field
//	            );
//
//	            Map<String, String> response = new HashMap<>();
//	            response.put("clientSecret", intent.getClientSecret());
//
//	            return ResponseEntity.ok(response);
//	        } catch (StripeException e) {
//	            return ResponseEntity.badRequest().body(e.getMessage());
//	        }
//	    }

}
