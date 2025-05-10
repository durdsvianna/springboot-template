package com.example.customerservice.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.customerservice.dto.CustomerDto;
import com.example.customerservice.service.CustomerService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
@Tag(name = "Customer", description = "Customer management endpoints")
public class CustomerController {

    private final CustomerService customerService;

    @Operation(summary = "Create a new customer")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Customer created", 
                content = @Content(schema = @Schema(implementation = CustomerDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "409", description = "Customer already exists")
    })
    @PostMapping
    public ResponseEntity<CustomerDto> createCustomer(
            @Valid @RequestBody CustomerDto customerDto) {
        
        CustomerDto createdCustomer = customerService.createCustomer(customerDto);
        
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdCustomer.getId())
                .toUri();
        
        return ResponseEntity.created(location).body(createdCustomer);
    }

    @Operation(summary = "Get a customer by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Found the customer", 
                content = @Content(schema = @Schema(implementation = CustomerDto.class))),
        @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CustomerDto> getCustomerById(
            @Parameter(description = "Customer ID") @PathVariable String id) {
        
        CustomerDto customer = customerService.getCustomerById(id);
        return ResponseEntity.ok(customer);
    }

    @Operation(summary = "Get a customer by email")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Found the customer", 
                content = @Content(schema = @Schema(implementation = CustomerDto.class))),
        @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @GetMapping("/email/{email}")
    public ResponseEntity<CustomerDto> getCustomerByEmail(
            @Parameter(description = "Customer email") @PathVariable String email) {
        
        CustomerDto customer = customerService.getCustomerByEmail(email);
        return ResponseEntity.ok(customer);
    }

    @Operation(summary = "Update a customer")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Customer updated", 
                content = @Content(schema = @Schema(implementation = CustomerDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "404", description = "Customer not found"),
        @ApiResponse(responseCode = "409", description = "Email already in use")
    })
    @PutMapping("/{id}")
    public ResponseEntity<CustomerDto> updateCustomer(
            @Parameter(description = "Customer ID") @PathVariable String id,
            @Valid @RequestBody CustomerDto customerDto) {
        
        CustomerDto updatedCustomer = customerService.updateCustomer(id, customerDto);
        return ResponseEntity.ok(updatedCustomer);
    }

    @Operation(summary = "Delete a customer")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Customer deleted"),
        @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCustomer(
            @Parameter(description = "Customer ID") @PathVariable String id) {
        
        customerService.deleteCustomer(id);
    }

    @Operation(summary = "Get all customers")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of customers returned")
    })
    @GetMapping
    public ResponseEntity<List<CustomerDto>> getAllCustomers() {
        List<CustomerDto> customers = customerService.getAllCustomers();
        return ResponseEntity.ok(customers);
    }

    @Operation(summary = "Search customers by first name")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of matching customers returned")
    })
    @GetMapping("/search/firstname")
    public ResponseEntity<List<CustomerDto>> searchByFirstName(
            @Parameter(description = "First name to search for") 
            @RequestParam String firstName) {
        
        List<CustomerDto> customers = customerService.findCustomersByFirstName(firstName);
        return ResponseEntity.ok(customers);
    }

    @Operation(summary = "Search customers by last name")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of matching customers returned")
    })
    @GetMapping("/search/lastname")
    public ResponseEntity<List<CustomerDto>> searchByLastName(
            @Parameter(description = "Last name to search for") 
            @RequestParam String lastName) {
        
        List<CustomerDto> customers = customerService.findCustomersByLastName(lastName);
        return ResponseEntity.ok(customers);
    }
}