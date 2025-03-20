package com.example.customerservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.example.customerservice.model.Address;

@Repository
public interface AddressRepository extends MongoRepository<Address, String> {
    
    /**
     * Find all addresses for a customer
     * 
     * @param customerId The customer ID
     * @return List of addresses for the customer
     */
    List<Address> findByCustomerId(String customerId);
    
    /**
     * Find addresses by customer ID and city
     * 
     * @param customerId The customer ID
     * @param city The city to search for
     * @return List of matching addresses
     */
    List<Address> findByCustomerIdAndCity(String customerId, String city);
    
    /**
     * Find default address for a customer
     * 
     * @param customerId The customer ID
     * @param isDefault Should be true to find the default address
     * @return Optional containing the default address if found
     */
    Optional<Address> findByCustomerIdAndIsDefault(String customerId, boolean isDefault);
    
    /**
     * Find addresses by city
     * 
     * @param city The city to search for
     * @return List of matching addresses
     */
    List<Address> findByCity(String city);
    
    /**
     * Find addresses by state
     * 
     * @param state The state to search for
     * @return List of matching addresses
     */
    List<Address> findByState(String state);
    
    /**
     * Find addresses by zip code
     * 
     * @param zipCode The zip code to search for
     * @return List of matching addresses
     */
    List<Address> findByZipCode(String zipCode);
    
    /**
     * Find addresses by country
     * 
     * @param country The country to search for
     * @return List of matching addresses
     */
    List<Address> findByCountry(String country);
    
    /**
     * Delete all addresses for a customer
     * 
     * @param customerId The customer ID
     */
    void deleteByCustomerId(String customerId);
}