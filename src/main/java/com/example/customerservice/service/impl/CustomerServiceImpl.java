package com.example.customerservice.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.customerservice.dto.CustomerDto;
import com.example.customerservice.exception.DuplicateResourceException;
import com.example.customerservice.exception.NotFoundException;
import com.example.customerservice.mapper.CustomerMapper;
import com.example.customerservice.model.Customer;
import com.example.customerservice.repository.CustomerRepository;
import com.example.customerservice.service.AddressService;
import com.example.customerservice.service.CustomerService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final AddressService addressService;

    @Override
    @Transactional
    public CustomerDto createCustomer(CustomerDto customerDto) {
        log.debug("Creating customer with email: {}", customerDto.getEmail());
        
        if (customerRepository.existsByEmail(customerDto.getEmail())) {
            throw new DuplicateResourceException("Customer", "email", customerDto.getEmail());
        }
        
        Customer customer = customerMapper.toEntity(customerDto);
        Customer savedCustomer = customerRepository.save(customer);
        
        return customerMapper.toDto(savedCustomer);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerDto getCustomerById(String id) {
        log.debug("Getting customer with ID: {}", id);
        
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Customer", id));
        
        return customerMapper.toDto(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerDto getCustomerByEmail(String email) {
        log.debug("Getting customer with email: {}", email);
        
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Customer", "email", email));
        
        return customerMapper.toDto(customer);
    }

    @Override
    @Transactional
    public CustomerDto updateCustomer(String id, CustomerDto customerDto) {
        log.debug("Updating customer with ID: {}", id);
        
        Customer existingCustomer = customerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Customer", id));
        
        // Check if email is being changed and if it already exists
        if (!existingCustomer.getEmail().equals(customerDto.getEmail()) && 
            customerRepository.existsByEmail(customerDto.getEmail())) {
            throw new DuplicateResourceException("Customer", "email", customerDto.getEmail());
        }
        
        Customer updatedCustomer = customerMapper.updateEntity(customerDto, existingCustomer);
        Customer savedCustomer = customerRepository.save(updatedCustomer);
        
        return customerMapper.toDto(savedCustomer);
    }

    @Override
    @Transactional
    public void deleteCustomer(String id) {
        log.debug("Deleting customer with ID: {}", id);
        
        if (!customerRepository.existsById(id)) {
            throw new NotFoundException("Customer", id);
        }
        
        // First delete all addresses associated with the customer
        addressService.deleteAddressesByCustomerId(id);
        
        // Then delete the customer
        customerRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerDto> getAllCustomers() {
        log.debug("Getting all customers");
        
        List<Customer> customers = customerRepository.findAll();
        
        return customerMapper.toDtoList(customers);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerDto> findCustomersByFirstName(String firstName) {
        log.debug("Finding customers with first name: {}", firstName);
        
        List<Customer> customers = customerRepository.findByFirstName(firstName);
        
        return customerMapper.toDtoList(customers);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerDto> findCustomersByLastName(String lastName) {
        log.debug("Finding customers with last name: {}", lastName);
        
        List<Customer> customers = customerRepository.findByLastName(lastName);
        
        return customerMapper.toDtoList(customers);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return customerRepository.existsByEmail(email);
    }
}