package e_commerce.khilat.admin;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import e_commerce.khilat.dtomodels.ProductRequest;
import e_commerce.khilat.entity.Product;
import e_commerce.khilat.entity.User;
import e_commerce.khilat.service.ProductService;
import e_commerce.khilat.service.UserService;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin

public class AdminProductController {

	private static final Logger LOGGER = LoggerFactory.getLogger(AdminProductController.class);

	@Autowired
	private ProductService productService;

	@Autowired
	private UserService userService;

	@PostMapping("/login")
	public ResponseEntity<User> login(@RequestBody User user) {
		return ResponseEntity.ok(userService.login(user.getEmail(), user.getPassword()));
	}

	@PostMapping("/signup")
	public ResponseEntity<User> signup(@RequestBody User user) {
		return ResponseEntity.ok(userService.register(user));
	}

	@PostMapping(value = "/addproducts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<Product> createProductWithImages(@RequestPart("product") String productJson,
			@RequestPart(value = "images", required = false) List<MultipartFile> images) throws Exception {
		System.out.println("process started of saving product with image");
		LOGGER.debug("process started of saving product with image");

		LOGGER.debug("productJSon Vaue : {}", productJson.toString());

		ObjectMapper mapper = new ObjectMapper();
		ProductRequest request = mapper.readValue(productJson, ProductRequest.class);

		LOGGER.debug("request before sending to service Vaue : {}", request.toString());
		LOGGER.debug("image Vaue : {}", images);

		return ResponseEntity.ok(productService.createProductWithImages(request, images));
	}

	@PostMapping(value = "/updateproduct/{productId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<Product> updateProductWithImages(@PathVariable Long productId,
			@RequestPart("product") String productJson,
			@RequestPart(value = "images", required = false) List<MultipartFile> images,
			@RequestPart(value = "deleteImageIds", required = false) String deleteImageIds) throws Exception {

		ObjectMapper mapper = new ObjectMapper();
		ProductRequest request = mapper.readValue(productJson, ProductRequest.class);

		List<Long> deleteIds = null;
		if (deleteImageIds != null && !deleteImageIds.isEmpty()) {
			deleteIds = Arrays.stream(deleteImageIds.split(",")).map(String::trim).map(Long::parseLong).toList();
		}

		Product updated = productService.updateProductWithImages(productId, request, images, deleteIds);

		return ResponseEntity.ok(updated);
	}
	
	@DeleteMapping("/deleteproduct/{productId}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long productId) {

        productService.deleteProduct(productId);

        return ResponseEntity.ok("Product deleted successfully");
    }

}
