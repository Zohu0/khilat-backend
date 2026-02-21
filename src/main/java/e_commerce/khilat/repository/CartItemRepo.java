package e_commerce.khilat.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import e_commerce.khilat.entity.Cart;
import e_commerce.khilat.entity.CartItem;

@Repository
public interface CartItemRepo extends JpaRepository<CartItem, Long> {
	
	 List<CartItem> findByCart(Cart cart);

}
