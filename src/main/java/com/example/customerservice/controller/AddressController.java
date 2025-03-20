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

import com.example.customerservice.dto.AddressDto;
import com.example.customerservice.service.AddressService;

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
@RequestMapping("/addresses")
@RequiredArgsConstructor
@Tag(name = "Address", description = "Address management endpoints")
public class AddressController {

    private final AddressService addressService;

    @Operation(summary = "Create a new address for a customer")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Address created", 
                content = @Content(schema = @Schema(implementation = AddressDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @PostMapping("/customer/{customerId}")
    public ResponseEntity<AddressDto> createAddress(
            @Parameter(description = "Customer ID") @PathVariable String customerId,
            @Valid @RequestBody AddressDto addressDto) {
        
        AddressDto createdAddress = addressService.createAddress(customerId, addressDto);
        
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .replacePath("/addresses/{id}")
                .buildAndExpand(createdAddress.getId())
                .toUri();
        
        return ResponseEntity.created(location).body(createdAddress);
    }

    @Operation(summary = "Get an address by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Found the address", 
                content = @Content(schema = @Schema(implementation = AddressDto.class))),
        @ApiResponse(responseCode = "404", description = "Address not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<AddressDto> getAddressById(
            @Parameter(description = "Address ID") @PathVariable String id) {
        
        AddressDto address = addressService.getAddressById(id);
        return ResponseEntity.ok(address);
    }

    @Operation(summary = "Get all addresses for a customer")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of addresses returned"),
        @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<AddressDto>> getAddressesByCustomerId(
            @Parameter(description = "Customer ID") @PathVariable String customerId) {
        
        List<AddressDto> addresses = addressService.getAddressesByCustomerId(customerId);
        return ResponseEntity.ok(addresses);
    }

    @Operation(summary = "Get the default address for a customer")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Found the default address", 
                content = @Content(schema = @Schema(implementation = AddressDto.class))),
        @ApiResponse(responseCode = "404", description = "Customer not found or no default address")
    })
    @GetMapping("/customer/{customerId}/default")
    public ResponseEntity<AddressDto> getDefaultAddress(
            @Parameter(description = "Customer ID") @PathVariable String customerId) {
        
        AddressDto address = addressService.getDefaultAddress(customerId);
        return ResponseEntity.ok(address);
    }

    @Operation(summary = "Update an address")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Address updated", 
                content = @Content(schema = @Schema(implementation = AddressDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "404", description = "Address not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<AddressDto> updateAddress(
            @Parameter(description = "Address ID") @PathVariable String id,
            @Valid @RequestBody AddressDto addressDto) {
        
        AddressDto updatedAddress = addressService.updateAddress(id, addressDto);
        return ResponseEntity.ok(updatedAddress);
    }

    @Operation(summary = "Set an address as the default")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Address set as default", 
                content = @Content(schema = @Schema(implementation = AddressDto.class))),
        @ApiResponse(responseCode = "404", description = "Address or customer not found")
    })
    @PutMapping("/{addressId}/customer/{customerId}/default")
    public ResponseEntity<AddressDto> setAddressAsDefault(
            @Parameter(description = "Address ID") @PathVariable String addressId,
            @Parameter(description = "Customer ID") @PathVariable String customerId) {
        
        AddressDto updatedAddress = addressService.setAddressAsDefault(addressId, customerId);
        return ResponseEntity.ok(updatedAddress);
    }

    @Operation(summary = "Delete an address")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Address deleted"),
        @ApiResponse(responseCode = "404", description = "Address not found")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAddress(
            @Parameter(description = "Address ID") @PathVariable String id) {
        
        addressService.deleteAddress(id);
    }

    @Operation(summary = "Search addresses by city")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of matching addresses returned"),
        @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @GetMapping("/customer/{customerId}/search")
    public ResponseEntity<List<AddressDto>> findAddressesByCity(
            @Parameter(description = "Customer ID") @PathVariable String customerId,
            @Parameter(description = "City to search for") @RequestParam String city) {
        
        List<AddressDto> addresses = addressService.findAddressesByCity(customerId, city);
        return ResponseEntity.ok(addresses);
    }
}