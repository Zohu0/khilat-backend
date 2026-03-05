package e_commerce.khilat.admin;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import e_commerce.khilat.dtomodels.OrderDto;
import e_commerce.khilat.dtomodels.OrderSummaryDto;
import e_commerce.khilat.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.data.domain.PageRequest;

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
	
	
	
	@GetMapping("/order-pending")
	public ResponseEntity<Page<OrderSummaryDto>> getAllOrderSummaries(
	        @RequestParam(defaultValue = "0") int page,
	        @RequestParam(defaultValue = "10") int size,
	        @RequestParam(required = false) Long date) {

	    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
	    Page<OrderSummaryDto> result = orderService.getOrderSummariesForAdmin(pageable, date);

	    return ResponseEntity.ok(result);
	}
	
	
	@GetMapping("/dispatched-orders")
	public ResponseEntity<Page<OrderSummaryDto>> getDispatchedOrders(
	        @RequestParam(defaultValue = "0") int page,
	        @RequestParam(defaultValue = "10") int size) {

	    Pageable pageable = PageRequest.of(page, size);
	    Page<OrderSummaryDto> result = orderService.getDispatchedOrderSummaries(pageable);

	    return ResponseEntity.ok(result);
	}
	
	
	
	@PostMapping("/dispatch/{orderId}")
	public ResponseEntity<String> dispatchOrder(@PathVariable Long orderId) {
	    try {
	        orderService.markOrderAsDispatched(orderId);
	        return ResponseEntity.ok("Order marked as DISPATCHED and email sent.");
	    } catch (Exception e) {
	        return ResponseEntity.badRequest().body(e.getMessage());
	    }
	}
	
	

	
	

}
