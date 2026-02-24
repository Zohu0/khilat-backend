package e_commerce.khilat.service;

import e_commerce.khilat.entity.Category;


import java.util.List;

public interface CategoryService {

    Category addCategory(Category category);

    List<Category> addMultipleCategories(List<Category> categories);

    List<Category> getAllCategories();
    
    Category updateCategory(Long id, Category category);

    void deleteCategory(Long id);
    
    
    
    
    
    
}
