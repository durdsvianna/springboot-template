package com.example.customerservice.service;

import java.util.List;

import com.example.customerservice.dto.AddressDto;

public interface AddressService {

    /**
     * Create a new address for a customer
     * 
     * @param customerId The customer ID
     * @param addressDto The address DTO to create
     * @return The created address DTO
     */
    AddressDto createAddress(String customerId, AddressDto addressDto);

    /**
     * Get an address by ID
     * 
     * @param id The address ID
     * @return The address DTO
     */
    AddressDto getAddressById(String id);

    /**
     * Get all addresses for a customer
     * 
     * @param customerId The customer ID
     * @return List of address DTOs
     */
    List<AddressDto> getAddressesByCustomerId(String customerId);

    /**
     * Get the default address for a customer
     * 
     * @param customerId The customer ID
     * @return The default address DTO
     */
    AddressDto getDefaultAddress(String customerId);

    /**
     * Update an address
     * 
     * @param id The address ID
     * @param addressDto The address DTO with updates
     * @return The updated address DTO
     */
    AddressDto updateAddress(String id, AddressDto addressDto);

    /**
     * Set an address as the default
     * 
     * @param addressId The address ID to set as default
     * @param customerId The customer ID
     * @return The updated address DTO
     */
    AddressDto setAddressAsDefault(String addressId, String customerId);

    /**
     * Delete an address
     * 
     * @param id The address ID
     */
    void deleteAddress(String id);

    /**
     * Search addresses by city
     * 
     * @param customerId The customer ID
     * @param city The city to search for
     * @return List of matching address DTOs
     */
    List<AddressDto> findAddressesByCity(String customerId, String city);

    /**
     * Delete all addresses for a customer
     * 
     * @param customerId The customer ID
     */
    void deleteAddressesByCustomerId(String customerId);
}