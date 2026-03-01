package e_commerce.khilat.service;

import java.math.BigDecimal;

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
import e_commerce.khilat.entity.ProductVariant;
import e_commerce.khilat.repository.CartItemRepo;
import e_commerce.khilat.repository.CartRepo;
import e_commerce.khilat.repository.ProductRepo;
import e_commerce.khilat.repository.ProductVariantRepo;
import jakarta.transaction.Transactional;

@Service
public class CartService {

    @Autowired private CartRepo cartRepository;
    @Autowired private CartItemRepo cartItemRepository;
    @Autowired private ProductRepo productRepository;
    @Autowired
	private ProductVariantRepo productVariantRepo;

    @Transactional
    public void addToCart(AddToCartRequest request) {
        // 1. Get or Create Cart (Logic remains same)
        Cart cart = cartRepository.findByGuestId(request.getGuestId())
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setGuestId(request.getGuestId());
                    return cartRepository.save(newCart);
                });

        // 2. Fetch Product Variant instead of Product
        // The frontend should now pass the specific variant ID selected by the user
        ProductVariant variant = productVariantRepo.findById(request.getVariantId())
                .orElseThrow(() -> new RuntimeException("Product Variant not found"));

        // 3. Check if this specific Variant is already in the cart
        // Update your Repository to use findByCartAndVariant
        Optional<CartItem> existingItem = cartItemRepository.findByCartAndVariant(cart, variant);

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + request.getQuantity();
            
            // Safety Check: Don't allow adding more than available stock
            if (newQuantity > variant.getStock()) {
                throw new RuntimeException("Insufficient stock for this variant.");
            }
            
            item.setQuantity(newQuantity);
            cartItemRepository.save(item);
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            
            // Link to the specific Variant
            newItem.setVariant(variant); 
            
            newItem.setQuantity(request.getQuantity());
            
            // Price should be pulled from the variant (in case prices vary by size)
            newItem.setPrice(variant.getPrice());
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
