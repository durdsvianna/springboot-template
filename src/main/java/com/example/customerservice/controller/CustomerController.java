package com.example.customerservice.controller;

import com.example.customerservice.dto.CustomerDto;
import com.example.customerservice.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
@Tag(name = "Customer", description = "Customer management API")
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    @Operation(summary = "Create a new customer", description = "Creates a new customer with optional addresses")
    public ResponseEntity<CustomerDto> createCustomer(@Valid @RequestBody CustomerDto customerDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.createCustomer(customerDto));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get customer by ID", description = "Returns a customer by their ID")
    public ResponseEntity<CustomerDto> getCustomerById(
            @Parameter(description = "Customer ID", required = true) @PathVariable String id) {
        return ResponseEntity.ok(customerService.getCustomerById(id));
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Get customer by email", description = "Returns a customer by their email address")
    public ResponseEntity<CustomerDto> getCustomerByEmail(
            @Parameter(description = "Customer email", required = true) @PathVariable String email) {
        return ResponseEntity.ok(customerService.getCustomerByEmail(email));
    }

    @GetMapping
    @Operation(summary = "Get all customers or search by name", description = "Returns all customers or filters by first/last name if provided")
    public ResponseEntity<List<CustomerDto>> getCustomers(
            @Parameter(description = "First name to search (optional)") @RequestParam(required = false) String firstName,
            @Parameter(description = "Last name to search (optional)") @RequestParam(required = false) String lastName) {
        
        if (firstName != null) {
            return ResponseEntity.ok(customerService.searchCustomersByFirstName(firstName));
        } else if (lastName != null) {
            return ResponseEntity.ok(customerService.searchCustomersByLastName(lastName));
        } else {
            return ResponseEntity.ok(customerService.getAllCustomers());
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update customer", description = "Updates an existing customer by ID")
    public ResponseEntity<CustomerDto> updateCustomer(
            @Parameter(description = "Customer ID", required = true) @PathVariable String id,
            @Valid @RequestBody CustomerDto customerDto) {
        return ResponseEntity.ok(customerService.updateCustomer(id, customerDto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete customer", description = "Deletes a customer and all their addresses")
    public ResponseEntity<Void> deleteCustomer(
            @Parameter(description = "Customer ID", required = true) @PathVariable String id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }
}