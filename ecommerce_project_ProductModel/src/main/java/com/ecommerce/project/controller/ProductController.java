package com.ecommerce.project.controller;


import com.ecommerce.project.model.Category;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.payload.ProductResponse;
import com.ecommerce.project.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ProductController {


    @Autowired
    ProductService productService;


    //add product
    @PostMapping("/admin/categories/{categoryId}/product")
    public ResponseEntity<ProductDTO> addProduct(@RequestBody Product product,@PathVariable Long categoryId){
       ProductDTO productDTO= productService.addProduct(product,categoryId);
       return new ResponseEntity<>(productDTO, HttpStatus.CREATED);
    }

    //get all products
    @GetMapping("/public/products")
    public ResponseEntity<ProductResponse> getAllProducts(){
      ProductResponse productResponse=  productService.getAllProduct();
        return new ResponseEntity<>(productResponse,HttpStatus.OK);
    }

    //get all products by category
    @GetMapping("/public/categories/{categoryId}/products")
    public ResponseEntity<ProductResponse> searchByCategory(@PathVariable Long categoryId){
        ProductResponse allProductByCategory = productService.searchByCategory(categoryId);
        return new ResponseEntity<>(allProductByCategory,HttpStatus.OK);
    }

    //search products using keyword
    @GetMapping("/public/products/keyword/{keyword}")
    public ResponseEntity<ProductResponse>searchProductByKeyword(@PathVariable String keyword){
       ProductResponse productResponse= productService.searchProductByKeyWord(keyword);
       return new ResponseEntity<>(productResponse,HttpStatus.FOUND);
    }

}
