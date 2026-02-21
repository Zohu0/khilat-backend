package e_commerce.khilat.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import e_commerce.khilat.entity.Cart;

@Repository
public interface CartRepo extends JpaRepository<Cart, Long> {
	
	Optional<Cart> findByGuestId(UUID guestId);

}
