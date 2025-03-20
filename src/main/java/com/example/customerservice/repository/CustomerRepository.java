package com.example.customerservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.example.customerservice.model.Customer;

@Repository
public interface CustomerRepository extends MongoRepository<Customer, String> {
    
    /**
     * Find a customer by email
     * 
     * @param email The email to search for
     * @return Optional containing the customer if found
     */
    Optional<Customer> findByEmail(String email);
    
    /**
     * Find customers by first name
     * 
     * @param firstName The first name to search for
     * @return List of matching customers
     */
    List<Customer> findByFirstName(String firstName);
    
    /**
     * Find customers by last name
     * 
     * @param lastName The last name to search for
     * @return List of matching customers
     */
    List<Customer> findByLastName(String lastName);
    
    /**
     * Find customers by first name and last name
     * 
     * @param firstName The first name to search for
     * @param lastName The last name to search for
     * @return List of matching customers
     */
    List<Customer> findByFirstNameAndLastName(String firstName, String lastName);
    
    /**
     * Find customers by phone
     * 
     * @param phone The phone number to search for
     * @return List of matching customers
     */
    List<Customer> findByPhone(String phone);
    
    /**
     * Check if a customer exists by email
     * 
     * @param email The email to check
     * @return true if a customer with the email exists, false otherwise
     */
    boolean existsByEmail(String email);
    
    /**
     * Delete a customer by email
     * 
     * @param email The email of the customer to delete
     */
    void deleteByEmail(String email);
}