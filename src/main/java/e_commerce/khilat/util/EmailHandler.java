//package e_commerce.khilat.util;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.*;
//import org.springframework.web.client.RestTemplate;
//
//import org.springframework.mail.SimpleMailMessage;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.stereotype.Component;
//
//@Component
//public class EmailHandler {
//
//	@Value("${spring.mail.username}")
//	private String fromEmail;
//
//	@Autowired
//	private JavaMailSender mailSender;
//
//	
//	
//	
//	
////	@Async
////	public void sendEmailtoGuest(String guestEmail, String guestName, String trckngKey) {
////		
////		System.out.println("📨 sendEmailtoGuest method called");
////	    System.out.println("Email: " + guestEmail);
////	    System.out.println("Name: " + guestName);
////	    System.out.println("Tracking Key: " + trckngKey);
////		try {
////			SimpleMailMessage message = new SimpleMailMessage();
////
////			message.setFrom(fromEmail);
////			message.setTo(guestEmail);
////
////			message.setSubject("Order Confirmation - Khilat Store 🎉");
////			String emailContent = "Hi " + (guestName != null ? guestName : "Customer") + "Your Order Id is : " + trckngKey
////					+ " ,\n\n" + CommonConstant.ORDER_PLACED;
////			message.setText(emailContent);
////
////	        System.out.println("📤 Attempting to send email...");
////
////			mailSender.send(message);
////			
////	        System.out.println("✅ Email sent successfully!");
////		} catch (Exception e) {
////			System.err.println("❌ Error sending email:");
////			e.printStackTrace();		}
////	}
//	
//	
//	@Async
//	public void sendEmailtoGuest(String guestEmail, String guestName, String trckngKey) {
//
//	    System.out.println("📨 Mailbluster method called");
//	    System.out.println("Email: " + guestEmail);
//	    System.out.println("Name: " + guestName);
//	    System.out.println("Tracking Key: " + trckngKey);
//
//	    try {
//	        RestTemplate restTemplate = new RestTemplate();
//
//	        String url = "https://api.mailbluster.com/api/leads";
//
//	        // 🔑 Prepare headers
//	        HttpHeaders headers = new HttpHeaders();
//	        headers.setContentType(MediaType.APPLICATION_JSON);
//
//	        // 📦 Prepare body (as per Mailbluster docs)
//	        Map<String, Object> body = new HashMap<>();
//	        body.put("authorization", System.getenv("MAILBLUSTER_API_KEY")); // from Railway env
//	        body.put("firstName", guestName != null ? guestName : "Customer");
//	        body.put("lastName", "");
//	        body.put("email", guestEmail);
//	        body.put("subscribed", true); // IMPORTANT → must be true to receive emails
//
//	        System.out.println("📤 Sending request to Mailbluster...");
//	        System.out.println("Request Body: " + body);
//
//	        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
//
//	        // 🚀 API call
//	        ResponseEntity<String> response =
//	                restTemplate.postForEntity(url, request, String.class);
//
//	        System.out.println("✅ Mailbluster Response Status: " + response.getStatusCode());
//	        System.out.println("📩 Mailbluster Response Body: " + response.getBody());
//
//	    } catch (Exception e) {
//	        System.err.println("❌ Mailbluster Error:");
//	        e.printStackTrace();
//	    }
//	}
//
//	@Async
//	public void sendDispatchEmail(String guestEmail, String guestName, String trckngKey) {
//	    try {
//	        SimpleMailMessage message = new SimpleMailMessage();
//	        message.setFrom(fromEmail);
//	        message.setTo(guestEmail);
//	        message.setSubject("Great News! Your Order #" + trckngKey + " is Dispatched 🚚");
//
//	        // 1. Check for null and use 'guestName' (the parameter)
//	        String displayName = (guestName != null) ? guestName : "Customer";
//
//	        
//	        
//	        String content = String.format(CommonConstant.ORDER_DISPATCH_EMAIL_TEMPLATE, displayName, trckngKey);
//
//	        // 3. Set the text to the message
//	        message.setText(content);
//
//	        mailSender.send(message);
//	        System.out.println("Dispatch email sent to: " + guestEmail);
//	    } catch (Exception e) {
//	        System.err.println("Dispatch email fail hui: " + e.getMessage());
//	    }
//	}
//	
//	
//	@Async
//	public void sendCancelEmail(String email, String name, String trckngKey) {
//	    try {
//	        SimpleMailMessage message = new SimpleMailMessage();
//	        message.setFrom(fromEmail);
//	        message.setTo(email);
//	        message.setSubject("Request For Cancellation #" + trckngKey + " is cancelled 🚚");
//
//	        // 1. Check for null and use 'guestName' (the parameter)
//	        String displayName = (name != null) ? name : "Customer";
//
//	        
//	        
//	        String content = String.format(CommonConstant.ORDER_CANCELLED_EMAIL_TEMPLATE, displayName, trckngKey);
//
//	        // 3. Set the text to the message
//	        message.setText(content);
//
//	        mailSender.send(message);
//	    } catch (Exception e) {
//	        System.err.println("Cancel email fail hui: " + e.getMessage());
//	    }
//	}
//	
//	
//
//	@Async
//	public void sendDeliveredEmail(String guestEmail, String guestName, String trckngKey) {
//	    try {
//	        SimpleMailMessage message = new SimpleMailMessage();
//	        message.setFrom(fromEmail);
//	        message.setTo(guestEmail);
//	        message.setSubject("Great News! Your Order #" + trckngKey + " is Delivered 🚚");
//
//	        // 1. Check for null and use 'guestName' (the parameter)
//	        String displayName = (guestName != null) ? guestName : "Customer";
//
//	        
//	        
//	        String content = String.format(CommonConstant.ORDER_DELIVERED_EMAIL_TEMPLATE, displayName, trckngKey);
//
//	        // 3. Set the text to the message
//	        message.setText(content);
//
//	        mailSender.send(message);
//	        System.out.println("Dispatch email sent to: " + guestEmail);
//	    } catch (Exception e) {
//	        System.err.println("Dispatch email fail hui: " + e.getMessage());
//	    }
//	}
//
//}



package e_commerce.khilat.util;

import com.resend.*;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class EmailHandler {

    @Value("${resend.api.key}")
    private String resendApiKey;

    private static final String FROM_EMAIL = "Khilat Store <onboarding@resend.dev>";
    // 👆 Use this until you verify your own domain on Resend
    // After domain verification, replace with: "Khilat Store <no-reply@yourdomain.com>"


    
    @PostConstruct
    public void debugEnv() {
        System.out.println("ENV RESEND_API_KEY: " + System.getenv("RESEND_API_KEY"));
        System.out.println("PROPERTY resend.api.key: " + resendApiKey);
    }
    // ─── Order Placed ─────────────────────────────────────────────
    @Async
    public void sendEmailtoGuest(String guestEmail, String guestName, String trckngKey) {
        System.out.println("📨 sendEmailtoGuest called for: " + guestEmail);
        System.out.println("👤 Guest Name: " + guestName);
        System.out.println("🔑 Tracking Key: " + trckngKey);
        System.out.println("🔐 Resend API Key present: " + (resendApiKey != null && !resendApiKey.isEmpty()));

        String displayName = (guestName != null) ? guestName : "Customer";

        String html = "<h2>Hi " + displayName + "! 🎉</h2>"
                + "<p>Your order has been placed successfully.</p>"
                + "<p><strong>Order ID:</strong> " + trckngKey + "</p>"
                + "<p>" + CommonConstant.ORDER_PLACED + "</p>"
                + "<br><p>Thanks for shopping with <strong>Khilat Store</strong>!</p>";

        System.out.println("📧 Preparing to send email to: " + guestEmail);
        System.out.println("📝 Subject: Order Confirmation - Khilat Store 🎉");

        sendEmail(guestEmail, "Order Confirmation - Khilat Store 🎉", html);

        System.out.println("✅ sendEmailtoGuest() completed — check sendEmail() logs below");
    }


    // ─── Order Dispatched ──────────────────────────────────────────
    @Async
    public void sendDispatchEmail(String guestEmail, String guestName, String trckngKey) {
        System.out.println("📨 sendDispatchEmail called for: " + guestEmail);

        String displayName = (guestName != null) ? guestName : "Customer";
        String text = String.format(CommonConstant.ORDER_DISPATCH_EMAIL_TEMPLATE, displayName, trckngKey);

        String html = "<h2>Great News, " + displayName + "! 🚚</h2>"
                + "<p>Your order <strong>#" + trckngKey + "</strong> has been dispatched.</p>"
                + "<p>" + text.replace("\n", "<br>") + "</p>";

        sendEmail(guestEmail, "Your Order #" + trckngKey + " is Dispatched 🚚", html);
    }


    // ─── Order Cancelled ──────────────────────────────────────────
    @Async
    public void sendCancelEmail(String email, String name, String trckngKey) {
        System.out.println("📨 sendCancelEmail called for: " + email);

        String displayName = (name != null) ? name : "Customer";
        String text = String.format(CommonConstant.ORDER_CANCELLED_EMAIL_TEMPLATE, displayName, trckngKey);

        String html = "<h2>Hi " + displayName + ",</h2>"
                + "<p>Your cancellation request for order <strong>#" + trckngKey + "</strong> has been processed.</p>"
                + "<p>" + text.replace("\n", "<br>") + "</p>";

        sendEmail(email, "Order #" + trckngKey + " Cancellation Update", html);
    }


    // ─── Order Delivered ──────────────────────────────────────────
    @Async
    public void sendDeliveredEmail(String guestEmail, String guestName, String trckngKey) {
        System.out.println("📨 sendDeliveredEmail called for: " + guestEmail);

        String displayName = (guestName != null) ? guestName : "Customer";
        String text = String.format(CommonConstant.ORDER_DELIVERED_EMAIL_TEMPLATE, displayName, trckngKey);

        String html = "<h2>Your order is Delivered, " + displayName + "! 🎁</h2>"
                + "<p>Order <strong>#" + trckngKey + "</strong> has been delivered successfully.</p>"
                + "<p>" + text.replace("\n", "<br>") + "</p>"
                + "<br><p>Hope you love your purchase! 😊</p>";

        sendEmail(guestEmail, "Your Order #" + trckngKey + " is Delivered 🎁", html);
    }


    // ─── Core Send Method (shared by all) ─────────────────────────
    private void sendEmail(String to, String subject, String html) {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("🚀 sendEmail() triggered");
        System.out.println("📬 To: " + to);
        System.out.println("📌 Subject: " + subject);
        System.out.println("🔑 API Key (first 8 chars): " + 
            (resendApiKey != null && resendApiKey.length() > 8 
                ? resendApiKey.substring(0, 8) + "..." 
                : "❌ NULL OR TOO SHORT"));
        try {
            Resend resend = new Resend(resendApiKey);
            System.out.println("✅ Resend client created");

            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from(FROM_EMAIL)
                    .to(to)
                    .subject(subject)
                    .html(html)
                    .build();
            System.out.println("✅ Email options built");

            CreateEmailResponse response = resend.emails().send(params);
            System.out.println("✅ Email sent! Resend ID: " + response.getId());

        } catch (Exception e) {
            System.err.println("❌ sendEmail() FAILED");
            System.err.println("❌ Exception type: " + e.getClass().getName());
            System.err.println("❌ Message: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }
}
