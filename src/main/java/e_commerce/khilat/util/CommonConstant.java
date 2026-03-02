package e_commerce.khilat.util;

public class CommonConstant {
	
	public static final String EmailMessage = "\"Aapka order successfully place ho gaya hai! ✅\\n\" +\n"
			+ "                    \"Humne aapka payment receive kar liya hai aur hum jald hi aapka order dispatch karenge.\\n\\n\" +\n"
			+ "                    \"Thank you for shopping with Khilat!\\n\" +\n"
			+ "                    \"Best Regards,\\nKhilat Team\"";
	
	public static final String SUCCESS = "SUCCESS";
	
	
	public static String getDispatchMessage(String guestName, String orderId) {
        String name = (guestName != null) ? guestName : "Customer";
        
        return "Hi " + name + ",\n\n" +
               "Aapka order #" + orderId + " dispatch ho gaya hai aur raste mein hai! 📦\n" +
               "Jald hi aapko ye mil jayega.\n\n" +
               "Thank you for shopping with Khilat!\n" +
               "Best Regards,\n" +
               "Khilat Team";
    }
	
	public static final String ORDER_DISPATCH_EMAIL_TEMPLATE = 
		    "Dear %s,\n\n" +
		    "Your order #%s has been dispatched and is currently in transit. 📦\n" +
		    "You will receive it shortly.\n\n" +
		    "Thank you for shopping with Khilat!\n\n" +
		    "Best Regards,\n" +
		    "Team Khilat";
}
