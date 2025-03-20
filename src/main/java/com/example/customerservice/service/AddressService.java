package com.example.customerservice.service;

import com.example.customerservice.dto.AddressDto;

import java.util.List;

public interface AddressService {
    
    AddressDto createAddress(AddressDto addressDto);
    
    AddressDto getAddressById(String id);
    
    List<AddressDto> getAddressesByCustomerId(String customerId);
    
    AddressDto getDefaultAddress(String customerId);
    
    List<AddressDto> searchAddresses(String city, String state, String zipCode);
    
    AddressDto updateAddress(String id, AddressDto addressDto);
    
    void deleteAddress(String id);
    
    void deleteCustomerAddresses(String customerId);
    
    AddressDto setDefaultAddress(String addressId, String customerId);
} 