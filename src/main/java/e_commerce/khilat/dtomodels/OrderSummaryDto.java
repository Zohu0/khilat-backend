package e_commerce.khilat.dtomodels;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderSummaryDto {

    private Long orderId;
    private String name;
    private Long phone;
    private BigDecimal amount;
    private String paymentStatus;
    private String orderStatus;
    private LocalDateTime createdAt;
    private String email;
    private String trckngKey;
    
    
    
    

    public String getTrckngKey() {
		return trckngKey;
	}

	public void setTrckngKey(String trckngKey) {
		this.trckngKey = trckngKey;
	}

	public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public String getEmail() {
    	return email;
    }

    public void setEmail(String email) {
    	this.email=email;
    }
    

    public Long getPhone() {
		return phone;
	}

	public void setPhone(Long phone) {
		this.phone = phone;
	}

	public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}