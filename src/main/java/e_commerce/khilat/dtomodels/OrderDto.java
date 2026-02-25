package e_commerce.khilat.dtomodels;

import java.time.LocalDateTime;
import java.util.List;

import e_commerce.khilat.entity.Payment;

public class OrderDto {
	
	private Long id;

	private PaymentDto payment;
	
	private String email;
    
    private String name;
    
    private String address;
    
    private String OrderStatus;
    
	private Long phone;
    
	private LocalDateTime createdAt;
	
	private List<OrderItemDto> items;
	
	private List<OrderDto> orderItem; 
    
    public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public Long getPhone() {
		return phone;
	}

	public void setPhone(Long phone) {
		this.phone = phone;
	}


	public String getOrderStatus() {
		return OrderStatus;
	}

	public void setOrderStatus(String OrderStatus) {
		this.OrderStatus = OrderStatus;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public void setPayment(PaymentDto payment) {
		this.payment = payment;
	}
	
	public PaymentDto getPayment() {
		return payment;
	}
	
    public List<OrderItemDto> getItems() {
        return items;
    }

    public void setItems(List<OrderItemDto> items) {
        this.items = items;
    }

}
