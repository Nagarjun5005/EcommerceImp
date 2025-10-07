package com.ecommerce.project.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "addresses")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long addressId;

    @NotBlank
    @Size(min=5,message = "Street name must be least 5 chars")
    private String street;


    @NotBlank
    @Size(min = 5,message = "building name must be least 5 chars")
    private String buildingName;

    @NotBlank
    @Size(min = 4,message = "city name must be least 4 chars")
    private String city;

    @NotBlank
    @Size(min = 2,message = "state name must be least 2 chars")
    private String state;

    @NotBlank
    @Size(min = 2,message = "country name must be least 2 chars")
    private String country;

    @NotBlank
    @Size(min = 6,message = "pincode  must be least 6 chars")
    private String pincode;


    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Address(String country, String street, String buildingName, String city, String state, String pincode) {
        this.country = country;
        this.street = street;
        this.buildingName = buildingName;
        this.city = city;
        this.state = state;
        this.pincode = pincode;
    }
}
