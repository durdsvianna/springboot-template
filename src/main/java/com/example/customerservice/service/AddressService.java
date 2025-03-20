package com.example.customerservice.service;

import com.example.customerservice.dto.AddressDto;

import java.util.List;

public interface AddressService {
    
    AddressDto createAddress(AddressDto addressDto);
    
    AddressDto getAddressById(String id);
    
    List<AddressDto> getAddressesByCustomerId(String customerId);
    
    AddressDto getDefaultAddressByCustomerId(String customerId);
    
    List<AddressDto> searchAddressesByCity(String city);
    
    List<AddressDto> searchAddressesByState(String state);
    
    List<AddressDto> searchAddressesByZipCode(String zipCode);
    
    AddressDto updateAddress(String id, AddressDto addressDto);
    
    AddressDto setAddressAsDefault(String addressId, String customerId);
    
    void deleteAddress(String id);
}