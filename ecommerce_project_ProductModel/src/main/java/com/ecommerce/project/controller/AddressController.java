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
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/address")
    public ResponseEntity<List<AddressDTO>>getAllAddress(){
       List<AddressDTO>addressDTOList= addressService.getAllAddress();
       return new ResponseEntity<>(addressDTOList,HttpStatus.FOUND);
    }

    @GetMapping("/address/{id}")
    public ResponseEntity<AddressDTO>getAddressById(@PathVariable Long id){
        AddressDTO addressDTO=addressService.getAddressById(id);
        return new ResponseEntity<AddressDTO>(addressDTO,HttpStatus.FOUND);
    }


    @GetMapping("/users/address")
    public ResponseEntity<List<AddressDTO>>getAddressByUser(){
        User user = authUtil.loggedInUser();
        List<AddressDTO>  addressDTO=addressService.getUserAddress(user);
        return new ResponseEntity<List<AddressDTO>>(addressDTO,HttpStatus.FOUND);
    }

    @PutMapping("/address/{addressId}")
    public ResponseEntity<AddressDTO>updateAddress(@RequestBody AddressDTO addressDTO, @PathVariable Long addressId){
        AddressDTO updatedAddress=addressService.updateAddress(addressDTO,addressId);
        return new ResponseEntity<AddressDTO>(updatedAddress,HttpStatus.OK);

    }

}
