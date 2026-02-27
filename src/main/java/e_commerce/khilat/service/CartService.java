package e_commerce.khilat.service;

//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import e_commerce.khilat.dtomodels.AddToCartRequest;
import e_commerce.khilat.entity.Cart;
import e_commerce.khilat.entity.CartItem;
import e_commerce.khilat.entity.Product;
import e_commerce.khilat.repository.CartItemRepo;
import e_commerce.khilat.repository.CartRepo;
import e_commerce.khilat.repository.ProductRepo;
import jakarta.transaction.Transactional;

@Service
public class CartService {

    @Autowired private CartRepo cartRepository;
    @Autowired private CartItemRepo cartItemRepository;
    @Autowired private ProductRepo productRepository;

    @Transactional
    public void addToCart(AddToCartRequest request) {
        // 1. Get or Create Cart
        Cart cart = cartRepository.findByGuestId(request.getGuestId())
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setGuestId(request.getGuestId());
                    return cartRepository.save(newCart);
                });

        // 2. Fetch Product
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // 3. Check if Item already in cart
        Optional<CartItem> existingItem = cartItemRepository.findByCartAndProduct(cart, product);

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
            cartItemRepository.save(item);
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(request.getQuantity());
            newItem.setPrice(product.getPrice()); // Capture current price
            cartItemRepository.save(newItem);
        }
    }
    
    @Transactional
    public void increaseQuantity(Long cartItemId) {

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        item.setQuantity(item.getQuantity() + 1);

        cartItemRepository.save(item);
    }
    
    @Transactional
    public void decreaseQuantity(Long cartItemId) {

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        if (item.getQuantity() <= 1) {
            cartItemRepository.delete(item); // item removed from cart
        } else {
            item.setQuantity(item.getQuantity() - 1);
            cartItemRepository.save(item);
        }
    }

    public List<CartItem> getCartItems(UUID guestId) {
        return cartRepository.findByGuestId(guestId)
                .map(cart -> cartItemRepository.findByCartWithProductDetails(cart))
                .orElse(new ArrayList<>()); // Return empty list if no cart exists yet
    }
}
