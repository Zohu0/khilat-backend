package e_commerce.khilat.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import e_commerce.khilat.dtomodels.OrderDto;
import e_commerce.khilat.service.OrderService;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin
public class AdminOrderController {
	
	@Autowired
	private OrderService orderService;
	
	@GetMapping("/order/{id}") 
	public ResponseEntity<?> getOrderDetailController(@PathVariable Long id) {
	    try {
	  
	        OrderDto orderDetail = orderService.getOrderDetail(id);
	        
	      
	        return ResponseEntity.ok(orderDetail);
	        
	    } catch (RuntimeException e) {
	        return ResponseEntity
	                .status(HttpStatus.NOT_FOUND)
	                .body("Error: " + e.getMessage());
	    } catch (Exception e) {
	        return ResponseEntity
	                .status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body("An unexpected error occurred: " + e.getMessage());
	    }
	}
	
	

}
