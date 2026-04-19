package e_commerce.khilat.util;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class EmailHandler {

	@Value("${spring.mail.username}")
	private String fromEmail;

	@Autowired
	private JavaMailSender mailSender;

	
	
	
	
//	@Async
//	public void sendEmailtoGuest(String guestEmail, String guestName, String trckngKey) {
//		
//		System.out.println("📨 sendEmailtoGuest method called");
//	    System.out.println("Email: " + guestEmail);
//	    System.out.println("Name: " + guestName);
//	    System.out.println("Tracking Key: " + trckngKey);
//		try {
//			SimpleMailMessage message = new SimpleMailMessage();
//
//			message.setFrom(fromEmail);
//			message.setTo(guestEmail);
//
//			message.setSubject("Order Confirmation - Khilat Store 🎉");
//			String emailContent = "Hi " + (guestName != null ? guestName : "Customer") + "Your Order Id is : " + trckngKey
//					+ " ,\n\n" + CommonConstant.ORDER_PLACED;
//			message.setText(emailContent);
//
//	        System.out.println("📤 Attempting to send email...");
//
//			mailSender.send(message);
//			
//	        System.out.println("✅ Email sent successfully!");
//		} catch (Exception e) {
//			System.err.println("❌ Error sending email:");
//			e.printStackTrace();		}
//	}
	
	
	@Async
	public void sendEmailtoGuest(String guestEmail, String guestName, String trckngKey) {

	    System.out.println("📨 Mailbluster method called");
	    System.out.println("Email: " + guestEmail);
	    System.out.println("Name: " + guestName);
	    System.out.println("Tracking Key: " + trckngKey);

	    try {
	        RestTemplate restTemplate = new RestTemplate();

	        String url = "https://api.mailbluster.com/api/leads";

	        // 🔑 Prepare headers
	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.APPLICATION_JSON);

	        // 📦 Prepare body (as per Mailbluster docs)
	        Map<String, Object> body = new HashMap<>();
	        body.put("authorization", System.getenv("MAILBLUSTER_API_KEY")); // from Railway env
	        body.put("firstName", guestName != null ? guestName : "Customer");
	        body.put("lastName", "");
	        body.put("email", guestEmail);
	        body.put("subscribed", true); // IMPORTANT → must be true to receive emails

	        System.out.println("📤 Sending request to Mailbluster...");
	        System.out.println("Request Body: " + body);

	        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

	        // 🚀 API call
	        ResponseEntity<String> response =
	                restTemplate.postForEntity(url, request, String.class);

	        System.out.println("✅ Mailbluster Response Status: " + response.getStatusCode());
	        System.out.println("📩 Mailbluster Response Body: " + response.getBody());

	    } catch (Exception e) {
	        System.err.println("❌ Mailbluster Error:");
	        e.printStackTrace();
	    }
	}

	@Async
	public void sendDispatchEmail(String guestEmail, String guestName, String trckngKey) {
	    try {
	        SimpleMailMessage message = new SimpleMailMessage();
	        message.setFrom(fromEmail);
	        message.setTo(guestEmail);
	        message.setSubject("Great News! Your Order #" + trckngKey + " is Dispatched 🚚");

	        // 1. Check for null and use 'guestName' (the parameter)
	        String displayName = (guestName != null) ? guestName : "Customer";

	        
	        
	        String content = String.format(CommonConstant.ORDER_DISPATCH_EMAIL_TEMPLATE, displayName, trckngKey);

	        // 3. Set the text to the message
	        message.setText(content);

	        mailSender.send(message);
	        System.out.println("Dispatch email sent to: " + guestEmail);
	    } catch (Exception e) {
	        System.err.println("Dispatch email fail hui: " + e.getMessage());
	    }
	}
	
	
	@Async
	public void sendCancelEmail(String email, String name, String trckngKey) {
	    try {
	        SimpleMailMessage message = new SimpleMailMessage();
	        message.setFrom(fromEmail);
	        message.setTo(email);
	        message.setSubject("Request For Cancellation #" + trckngKey + " is cancelled 🚚");

	        // 1. Check for null and use 'guestName' (the parameter)
	        String displayName = (name != null) ? name : "Customer";

	        
	        
	        String content = String.format(CommonConstant.ORDER_CANCELLED_EMAIL_TEMPLATE, displayName, trckngKey);

	        // 3. Set the text to the message
	        message.setText(content);

	        mailSender.send(message);
	    } catch (Exception e) {
	        System.err.println("Cancel email fail hui: " + e.getMessage());
	    }
	}
	
	

	@Async
	public void sendDeliveredEmail(String guestEmail, String guestName, String trckngKey) {
	    try {
	        SimpleMailMessage message = new SimpleMailMessage();
	        message.setFrom(fromEmail);
	        message.setTo(guestEmail);
	        message.setSubject("Great News! Your Order #" + trckngKey + " is Delivered 🚚");

	        // 1. Check for null and use 'guestName' (the parameter)
	        String displayName = (guestName != null) ? guestName : "Customer";

	        
	        
	        String content = String.format(CommonConstant.ORDER_DELIVERED_EMAIL_TEMPLATE, displayName, trckngKey);

	        // 3. Set the text to the message
	        message.setText(content);

	        mailSender.send(message);
	        System.out.println("Dispatch email sent to: " + guestEmail);
	    } catch (Exception e) {
	        System.err.println("Dispatch email fail hui: " + e.getMessage());
	    }
	}

}
