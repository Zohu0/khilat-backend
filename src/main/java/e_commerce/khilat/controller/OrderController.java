package e_commerce.khilat.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import e_commerce.khilat.dtomodels.CancelOrderDto;
import e_commerce.khilat.dtomodels.OrderTrackingResponseDto;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import e_commerce.khilat.service.OrderService;


@RestController
@RequestMapping("/api/order")
@CrossOrigin
public class OrderController {
	
	
	@Autowired
	private OrderService orderService;
	
	
	@PostMapping(value = "/cancel-order")
	public ResponseEntity<String> cancelOrder(@RequestBody CancelOrderDto request) {
	    try {
	        String message = orderService.cancelOrderService(request);
	        
	        return ResponseEntity.ok(message);
	        
	    } catch (Exception e) {
	        // e.getMessage() will contain "Order is dispatched" or whatever you threw
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
	                             .body(e.getMessage());
	    }
	}
	
	@GetMapping("/track/{trackingKey}")
	public ResponseEntity<?> getOrderUpdate(@PathVariable String  trackingKey){
		
		try {
			OrderTrackingResponseDto response = orderService.getOrderUpdate(trackingKey); 
			return ResponseEntity.ok(response);
			
		}catch(RuntimeException e) {
			
			return ResponseEntity.status(400).body(e.getMessage());
		}
		
	}

}
