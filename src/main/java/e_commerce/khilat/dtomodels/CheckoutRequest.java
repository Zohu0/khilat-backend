package e_commerce.khilat.dtomodels;

import java.util.UUID;

public class CheckoutRequest {
	
	 private UUID guestId;
	 private String currency; // "inr"
	 
	 
	 public UUID getGuestId() {
		 return guestId;
	 }
	 public void setGuestId(UUID guestId) {
		 this.guestId = guestId;
	 }
	 public String getCurrency() {
		 return currency;
	 }
	 public void setCurrency(String currency) {
		 this.currency = currency;
	 }



}
