package e_commerce.khilat.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

	
	
	
	
	@Async
	public void sendEmailtoGuest(String guestEmail, String guestName, Long orderId) {
		try {
			SimpleMailMessage message = new SimpleMailMessage();

			message.setFrom(fromEmail);
			message.setTo(guestEmail);

			message.setSubject("Order Confirmation - Khilat Store 🎉");
			String emailContent = "Hi " + (guestName != null ? guestName : "Customer") + "Your Order Id is : " + orderId
					+ " ,\n\n" + CommonConstant.EmailMessage;
			message.setText(emailContent);

			mailSender.send(message);
		} catch (Exception e) {
			System.err.println("Error sending email: " + e.getMessage());
		}
	}

	@Async
	public void sendDispatchEmail(String guestEmail, String guestName, Long orderId) {
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
	
	
	@Async
	public void sendCancelEmail(String email, String name, Long orderId) {
	    try {
	        SimpleMailMessage message = new SimpleMailMessage();
	        message.setFrom(fromEmail);
	        message.setTo(email);
	        message.setSubject("Request For Cancellation #" + orderId + " is cancelled 🚚");

	        // 1. Check for null and use 'guestName' (the parameter)
	        String displayName = (name != null) ? name : "Customer";

	        
	        
	        String content = String.format(CommonConstant.ORDER_CANCELLED_EMAIL_TEMPLATE, displayName, orderId);

	        // 3. Set the text to the message
	        message.setText(content);

	        mailSender.send(message);
	    } catch (Exception e) {
	        System.err.println("Cancel email fail hui: " + e.getMessage());
	    }
	}

}
