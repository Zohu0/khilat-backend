package e_commerce.khilat.controller;

import e_commerce.khilat.entity.Category;
import e_commerce.khilat.service.CategoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public List<Category> getAllCategories() {
        return categoryService.getAllCategories();
    }

    @PostMapping
    public Category addCategory(@RequestBody Category category) {
        return categoryService.addCategory(category);
    }

    @PostMapping("/bulk")
    public List<Category> addMultipleCategories(@RequestBody List<Category> categories) {
        return categoryService.addMultipleCategories(categories);
    }
}
