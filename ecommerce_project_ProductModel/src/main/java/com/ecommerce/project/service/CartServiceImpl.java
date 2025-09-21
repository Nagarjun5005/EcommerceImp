package com.ecommerce.project.service;

import com.ecommerce.project.exception.APIException;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.model.Cart;
import com.ecommerce.project.model.CartItem;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.payload.CartDTO;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.repository.CartItemRepository;
import com.ecommerce.project.repository.CartRepository;
import com.ecommerce.project.repository.ProductRepository;
import com.ecommerce.project.util.AuthUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Stream;


@Service
public class CartServiceImpl implements CartService{

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ModelMapper modelMapper;


    @Override
    public CartDTO addProductToCart(Long productId, Integer quantity) {
        // Find existing cart for the logged-in user or create a new one
        Cart cart=createCart();
        // Retrieve product details by productId or throw exception if not found
        Product product=productRepository.findById(productId).orElseThrow(()->new ResourceNotFoundException("Product","productId",productId));
        //Perform validations

        //Retrieve cartItem
        // Check if the product is already present in the cart
        CartItem cartItem=cartItemRepository.findCartItemByProductIdAndCartId(cart.getCartId(),productId);
        if(cartItem!=null){
            throw new APIException("product "+product.getProductName()+"already exists");
        }
        // Check if the product is out of stock
        if(product.getQuantity()==0){
            throw new APIException(product.getProductName()+" is not available");
        }
        // Check if the requested quantity exceeds available stock
        if(product.getQuantity()<quantity){
            throw new APIException(product.getProductName()+" quantities remaining "+product.getQuantity());
        }


        // --- Create and save a new cart item ---
        CartItem newCartItem=new CartItem();
        newCartItem.setCart(cart);
        newCartItem.setProduct(product);
        newCartItem.setQuantity(quantity);
        newCartItem.setDiscount(product.getDiscount());
        newCartItem.setProductPrice(product.getSpecialPrice());

        // Persist the new cart item in the database
        cartItemRepository.save(newCartItem);
        //Updating the database once the product has been added to cart
        product.setQuantity(product.getQuantity());
        // Update the cart's total price with the added product cost
        cart.setTotalPrice(cart.getTotalPrice()+(product.getSpecialPrice()*quantity));
        cartRepository.save(cart);
        // Convert the updated cart entity to CartDTO
        CartDTO cartDto = modelMapper.map(cart, CartDTO.class);
        // Extract all items from the cart and map their products to ProductDTO
        List<CartItem>cartItems=cart.getCartItems();

        Stream<ProductDTO>productDTOStream=cartItems.stream().map(item->{
            ProductDTO map=modelMapper.map(item.getProduct(),ProductDTO.class);
            return map;
        });
        // Attach the list of products to the cart DTO
        cartDto.setProducts(productDTOStream.toList());
        // Return the final updated cart
        return cartDto;
    }

    /**
     * Retrieves the logged-in user's cart, or creates a new one if none exists.
     */
    private Cart createCart(){
        // Try to find an existing cart using the logged-in user's email
        Cart userCart=cartRepository.findCartByEmail(authUtil.loggedInEmail());
        if(userCart!=null){
            return userCart;
        }
        // Create a new cart for the user with total price set to 0
        Cart cart=new Cart();
        cart.setTotalPrice(0.00);
        cart.setUser(authUtil.loggedInUser());
        Cart newCart = cartRepository.save(cart);
        // Save the new cart in the database and return it
        return newCart;
    }
}
