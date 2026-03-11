package e_commerce.khilat.service;


import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

@Service
public class RazorpayPaymentService {

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    public com.razorpay.Order createRazorpayOrder(Long amount, String currency, String guestId) throws RazorpayException {
        RazorpayClient client = new RazorpayClient(keyId, keySecret);

        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amount); // Amount in paise
        orderRequest.put("currency", currency);
        orderRequest.put("receipt", "receipt_" + System.currentTimeMillis());
        
        // Metadata in Razorpay is passed via 'notes'
        JSONObject notes = new JSONObject();
        notes.put("guestId", guestId);
        orderRequest.put("notes", notes);

        return client.orders.create(orderRequest);
    }
}