package com.ecommerce.project.controller;

import com.ecommerce.project.config.AppConstants;
import com.ecommerce.project.payload.CategoryDTO;
import com.ecommerce.project.payload.CategoryResponse;
import com.ecommerce.project.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class categoryController {

    private final CategoryService categoryService;

    //RequestParam
    @GetMapping("/echo")
    public ResponseEntity<String> echoMessage(@RequestParam(name = "message",defaultValue = "default value") String message){
        return new ResponseEntity<>("Echoed message : "+message,HttpStatus.OK);
    }

    public categoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }


 @RequestMapping(value = "public/categories",method = RequestMethod.GET)
 public  ResponseEntity<CategoryResponse> getAllCategories(
         @RequestParam(name="pageNumber" ,defaultValue = AppConstants.PAGE_NUMBER,required = false)Integer pageNumber,
         @RequestParam(name = "pageSize",defaultValue = AppConstants.PAGE_SIZE,required = false) Integer pageSize,
         @RequestParam(name = "sortBy",defaultValue = AppConstants.SORT_CATEGORIES_BY,required = false)     String sortBy,
         @RequestParam(name = "sortOrder",defaultValue = AppConstants.SORT_DIR,required = false) String sortOrder)

 {
     CategoryResponse categoryResponse = categoryService.getAllcategories(pageNumber,pageSize,sortBy,sortOrder);
     return new ResponseEntity<>(categoryResponse,HttpStatus.OK);
 }

    @PostMapping("/public/categories")
    public ResponseEntity<String> addCategory(@Valid  @RequestBody CategoryDTO categoryDTO){
        categoryService.createCategory(categoryDTO);
        return new ResponseEntity<>("created the category",HttpStatus.CREATED);
    }


    @DeleteMapping("/admin/categories/{categoryId}")
    public ResponseEntity<CategoryDTO> deleteCategory(@PathVariable Long categoryId){
        CategoryDTO categoryDTO = categoryService.deleteCategory(categoryId);
        return new ResponseEntity<>(categoryDTO,HttpStatus.OK);
    }

    @PutMapping("/public/categories/{categoryId}")
    public ResponseEntity<CategoryDTO>updateCategory(@Valid @RequestBody CategoryDTO categoryDTO,@PathVariable Long categoryId){
        CategoryDTO updateCategory = categoryService.updateCategory(categoryDTO, categoryId);
        return new ResponseEntity<>(updateCategory,HttpStatus.CREATED);
    }



}
