package e_commerce.khilat.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import e_commerce.khilat.entity.Order;

@Repository
public interface OrderRepo extends JpaRepository<Order, Long> {
	
	Order findByguestId(UUID guestId);
	
	Page<Order> findByStatus(String status, Pageable pageable);
	
	
	
	
	

}
