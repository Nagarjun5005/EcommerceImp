package com.ecommerce.project.service;

import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.model.Address;
import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.AddressDTO;
import com.ecommerce.project.repository.AddressRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


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

    @Override
    public List<AddressDTO> getAllAddress() {
        // 1️⃣ Fetch all addresses from the database
        List<Address> addressList = addressRepository.findAll();
        // 2️⃣ Convert list of Address → list of AddressDTO
        List<AddressDTO> addressDTOList = addressList.stream().map(address ->
            modelMapper.map(address, AddressDTO.class))
        .collect(Collectors.toList());
        // 3️⃣ Return the DTO list
        return addressDTOList;
    }

    @Override
    public AddressDTO getAddressById(Long id) {
        //get the address by id : if not found throw resource not found exception
        Address addressFound = addressRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("Address","AddressId",id));
        //convert address to addressDto
        AddressDTO addressDTOFound    = modelMapper.map(addressFound, AddressDTO.class);
        //return the addressDto
        return addressDTOFound;
    }
}
