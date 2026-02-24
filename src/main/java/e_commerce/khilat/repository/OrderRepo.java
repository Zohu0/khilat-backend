package e_commerce.khilat.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import e_commerce.khilat.entity.Order;

@Repository
public interface OrderRepo extends JpaRepository<Order, Long> {
	
	Order findByguestId(UUID guestId);
	
	
	

}
