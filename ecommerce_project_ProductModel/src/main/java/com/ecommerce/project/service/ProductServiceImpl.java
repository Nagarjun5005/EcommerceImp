package com.ecommerce.project.service;


import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.payload.ProductResponse;
import com.ecommerce.project.repository.CategoryRepository;
import com.ecommerce.project.repository.ProductRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class ProductServiceImpl implements ProductService{

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ModelMapper modelMapper;


    @Override
    public ProductDTO addProduct(Product product, Long categoryId) {
       //find the category and map to product
        Category category = categoryRepository.findById(categoryId).orElseThrow(() ->
                new ResourceNotFoundException("category", "categoryId", categoryId));
        product.setImage("default.png");
        product.setCategory(category);
        //calculate specialPrice =Special Price = Price - (Price × (Discount / 100))
       double specialPrice= product.getPrice()-(product.getPrice()*(product.getDiscount()/100));
       product.setSpecialPrice(specialPrice);
       //save the object
        Product savedProduct = productRepository.save(product);
        return modelMapper.map(savedProduct,ProductDTO.class);
    }

    @Override
    public ProductResponse getAllProduct() {
        List<Product> allProducts  = productRepository.findAll();
        //need to convert all the product to product dto
        List<ProductDTO> productDTOList = allProducts.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .collect(Collectors.toList());

        ProductResponse productResponse=new ProductResponse();
        productResponse.setContent(productDTOList);
        return productResponse;
    }

    @Override
    public ProductResponse searchByCategory(Long categoryId) {

        //find the category
        Category categoryFound = categoryRepository.findById(categoryId).orElseThrow(() ->
                new ResourceNotFoundException("category", "categoryId", categoryId));

        //query used to find the products by categoryId
        List<Product> productList = productRepository.findByCategoryOrderByPriceAsc(categoryFound);

        //convert product to product dto-->and collect it as a list
        List<ProductDTO> productDTOList = productList.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .collect(Collectors.toList());

        //return the ProductResponse
        ProductResponse productResponse=new ProductResponse();
        productResponse.setContent(productDTOList);
        return productResponse;




    }

    @Override
    public ProductResponse searchProductByKeyWord(String keyword) {

        //getting the data from database---using query
     // List<Product>productList= productRepository.findByProductNameLikeIgnoreCase('%'+keyword+'%');
      List<Product> productList = productRepository.findByProductNameLikeIgnoreCase("%" + keyword + "%");
        //convert product to product dto-->and collect it as a list
        List<ProductDTO> productDTOList = productList.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .collect(Collectors.toList());

        //return the ProductResponse
        ProductResponse productResponse=new ProductResponse();
        productResponse.setContent(productDTOList);
        return productResponse;

    }
}
