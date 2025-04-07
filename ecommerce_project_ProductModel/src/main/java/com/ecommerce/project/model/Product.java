package com.ecommerce.project.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;
    private String productName;
    private String Description;
    private Integer quantity;
    private double price;
    private double specialPrice;
    private double discount;
    private String image;


    @ManyToOne
    @JoinColumn(name = "categoryId", nullable = false)
    private Category category;

}
