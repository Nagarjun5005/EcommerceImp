package com.ecommerce.project.payload;


import com.ecommerce.project.model.Cart;
import com.ecommerce.project.model.Product;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDTO {
    private Long cartItemId;
    private CartDTO cart;
    private ProductDTO product;
    private Integer quantity;
    private double discount;
    private double productPrice;
}
