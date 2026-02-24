package e_commerce.khilat.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import e_commerce.khilat.dtomodels.AddToCartRequest;
import e_commerce.khilat.entity.CartItem;
import e_commerce.khilat.entity.User;
import e_commerce.khilat.service.CartService;
import e_commerce.khilat.service.UserService;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin
public class CartController {
	
	

	@Autowired
	private  CartService cartservice;
	
	@Autowired
	private  UserService userService;

	
	// 1. Add item to cart
    @PostMapping("/addcart")
    public ResponseEntity<String> addToCart(@RequestBody AddToCartRequest request) {
        try {
        	UUID guestId = request.getGuestId();
//        	System.out.println(guestId);
        	cartservice.addToCart(request);
            return ResponseEntity.ok("Item added to cart successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
    
    @GetMapping("/{guestId}")
    public ResponseEntity<List<CartItem>> getCart(@PathVariable UUID guestId) {
        List<CartItem> items = cartservice.getCartItems(guestId);
        return ResponseEntity.ok(items);
    }
    
   
    
    

}
