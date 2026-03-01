package e_commerce.khilat.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import e_commerce.khilat.controller.ProductController;
import e_commerce.khilat.dtomodels.ProductRequest;
import e_commerce.khilat.entity.Category;
import e_commerce.khilat.entity.Product;
import e_commerce.khilat.entity.ProductImage;
import e_commerce.khilat.entity.ProductVariant;
import e_commerce.khilat.repository.CategoryRepo;
import e_commerce.khilat.repository.ProductRepo;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;


@Service
public class ProductService {
	
	private static final Logger LOGGER =
            LoggerFactory.getLogger(ProductService.class);
	
	@Autowired
	private ProductRepo productRepo;
	
	 @Autowired
	  private CategoryRepo categoryRepo;
	 
	 @Autowired
	  private ProductImageService productImageService;
	 
	 
	
	
	@Transactional(readOnly = true)
    public List<Product> getLatestProducts(int limit) {

        if (limit <= 0 || limit > 50) {
            limit = 8; // safe default
        }

        Pageable pageable = PageRequest.of(
                0,
                limit,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return productRepo.findLatestProducts(pageable);
    }
	
	   
    public ProductRequest getProductById(Long id) {
    	
    	Product product = productRepo.findById(id).orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    	
    	
    	ProductRequest response = new ProductRequest();
    	
    	response.setCategory(product.getCategory());
    	response.setCategoryId(product.getCategory().getId());
    	response.setDescription(product.getDescription());
    	response.setName(product.getName());
    	response.setTrending(product.getTrending());
    	response.setProductImages(product.getProductImages());
    	response.setVariants(product.getVariants());
    	
    	return response;
    }
    
    public List<Product> getTrendingProducts(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        return productRepo.findTrendingProducts(pageable);
    }
    
    
    
    public Product createProduct(ProductRequest request) {

        if (request.getCategoryId() == null) {
            throw new IllegalArgumentException("Category ID is required");
        }

        Category category = categoryRepo.findById(request.getCategoryId())
            .orElseThrow(() -> new RuntimeException("Category not found"));

        Product product = new Product();
        product.setCategory(category);
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        
        if (request.getVariants() != null) {
            for (ProductVariant variant : request.getVariants()) {
                // This line ensures the 'product_id' column in 'product_variant' table is filled
                variant.setProduct(product); 
            }
            product.setVariants(request.getVariants());
        }
        
        product.setIsActive(
            request.getIsActive() != null ? request.getIsActive() : true
        );
        product.setTrending(
            request.getTrending() != null ? request.getTrending() : "n"
        );
        product.setCreatedAt(LocalDateTime.now());

        return productRepo.save(product);
    }

     @Transactional
     public Product createProductWithImages(ProductRequest request, List<MultipartFile> images) {
         // 1. Save the Product first (to generate the ID)
         Product savedProduct = createProduct(request);
         
         
         if(images == null) {
        	 LOGGER.debug("image is nulllll");
         }
         
         

         // 2. Iterate and Save Images if they exist
         if (images != null && !images.isEmpty()) {
        	    LOGGER.debug("Uploading {} images for product {}", images.size(), savedProduct.getId());
             for (MultipartFile file : images) {
                 ProductImage savedImage = productImageService.uploadProductImage(savedProduct.getId(), file);
                 
                 savedProduct.getProductImages().add(savedImage);
             }
         }
         

         return savedProduct;
     }
     
     @Transactional
     public Product updateProductWithImages(
             Long productId,
             ProductRequest request,
             List<MultipartFile> images,
             List<Long> deleteImageIds
     ) {

         Product product = productRepo.findById(productId)
                 .orElseThrow(() -> new RuntimeException("Product not found"));

         // 🔹 Update basic fields
         product.setName(request.getName());
         product.setDescription(request.getDescription());
         product.setVariants(request.getVariants());
         product.setIsActive(
                 request.getIsActive() != null ? request.getIsActive() : product.getIsActive()
         );
         product.setTrending(
                 request.getTrending() != null ? request.getTrending() : product.getTrending()
         );

         // 🔹 Update category (optional)
         if (request.getCategoryId() != null) {
             Category category = categoryRepo.findById(request.getCategoryId())
                     .orElseThrow(() -> new RuntimeException("Category not found"));
             product.setCategory(category);
         }

         // 🔹 Delete selected images
         if (deleteImageIds != null && !deleteImageIds.isEmpty()) {
             for (Long imageId : deleteImageIds) {
                 productImageService.deleteProductImage(imageId);
             }
         }

         // 🔹 Upload new images
         if (images != null && !images.isEmpty()) {
             for (MultipartFile file : images) {
                 ProductImage image =
                         productImageService.uploadProductImage(product.getId(), file);
                 product.getProductImages().add(image);
             }
         }

         return productRepo.save(product);
     }
     
     @Transactional
     public void deleteProduct(Long productId) {

         Product product = productRepo.findById(productId)
                 .orElseThrow(() -> new RuntimeException("Product not found"));

         productRepo.delete(product);  
     }
     
     public Page<Product> filterProducts(
             String keyword,
             String category,
             BigDecimal minPrice,
             BigDecimal maxPrice,
             Pageable pageable) {

         // Case 1: sab null → simple pagination
         if (keyword == null && category == null
                 && minPrice == null && maxPrice == null) {
             return productRepo.findAll(pageable);
         }

         // Case 2: filters applied
         return productRepo.filterProducts(
                 keyword,
                 category,
                 minPrice,
                 maxPrice,
                 pageable
         );
     }

}

