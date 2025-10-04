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
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
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

        if (cartItem != null) {
            throw new APIException("Product " + product.getProductName() + " already exists in the cart");
        }

        if (product.getQuantity() == 0) {
            throw new APIException(product.getProductName() + " is not available");
        }

        if (product.getQuantity() < quantity) {
            throw new APIException("Please, make an order of the " + product.getProductName()
                    + " less than or equal to the quantity " + product.getQuantity() + ".");
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

    @Override
    public List<CartDTO> getAllCarts() {
        // Retrieve all Cart entities from the repository
        List<Cart>carts  = cartRepository.findAll();
        // If no carts exist, throw an exception
        if(carts.isEmpty()){
            throw new APIException("No Carts Exists");
        }

        // Convert each Cart entity to CartDTO
        List<CartDTO>cartDTOs=  carts.stream().map(cart->{

            // Map basic Cart fields to CartDTO
            CartDTO cartDto = modelMapper.map(cart, CartDTO.class);

            // Map each CartItem's Product to ProductDTO
            List<ProductDTO>products=cart.getCartItems().stream().map(p->
                modelMapper.map(p.getProduct(),ProductDTO.class)).collect(Collectors.toList());

            // Set the ProductDTO list into CartDTO
          cartDto.setProducts(products);
          return cartDto;
        }).collect(Collectors.toList());

        // Return the list of CartDTOs
        return cartDTOs;
    }

    @Override
    public CartDTO getCart(String emailId, Long cartId) {
        // 1. Fetch cart by email and ID
        Cart cart = cartRepository.findCartByEmailAndCartId(emailId, cartId);
        if (cart == null) {
            throw new ResourceNotFoundException("cart", "cartId", cartId);
        }

        // 2. Map Cart entity → CartDTO (basic fields only)
        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

        // 3. Update Product entity's "quantity" field with CartItem.quantity
        cart.getCartItems().forEach(c -> c.getProduct().setQuantity(c.getQuantity()));

        // 4. Convert Product entities → ProductDTO
        List<ProductDTO> products = cart.getCartItems().stream()
                .map(cartItem -> modelMapper.map(cartItem.getProduct(), ProductDTO.class))
                .toList();

        // 5. Set product list into CartDTO
        cartDTO.setProducts(products);

        return cartDTO;
    }

    @Transactional
    @Override
    public CartDTO updateProductQuantityInCart(Long productId, Integer quantity) {

        // Get the logged-in user's email (who is updating the cart)
        String emailId = authUtil.loggedInEmail();

        // Fetch the cart linked to this user
        Cart userCart = cartRepository.findCartByEmail(emailId);

        // Extract cartId from the fetched cart
        Long cartId = userCart.getCartId();

        // Re-fetch the cart from DB (this is redundant since userCart already has the cart,
        // but here it's being done to ensure it exists & is fully managed by JPA)
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("cart", "cartId", cartId));

        // Fetch product by productId, or throw exception if it doesn't exist
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        // If product is out of stock
        if (product.getQuantity() == 0) {
            throw new APIException(product.getProductName() + " is not available");
        }

        // If user tries to add more quantity than available stock
        if (product.getQuantity() < quantity) {
            throw new APIException("Please, make an order of the " + product.getProductName()
                    + " less than or equal to the quantity " + product.getQuantity() + ".");
        }

        // Fetch the existing CartItem for this product in the user's cart
        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId);
        if (cartItem == null) {
            throw new APIException("Product " + product.getProductName() + " not available in the cart!!");
        }

        int newQuantity=cartItem.getQuantity()+quantity;
        if(newQuantity<0){
            throw new APIException("The resulting quantity cannot be zero");
        }

        if(newQuantity==0){
            deleteProductFromCart(cartId,productId);
        }else {

            // Update cartItem details with latest product values
            cartItem.setProductPrice(product.getSpecialPrice());
            cartItem.setQuantity(cartItem.getQuantity() + quantity); // increment/decrement quantity
            cartItem.setDiscount(product.getDiscount());

            // Update the total price of the cart
            cart.setTotalPrice(cart.getTotalPrice() + (cartItem.getProductPrice() * quantity));

            // Save the updated cart
            cartRepository.save(cart);
        }

        // Save the updated cart item
        CartItem updatedItem = cartItemRepository.save(cartItem);

        // If the updated item’s quantity becomes 0, remove it from the cart
        if (updatedItem.getQuantity() == 0) {
            cartItemRepository.deleteById(updatedItem.getCartItemId());
        }

        // Convert cart entity to DTO
        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

        // Map each cart item’s product to ProductDTO with the updated quantity
        List<CartItem> cartItems = cart.getCartItems();
        Stream<ProductDTO> productDTOStream = cartItems.stream().map(item -> {
            ProductDTO prd = modelMapper.map(item.getProduct(), ProductDTO.class);
            prd.setQuantity(item.getQuantity()); // override product stock with cart quantity
            return prd;
        });

        // Set the products inside CartDTO
        cartDTO.setProducts(productDTOStream.toList());

        // Return the final updated cart
        return cartDTO;
    }


    @Transactional
    @Override
    public String deleteProductFromCart(Long cartId, Long productId) {

        // 1. Fetch the cart by its ID from the database.
        //    If the cart does not exist, throw a ResourceNotFoundException.
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));

        // 2. Fetch the specific CartItem corresponding to the cart and product.
        //    This represents the product entry in the cart.
        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId);

        // 3. If the CartItem does not exist, throw an exception indicating the product is not in the cart.
        if (cartItem == null) {
            throw new ResourceNotFoundException("Product", "productId", productId);
        }

        // 4. Before deleting, update the cart's total price.
        //    Subtract the total price contribution of this product (price * quantity) from the cart total.
        cart.setTotalPrice(cart.getTotalPrice() - (cartItem.getProductPrice() * cartItem.getQuantity()));

        // 5. Delete the cart item from the cart in the database.
        cartItemRepository.deleteCartItemByProductIdAndCartId(cartId, productId);

        // 6. Return a success message including the product name to confirm deletion.
        return "Product " + cartItem.getProduct().getProductName() + " removed from the cart!!";
    }

    @Override
    public void updateProductInCarts(Long cartId, Long productId) {
        // ✅ 1. Fetch the cart by ID
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));
        // ✅ 2. Fetch the product by ID
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));
        // ✅ 3. Find the cart item (bridge between Cart and Product)
        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId);
        if (cartItem == null) {
            throw new APIException("Product " + product.getProductName() + " not available in the cart!!!");
        }
        // ✅ 4. Subtract the old product price from the cart total
        double cartPrice = cart.getTotalPrice()
                - (cartItem.getProductPrice() * cartItem.getQuantity());
        // ✅ 5. Update the cart item with the latest product special price
        cartItem.setProductPrice(product.getSpecialPrice());

        // ✅ 6. Add the updated product total back to the cart
        cart.setTotalPrice(cartPrice
                + (cartItem.getProductPrice() * cartItem.getQuantity()));
        // ✅ 7. Save the updated cart item (and cascaded cart total if needed)
        cartItem = cartItemRepository.save(cartItem);
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
