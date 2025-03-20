package com.example.customerservice.service.impl;

import com.example.customerservice.dto.AddressDto;
import com.example.customerservice.dto.CustomerDto;
import com.example.customerservice.model.Customer;
import com.example.customerservice.repository.CustomerRepository;
import com.example.customerservice.service.AddressService;
import com.example.customerservice.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final AddressService addressService;

    @Override
    @Transactional
    public CustomerDto createCustomer(CustomerDto customerDto) {
        if (customerRepository.existsByEmail(customerDto.getEmail())) {
            throw new IllegalArgumentException("Customer with email " + customerDto.getEmail() + " already exists");
        }

        Customer customer = toEntity(customerDto);
        customer.setCreatedAt(LocalDateTime.now());
        customer.setUpdatedAt(LocalDateTime.now());
        customer.setAddressIds(new ArrayList<>());

        Customer savedCustomer = customerRepository.save(customer);

        // Handle addresses if provided
        List<AddressDto> addresses = customerDto.getAddresses();
        if (addresses != null && !addresses.isEmpty()) {
            addresses.forEach(addressDto -> {
                addressDto.setCustomerId(savedCustomer.getId());
                AddressDto savedAddress = addressService.createAddress(addressDto);
                savedCustomer.getAddressIds().add(savedAddress.getId());
            });
            customerRepository.save(savedCustomer);
        }

        return toDto(savedCustomer);
    }

    @Override
    public CustomerDto getCustomerById(String id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Customer not found with id: " + id));
        
        return enrichWithAddresses(customer);
    }

    @Override
    public CustomerDto getCustomerByEmail(String email) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("Customer not found with email: " + email));
        
        return enrichWithAddresses(customer);
    }

    @Override
    public List<CustomerDto> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CustomerDto> searchCustomers(String firstName, String lastName) {
        List<Customer> customers;
        
        if (firstName != null && lastName != null) {
            customers = customerRepository.findByFirstNameContainingIgnoreCaseAndLastNameContainingIgnoreCase(firstName, lastName);
        } else if (firstName != null) {
            customers = customerRepository.findByFirstNameContainingIgnoreCase(firstName);
        } else if (lastName != null) {
            customers = customerRepository.findByLastNameContainingIgnoreCase(lastName);
        } else {
            customers = customerRepository.findAll();
        }
        
        return customers.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CustomerDto updateCustomer(String id, CustomerDto customerDto) {
        Customer existingCustomer = customerRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Customer not found with id: " + id));

        // Check if email is being changed and if it's already in use
        if (!existingCustomer.getEmail().equals(customerDto.getEmail()) && 
                customerRepository.existsByEmail(customerDto.getEmail())) {
            throw new IllegalArgumentException("Email already in use: " + customerDto.getEmail());
        }

        existingCustomer.setFirstName(customerDto.getFirstName());
        existingCustomer.setLastName(customerDto.getLastName());
        existingCustomer.setEmail(customerDto.getEmail());
        existingCustomer.setPhone(customerDto.getPhone());
        existingCustomer.setUpdatedAt(LocalDateTime.now());

        Customer updatedCustomer = customerRepository.save(existingCustomer);
        return enrichWithAddresses(updatedCustomer);
    }

    @Override
    @Transactional
    public void deleteCustomer(String id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Customer not found with id: " + id));
        
        // Delete all addresses associated with this customer
        addressService.deleteCustomerAddresses(id);
        
        // Delete the customer
        customerRepository.delete(customer);
    }

    private CustomerDto enrichWithAddresses(Customer customer) {
        CustomerDto dto = toDto(customer);
        List<AddressDto> addresses = addressService.getAddressesByCustomerId(customer.getId());
        dto.setAddresses(addresses);
        return dto;
    }

    private Customer toEntity(CustomerDto dto) {
        return Customer.builder()
                .id(dto.getId())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }

    private CustomerDto toDto(Customer entity) {
        return CustomerDto.builder()
                .id(entity.getId())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
} 