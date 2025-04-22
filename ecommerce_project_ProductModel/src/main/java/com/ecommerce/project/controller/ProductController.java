package com.ecommerce.project.controller;



import com.ecommerce.project.config.AppConstants;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.payload.ProductResponse;
import com.ecommerce.project.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api")
public class ProductController {


    @Autowired
    ProductService productService;


    //add product
    @PostMapping("/admin/categories/{categoryId}/product")
    public ResponseEntity<ProductDTO> addProduct(@Valid  @RequestBody ProductDTO productDTO, @PathVariable Long categoryId){
       ProductDTO productAdded= productService.addProduct(productDTO,categoryId);
       return new ResponseEntity<>(productAdded, HttpStatus.CREATED);
    }

    //get all products
    //need to debug this
    @GetMapping("/public/products")
    public ResponseEntity<ProductResponse> getAllProducts(
            @RequestParam(name="pageNumber",defaultValue = AppConstants.PAGE_NUMBER,required = false) Integer pageNumber,
            @RequestParam(name="pageSize",defaultValue = AppConstants.PAGE_SIZE,required = false) Integer pageSize,
            @RequestParam(name="sortBy", defaultValue = AppConstants.SORT_DIR,required = false) String sortBy,
            @RequestParam(name="sortOrder", defaultValue = AppConstants.SORT_PRODUCTS_BY,required = false) String sortOrder
            ){
      ProductResponse productResponse=  productService.getAllProduct(pageNumber,pageSize,sortBy,sortOrder);
        return new ResponseEntity<>(productResponse,HttpStatus.OK);
    }

    //get all products by category
    @GetMapping("/public/categories/{categoryId}/products")
    public ResponseEntity<ProductResponse> searchByCategory(@PathVariable Long categoryId,
        @RequestParam(name="pageNumber",defaultValue = AppConstants.PAGE_NUMBER,required = false) Integer pageNumber,
        @RequestParam(name="pageSize",defaultValue = AppConstants.PAGE_SIZE,required = false) Integer pageSize,
        @RequestParam(name="sortBy", defaultValue = AppConstants.SORT_DIR,required = false) String sortBy,
        @RequestParam(name="sortOrder", defaultValue = AppConstants.SORT_PRODUCTS_BY,required = false) String sortOrder){
        ProductResponse allProductByCategory = productService.searchByCategory(categoryId,pageNumber,pageSize,sortBy,sortOrder);
        return new ResponseEntity<>(allProductByCategory,HttpStatus.OK);
    }

    //search products using keyword
    @GetMapping("/public/products/keyword/{keyword}")
    public ResponseEntity<ProductResponse>searchProductByKeyword(@PathVariable String keyword,
    @RequestParam(name="pageNumber",defaultValue = AppConstants.PAGE_NUMBER,required = false) Integer pageNumber,
    @RequestParam(name="pageSize",defaultValue = AppConstants.PAGE_SIZE,required = false) Integer pageSize,
    @RequestParam(name="sortBy", defaultValue = AppConstants.SORT_DIR,required = false) String sortBy,
    @RequestParam(name="sortOrder", defaultValue = AppConstants.SORT_PRODUCTS_BY,required = false) String sortOrder){
       ProductResponse productResponse= productService.searchProductByKeyWord(keyword,pageNumber,pageSize,sortBy,sortOrder);
       return new ResponseEntity<>(productResponse,HttpStatus.FOUND);
    }

    //update the product
    @PutMapping("/admin/products/{productId}")
    public ResponseEntity<ProductDTO>updateProduct
    ( @Valid @RequestBody Product product,@PathVariable Long productId){
        ProductDTO updatedProduct=productService.updateProduct(product,productId);
        return new ResponseEntity<>(updatedProduct,HttpStatus.OK);
    }

    //delete the product

    @DeleteMapping("/admin/products/{productId}")
    public ResponseEntity<ProductDTO> deleteProduct(@PathVariable Long productId){
      ProductDTO deleteProductDTO=  productService.deleteProduct(productId);
      return new ResponseEntity<>(deleteProductDTO,HttpStatus.OK);
    }

    //update the image
    @PutMapping("/admin/{productId}/image")
    public ResponseEntity<ProductDTO>updateImage(@PathVariable Long productId,
                                                 @RequestParam("image")
                                                 MultipartFile image) throws IOException {
       ProductDTO updatedProduct= productService.updateImage(productId,image);
       return new ResponseEntity<>(updatedProduct,HttpStatus.OK);
    }

}
