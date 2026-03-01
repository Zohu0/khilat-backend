package e_commerce.khilat.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import e_commerce.khilat.entity.Cart;
import e_commerce.khilat.entity.CartItem;
import e_commerce.khilat.entity.Product;
import e_commerce.khilat.entity.ProductVariant;

@Repository
public interface CartItemRepo extends JpaRepository<CartItem, Long> {
	
	 List<CartItem> findByCart(Cart cart);
	 
	 Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
	 
	 @Query("SELECT ci FROM CartItem ci " +
	           "JOIN FETCH ci.product p " +
	           "LEFT JOIN FETCH p.productImages " +
	           "WHERE ci.cart = :cart")
	    List<CartItem> findByCartWithProductDetails(@Param("cart") Cart cart);
	 
	 Optional<CartItem> findByCartAndVariant(Cart cart, ProductVariant variant);

}
