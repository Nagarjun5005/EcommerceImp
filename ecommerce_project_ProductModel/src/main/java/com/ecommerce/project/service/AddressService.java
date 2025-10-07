package com.ecommerce.project.service;


import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.AddressDTO;
import org.springframework.stereotype.Service;

@Service
public interface AddressService {

    AddressDTO createAddress(AddressDTO address, User user);
}
