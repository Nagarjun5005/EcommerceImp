package com.ecommerce.project.service;

import com.ecommerce.project.model.Category;
import com.ecommerce.project.payload.CategoryDTO;
import com.ecommerce.project.payload.CategoryResponse;

public interface CategoryService {

    public CategoryResponse getAllcategories(Integer pageNumber,Integer pageSize,String sortBy,String sortOrder);

    public CategoryDTO createCategory(CategoryDTO categoryRequest);

    CategoryDTO deleteCategory(Long categoryId);

    CategoryDTO updateCategory(CategoryDTO categoryDTO, Long categoryId);

//    CategoryDTO updateCategory(Category category, Long categoryId);
}
