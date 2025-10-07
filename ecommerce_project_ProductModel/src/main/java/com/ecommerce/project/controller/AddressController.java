package com.ecommerce.project.controller;


import com.ecommerce.project.model.Address;
import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.AddressDTO;
import com.ecommerce.project.service.AddressService;
import com.ecommerce.project.util.AuthUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AddressController {


    @Autowired
    private AddressService addressService;

    @Autowired
    private AuthUtil  authUtil;

    @PostMapping("/address")
    public ResponseEntity<AddressDTO> createAddress(@Valid @RequestBody AddressDTO address){
        User user= authUtil.loggedInUser();
       AddressDTO addressDTO= addressService.createAddress(address,user);
       return new ResponseEntity<AddressDTO>(addressDTO, HttpStatus.CREATED);
    }
}
