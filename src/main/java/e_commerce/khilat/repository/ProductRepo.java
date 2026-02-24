package e_commerce.khilat.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;


import e_commerce.khilat.entity.Product;

@Repository
public interface ProductRepo extends JpaRepository<Product, Long> {

	@Query("""
	        SELECT p FROM Product p
	        JOIN FETCH p.category 
	        LEFT JOIN FETCH p.productImages
	        ORDER BY p.createdAt DESC
	    """)
	    List<Product> findLatestProducts(Pageable pageable);
	
	@Query("SELECT p FROM Product p WHERE p.trending = 'y' ORDER BY p.createdAt DESC")
	List<Product> findTrendingProducts(Pageable pageable);
	
	
	
	
}
