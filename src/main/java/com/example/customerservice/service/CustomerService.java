package com.example.customerservice.service;

import java.util.List;

import com.example.customerservice.dto.CustomerDto;

public interface CustomerService {

    /**
     * Create a new customer
     * 
     * @param customerDto The customer DTO to create
     * @return The created customer DTO
     */
    CustomerDto createCustomer(CustomerDto customerDto);

    /**
     * Get a customer by ID
     * 
     * @param id The customer ID
     * @return The customer DTO
     */
    CustomerDto getCustomerById(String id);

    /**
     * Get a customer by email
     * 
     * @param email The customer email
     * @return The customer DTO
     */
    CustomerDto getCustomerByEmail(String email);

    /**
     * Update a customer
     * 
     * @param id The customer ID
     * @param customerDto The customer DTO with updates
     * @return The updated customer DTO
     */
    CustomerDto updateCustomer(String id, CustomerDto customerDto);

    /**
     * Delete a customer
     * 
     * @param id The customer ID
     */
    void deleteCustomer(String id);

    /**
     * Get all customers
     * 
     * @return List of all customer DTOs
     */
    List<CustomerDto> getAllCustomers();

    /**
     * Search customers by first name
     * 
     * @param firstName The first name to search for
     * @return List of matching customer DTOs
     */
    List<CustomerDto> findCustomersByFirstName(String firstName);

    /**
     * Search customers by last name
     * 
     * @param lastName The last name to search for
     * @return List of matching customer DTOs
     */
    List<CustomerDto> findCustomersByLastName(String lastName);

    /**
     * Check if a customer exists by email
     * 
     * @param email The email to check
     * @return true if a customer with the email exists, false otherwise
     */
    boolean existsByEmail(String email);
}