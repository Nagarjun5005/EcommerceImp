package com.ecommerce.project.controller;


import com.ecommerce.project.model.Cart;
import com.ecommerce.project.payload.CartDTO;
import com.ecommerce.project.repository.CartRepository;
import com.ecommerce.project.service.CartService;
import com.ecommerce.project.util.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private AuthUtil authUtil;

    @PostMapping("/carts/products/{productId}/quantity/{quantity}")
    private ResponseEntity<CartDTO>addProductToCart(@PathVariable Long productId,@PathVariable Integer quantity){
         CartDTO cartDTO= cartService.addProductToCart(productId,quantity);
         return new ResponseEntity<>(cartDTO, HttpStatus.CREATED);
    }

    @GetMapping("/carts")
    private ResponseEntity<List<CartDTO>>getAllCarts(){
        List<CartDTO>carts=cartService.getAllCarts();
        return new ResponseEntity<>(carts,HttpStatus.FOUND);
    }

    @GetMapping("/carts/users/cart")
    private ResponseEntity<CartDTO>getUserCart(){
        String emailId=authUtil.loggedInEmail();
        Cart cart=cartRepository.findCartByEmail(emailId);
        Long cartId=cart.getCartId();
       CartDTO cartDTO= cartService.getCart(emailId,cartId);
       return new ResponseEntity<CartDTO>(cartDTO,HttpStatus.FOUND);
    }
}
