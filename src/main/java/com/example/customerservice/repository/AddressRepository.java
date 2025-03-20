package com.example.customerservice.repository;

import com.example.customerservice.model.Address;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends MongoRepository<Address, String> {
    
    List<Address> findByCustomerId(String customerId);
    
    Optional<Address> findByCustomerIdAndIsDefaultTrue(String customerId);
    
    List<Address> findByCity(String city);
    
    List<Address> findByState(String state);
    
    List<Address> findByZipCode(String zipCode);
    
    void deleteByCustomerId(String customerId);
} 