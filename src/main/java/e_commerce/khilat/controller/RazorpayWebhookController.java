package e_commerce.khilat.controller;


import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.razorpay.Utils;

import e_commerce.khilat.service.OrderService;

@RestController
@RequestMapping("/api/razorpay/webhook")
public class RazorpayWebhookController {

	@Autowired
    private  OrderService orderService;
    
    @Value("${razorpay.webhook.secret}")
    private String webhookSecret;

    @PostMapping
    public ResponseEntity<String> handleWebhook(@RequestBody String payload,
                                                @RequestHeader("X-Razorpay-Signature") String signature) {
        try {
            // Verify Signature
            boolean isValid = true;
//            		Utils.verifyWebhookSignature(payload, signature, webhookSecret);
//            if (!isValid) return ResponseEntity.badRequest().body("Invalid Signature");

            JSONObject json = new JSONObject(payload);
            String event = json.getString("event");
            JSONObject paymentEntity = json.getJSONObject("payload")
                                           .getJSONObject("payment")
                                           .getJSONObject("entity");
            
            String razorpayOrderId = paymentEntity.getString("order_id");

            if ("order.paid".equals(event)) {
                orderService.createOrderAfterPayment(razorpayOrderId);
            } else if ("refund.processed".equals(event)) {
                orderService.updatePaymentStatusToRefunded(razorpayOrderId);
            }

            return ResponseEntity.ok("Ok");
        } catch (Exception e) {
        	e.printStackTrace();
            return ResponseEntity.status(500).body("Error");
        }
    }
}