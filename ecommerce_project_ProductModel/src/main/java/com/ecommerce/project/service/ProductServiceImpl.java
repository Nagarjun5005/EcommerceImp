package com.ecommerce.project.service;


import com.ecommerce.project.exception.APIException;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.model.Cart;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.payload.CartDTO;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.payload.ProductResponse;
import com.ecommerce.project.repository.CartRepository;
import com.ecommerce.project.repository.CategoryRepository;
import com.ecommerce.project.repository.ProductRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class ProductServiceImpl implements ProductService{

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private FileService fileService;


    @Value("${project.image}")
    private String path;


    @Override
    public ProductDTO addProduct(ProductDTO productDTO, Long categoryId) {
       //find the category and map to product
        Category category = categoryRepository.findById(categoryId).orElseThrow(() ->
                new ResourceNotFoundException("category", "categoryId", categoryId));

        //add validation --->if same product is added again--->throw ApiException-->"product Already Exists"

        boolean isProductNotPresent=true;
        List<Product> products = category.getProducts();
        for(Product value:products){
            if(value.getProductName().equals(productDTO.getProductName())){
                isProductNotPresent=false;
                break;
            }
        }

        if(isProductNotPresent){
            Product product=modelMapper.map(productDTO,Product.class);
            product.setImage("default.png");
            product.setCategory(category);
            //calculate specialPrice =Special Price = Price - (Price × (Discount / 100))
            double specialPrice= product.getPrice()-(product.getPrice()*(product.getDiscount()/100));
            product.setSpecialPrice(specialPrice);
            //save the object
            Product savedProduct = productRepository.save(product);
            return modelMapper.map(savedProduct,ProductDTO.class);
        }else{
            throw new APIException("Product already exist");
        }



    }

    @Override
    public ProductResponse getAllProduct(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails= PageRequest.of(pageNumber,pageSize,sortByAndOrder);
        Page<Product> pageProducts = productRepository.findAll(pageDetails);

        List<Product> allProducts  = pageProducts.getContent();
        //need to convert all the product to product dto
        List<ProductDTO> productDTOList = allProducts.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .collect(Collectors.toList());

        ProductResponse productResponse=new ProductResponse();
        productResponse.setContent(productDTOList);
        productResponse.setPageNumber(pageProducts.getNumber());
        productResponse.setPageSize(pageProducts.getSize());
        productResponse.setTotalItems(pageProducts.getTotalElements());
        productResponse.setTotalPages(pageProducts.getTotalPages());
        productResponse.setLastPage(pageProducts.isLast());
        return productResponse;
    }

    @Override
    public ProductResponse searchByCategory(Long categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {

        //find the category
        Category categoryFound = categoryRepository.findById(categoryId).orElseThrow(() ->
                new ResourceNotFoundException("category", "categoryId", categoryId));


        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails= PageRequest.of(pageNumber,pageSize,sortByAndOrder);
        Page<Product> pageProducts = productRepository.findByCategoryOrderByPriceAsc(categoryFound,pageDetails);

        //query used to find the products by categoryId
        List<Product> productList =pageProducts.getContent();

        //convert product to product dto-->and collect it as a list
        List<ProductDTO> productDTOList = productList.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .collect(Collectors.toList());

        //return the ProductResponse
        ProductResponse productResponse=new ProductResponse();
        productResponse.setContent(productDTOList);
        productResponse.setPageNumber(pageProducts.getNumber());
        productResponse.setPageSize(pageProducts.getSize());
        productResponse.setTotalItems(pageProducts.getTotalElements());
        productResponse.setTotalPages(pageProducts.getTotalPages());
        productResponse.setLastPage(pageProducts.isLast());

        return productResponse;




    }

    @Override
    public ProductResponse searchProductByKeyWord(String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();


        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Product> pageProducts = productRepository.findByProductNameLikeIgnoreCase("%" + keyword + "%", pageDetails);


        //getting the data from database---using query
     // List<Product>productList= productRepository.findByProductNameLikeIgnoreCase('%'+keyword+'%');
      List<Product> productList = pageProducts.getContent();
        //convert product to product dto-->and collect it as a list
        List<ProductDTO> productDTOList = productList.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .collect(Collectors.toList());

        //return the ProductResponse
        ProductResponse productResponse=new ProductResponse();
        productResponse.setContent(productDTOList);
        productResponse.setPageNumber(pageProducts.getNumber());
        productResponse.setPageSize(pageProducts.getSize());
        productResponse.setTotalItems(pageProducts.getTotalElements());
        productResponse.setTotalPages(pageProducts.getTotalPages());
        productResponse.setLastPage(pageProducts.isLast());
        return productResponse;

    }

    @Override
    public ProductDTO updateProduct(Product product, Long productId) {
        // ✅ 1. Fetch the existing product from the database
        // If the product does not exist, throw a ResourceNotFoundException
        Product productFound = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("product", "productId", productId));
        // ✅ 2. Update the product fields with the new values from the input
        productFound.setProductName(product.getProductName());
        productFound.setQuantity(product.getQuantity());
        productFound.setPrice(product.getPrice());
        productFound.setDescription(product.getDescription());
        // ✅ 3. Recalculate the special price after applying the discount
        double specialPrice= product.getPrice()-(product.getPrice()*(product.getDiscount()/100));
        productFound.setSpecialPrice(specialPrice);

        // ✅ 4. Save the updated product details to the database
        productRepository.save(productFound);

        // ✅ 5. Retrieve all carts that currently contain this product
        // (So that their prices can be updated to reflect the new product price)
        List<Cart>carts=cartRepository.findCartsByProductId(productId);

        // 6. Convert carts to DTO (optional — for mapping convenience)
        List<CartDTO>cartDTOS=carts.stream().map(cart -> {
            CartDTO cartDTO=modelMapper.map(cart,CartDTO.class);

            List<ProductDTO>products=cart.getCartItems()
                    .stream()
                    .map(p->modelMapper.map(p.getProduct(),ProductDTO.class))
                    .collect(Collectors.toList());

            cartDTO.setProducts(products);
            return cartDTO;

        }).collect(Collectors.toList());

        //  7. For each cart containing this product, update its cart item
        cartDTOS.forEach(cart->cartService.updateProductInCarts(cart.getCartId(),productId));
        // ✅ 8. Return the updated product mapped to a ProductDTO
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
        String path="images/";
        String fileName=fileService.uploadImage(path,image);

        //updating the new file name to the product
        productFromDb.setImage(fileName);
        //save product
        Product saveProduct = productRepository.save(productFromDb);
        //return dto
        return modelMapper.map(saveProduct,ProductDTO.class);
    }




}
