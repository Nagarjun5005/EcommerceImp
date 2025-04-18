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
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
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

    @Override
    public ProductDTO updateProduct(Product product, Long productId) {
        //from db get  the product
        Product productFound = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("product", "productId", productId));
        // set all the product details
        productFound.setProductName(product.getProductName());
        productFound.setQuantity(product.getQuantity());
        productFound.setPrice(product.getPrice());
        productFound.setDescription(product.getDescription());
        double specialPrice= product.getPrice()-(product.getPrice()*(product.getDiscount()/100));
        productFound.setSpecialPrice(specialPrice);
        //save to the database
        productRepository.save(productFound);
        //return the productDto
        return modelMapper.map(productFound,ProductDTO.class);
    }

    @Override
    public ProductDTO deleteProduct(Long productId) {
        //find the product using the id ,if not found resourceNotFoundException
        Product productFound = productRepository.findById(productId).
                orElseThrow(() -> new ResourceNotFoundException("product", "productId", productId));
        //delete the id
        productRepository.delete(productFound);
        //return the dto
        return modelMapper.map(productFound,ProductDTO.class);
    }

    @Override
    public ProductDTO updateImage(Long productId, MultipartFile image) throws IOException {
        //get product from db
        Product productFromDb = productRepository.findById(productId).orElseThrow(() ->
                new ResourceNotFoundException("product", "productId", productId));
        //upload the image to server---to file system
        //get the file name of uploaded image
        String path="/images";
        String fileName=uploadImage(path,image);

        //updating the new file name to the product
        productFromDb.setImage(fileName);
        //save product
        Product saveProduct = productRepository.save(productFromDb);
        //return dto
        return modelMapper.map(saveProduct,ProductDTO.class);
    }

    private String uploadImage(String path, MultipartFile file) throws IOException {
        //file names of current /original file
        String originalFileName=file.getOriginalFilename();
        //generate a unique file(uuid)
        String randomId= UUID.randomUUID().toString();
        //mat.jpg--->1234--->1234.jpg
        assert originalFileName != null;
        String fileName=randomId.concat(originalFileName.substring(originalFileName.lastIndexOf('.')));
//        String filePath=path+ File.pathSeparator+fileName; //----->"/"

        String filePath = path + File.separator + fileName; // ✅ RIGHT

        //check if path exists or just create one
        File folder=new File(path);
        if(!folder.exists()){
            folder.mkdir();
        }
        //upload to server
        Files.copy(file.getInputStream(), Paths.get(filePath));
        //returning file name
        return fileName;


    }


}
