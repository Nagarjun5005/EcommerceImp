package com.ecommerce.project.service;

import com.ecommerce.project.exception.APIException;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.payload.CategoryDTO;
import com.ecommerce.project.payload.CategoryResponse;
import com.ecommerce.project.repository.CategoryRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
public class CategoryServiceImpl implements CategoryService {


    @Autowired
    private CategoryRepository categoryRepository;


    @Autowired
    private ModelMapper modelMapper;

    List<Category> categories = new ArrayList<>();
    public long nextId = 1L;

    @Override
    public CategoryResponse getAllcategories(Integer pageNumber,Integer pageSize,String sortBy,String sortOrder) {

        //Sort
        Sort sortByAndOrder=sortOrder.equalsIgnoreCase("asc")
                ?Sort.by(sortBy).ascending()
                :Sort.by(sortBy).descending();

        //pageAble
        Pageable pageable=PageRequest.of(pageNumber,pageSize,sortByAndOrder);

        //page
        Page<Category>categoryPage=categoryRepository.findAll(pageable);

        List<Category>categories=categoryPage.getContent();
        if(categories.isEmpty()){
            throw new APIException("The categories are empty for now!");
        }

        //convert Category entity to CategoryRequestDto using model Mapper
        List<CategoryDTO> categoryList = categories.stream().
                map(category -> modelMapper.map(category, CategoryDTO.class)).toList();
        CategoryResponse categoryResponse =new CategoryResponse();
        categoryResponse.setContent(categoryList);
        categoryResponse.setPageNumber(categoryPage.getNumber());
        categoryResponse.setPageSize(categoryPage.getSize());
        categoryResponse.setTotalPages( categoryPage.getTotalPages());
        categoryResponse.setTotalItems(categoryPage.getTotalElements());
        categoryResponse.setLastPage(categoryPage.isLast());

        return categoryResponse;
    }

    @Override
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {

        //convert CategoryDTO to entity
        Category category = modelMapper.map(categoryDTO, Category.class);

        Category foundCategory= categoryRepository.findByCategoryName(category.getCategoryName());
       if(foundCategory!=null){
           throw new APIException("Category with same name already exists");
       }
        Category savedCategory = categoryRepository.save(category);

        //need to return categoryDto --->hence convert Category to categoryDto
        CategoryDTO savedCategoryDto = modelMapper.map(savedCategory, CategoryDTO.class);
        return savedCategoryDto;
    }

    @Override
    public CategoryDTO deleteCategory(Long categoryId) {

        //database operations--->use Category Entity
        Category categoryFound = categoryRepository.findById(categoryId).
                orElseThrow(() -> new ResourceNotFoundException(
                      "category","categoryId",categoryId
                ));
        categoryRepository.delete(categoryFound);

        //convert Category to CategoryDto--->Since the return type is CategoryDto
        CategoryDTO categoryDTO=modelMapper.map(categoryFound,CategoryDTO.class);

        return categoryDTO;
    }


    @Override
    public CategoryDTO updateCategory(CategoryDTO categoryDTO, Long categoryId) {

        Category categoryFromDB = categoryRepository.findById(categoryId).orElseThrow(() -> new ResourceNotFoundException("Category", "CategoryId", categoryId));

        //input is CategoryDto--->convert to entity
        Category category = modelMapper.map(categoryDTO, Category.class);

        //find the category

        categoryFromDB.setCategoryId(categoryId);
        categoryRepository.save(categoryFromDB);

        CategoryDTO dto=new CategoryDTO();
//        CategoryDTO categoryDTO1 = modelMapper.map(categoryFromDB, CategoryDTO.class);
        dto.setCategoryId(categoryId);
        dto.setCategoryName(categoryFromDB.getCategoryName());
        return dto;
    }
}
