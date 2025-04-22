package com.ecommerce.project.service;

import com.ecommerce.project.model.Product;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.payload.ProductResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ProductService {

    ProductDTO addProduct(ProductDTO productDTO, Long categoryId);

    ProductResponse getAllProduct();

    ProductResponse searchByCategory(Long categoryId);

    ProductResponse searchProductByKeyWord(String keyword);

    ProductDTO updateProduct(Product product, Long productId);

    ProductDTO deleteProduct(Long productId);

    ProductDTO updateImage(Long categoryId, MultipartFile image) throws IOException;
}
