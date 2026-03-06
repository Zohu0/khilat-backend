package e_commerce.khilat.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import e_commerce.khilat.dtomodels.CancelOrderDto;
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
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
	                             .body("Order cancel failed, sorry!");
	    }
	}

}
