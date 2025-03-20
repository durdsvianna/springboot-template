package com.example.customerservice.service;

import com.example.customerservice.dto.CustomerDto;

import java.util.List;

public interface CustomerService {
    
    CustomerDto createCustomer(CustomerDto customerDto);
    
    CustomerDto getCustomerById(String id);
    
    CustomerDto getCustomerByEmail(String email);
    
    List<CustomerDto> getAllCustomers();
    
    List<CustomerDto> searchCustomers(String firstName, String lastName);
    
    CustomerDto updateCustomer(String id, CustomerDto customerDto);
    
    void deleteCustomer(String id);
} 