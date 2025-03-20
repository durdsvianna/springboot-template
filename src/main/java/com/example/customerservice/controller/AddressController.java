package com.example.customerservice.controller;

import com.example.customerservice.dto.AddressDto;
import com.example.customerservice.service.AddressService;
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
@RequestMapping("/addresses")
@RequiredArgsConstructor
@Tag(name = "Address", description = "Address management API")
public class AddressController {

    private final AddressService addressService;

    @PostMapping
    @Operation(summary = "Create a new address", description = "Creates a new address for a customer")
    public ResponseEntity<AddressDto> createAddress(@Valid @RequestBody AddressDto addressDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(addressService.createAddress(addressDto));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get address by ID", description = "Returns an address by its ID")
    public ResponseEntity<AddressDto> getAddressById(
            @Parameter(description = "Address ID", required = true) @PathVariable String id) {
        return ResponseEntity.ok(addressService.getAddressById(id));
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get addresses by customer ID", description = "Returns all addresses for a specific customer")
    public ResponseEntity<List<AddressDto>> getAddressesByCustomerId(
            @Parameter(description = "Customer ID", required = true) @PathVariable String customerId) {
        return ResponseEntity.ok(addressService.getAddressesByCustomerId(customerId));
    }

    @GetMapping("/customer/{customerId}/default")
    @Operation(summary = "Get default address", description = "Returns the default address for a customer if set")
    public ResponseEntity<AddressDto> getDefaultAddress(
            @Parameter(description = "Customer ID", required = true) @PathVariable String customerId) {
        return ResponseEntity.ok(addressService.getDefaultAddress(customerId));
    }

    @GetMapping("/search")
    @Operation(summary = "Search addresses", description = "Search addresses by city, state, or zip code")
    public ResponseEntity<List<AddressDto>> searchAddresses(
            @Parameter(description = "City") @RequestParam(required = false) String city,
            @Parameter(description = "State") @RequestParam(required = false) String state,
            @Parameter(description = "Zip code") @RequestParam(required = false) String zipCode) {
        return ResponseEntity.ok(addressService.searchAddresses(city, state, zipCode));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update address", description = "Updates an existing address")
    public ResponseEntity<AddressDto> updateAddress(
            @Parameter(description = "Address ID", required = true) @PathVariable String id,
            @Valid @RequestBody AddressDto addressDto) {
        return ResponseEntity.ok(addressService.updateAddress(id, addressDto));
    }

    @PutMapping("/{addressId}/customer/{customerId}/default")
    @Operation(summary = "Set as default address", description = "Sets an address as the default for a customer")
    public ResponseEntity<AddressDto> setDefaultAddress(
            @Parameter(description = "Address ID", required = true) @PathVariable String addressId,
            @Parameter(description = "Customer ID", required = true) @PathVariable String customerId) {
        return ResponseEntity.ok(addressService.setDefaultAddress(addressId, customerId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete address", description = "Deletes an address by ID")
    public ResponseEntity<Void> deleteAddress(
            @Parameter(description = "Address ID", required = true) @PathVariable String id) {
        addressService.deleteAddress(id);
        return ResponseEntity.noContent().build();
    }
}