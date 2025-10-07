package com.ecommerce.project.service;

import com.ecommerce.project.model.Address;
import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.AddressDTO;
import com.ecommerce.project.repository.AddressRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class AddressServiceImpl implements AddressService{

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private AddressRepository addressRepository;

    @Override
    public AddressDTO createAddress(AddressDTO addressDTO, User user) {
     //convert addressDto to address
        Address address = modelMapper.map(addressDTO, Address.class);
        //get the addresses of the user
        List<Address>addressList=user.getAddresses();
        //add the new address
        addressList.add(address);
        //update the user address
        user.setAddresses(addressList);

        //set the address for the user
        address.setUser(user);
        //save it to db
        Address savedAddress = addressRepository.save(address);

        //return the address as in model mapper
        return modelMapper.map(address,AddressDTO.class);
    }
}
