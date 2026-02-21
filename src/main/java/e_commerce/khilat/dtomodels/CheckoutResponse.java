package e_commerce.khilat.dtomodels;

public class CheckoutResponse {
	
	
    private String clientSecret;
    private Long amount;
	public String getClientSecret() {
		return clientSecret;
	}
	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}
	public Long getAmount() {
		return amount;
	}
	public void setAmount(Long amount) {
		this.amount = amount;
	}
    
    
    
}
