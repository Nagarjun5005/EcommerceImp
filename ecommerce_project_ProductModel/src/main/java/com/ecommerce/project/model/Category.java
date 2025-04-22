package com.ecommerce.project.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long categoryId;


    @NotBlank(message = "category name cannot be blank!!")
    @Size(min = 5,message = "Category should have least 5 characters ")
    public String categoryName;


    @OneToMany(mappedBy = "category",cascade = CascadeType.ALL)
    private List<Product>products;

}


