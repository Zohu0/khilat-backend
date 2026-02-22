package e_commerce.khilat.dtomodels;

import java.util.UUID;

public class AddToCartRequest {
	
	private UUID guestId;
    private Long productId;
    private Integer quantity;
    
    
    
    
	public UUID getGuestId() {
		return guestId;
	}
	public void setGuestId(UUID guestId) {
		this.guestId = guestId;
	}
	public Long getProductId() {
		return productId;
	}
	public void setProductId(Long productId) {
		this.productId = productId;
	}
	public Integer getQuantity() {
		return quantity;
	}
	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}
    
    
    

}
