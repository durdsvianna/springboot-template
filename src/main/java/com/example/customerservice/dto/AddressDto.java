package com.example.customerservice.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Address data transfer object")
public class AddressDto {

    @Schema(description = "Address unique identifier", example = "64a1db45e7cb171cddc52805")
    private String id;
    
    @Schema(description = "Customer ID this address belongs to", example = "64a1db45e7cb171cddc52804")
    private String customerId;
    
    @NotBlank(message = "Street is required")
    @Size(min = 3, max = 100, message = "Street must be between 3 and 100 characters")
    @Schema(description = "Street address", example = "123 Main St", required = true)
    private String street;
    
    @NotBlank(message = "City is required")
    @Size(min = 2, max = 50, message = "City must be between 2 and 50 characters")
    @Schema(description = "City", example = "Anytown", required = true)
    private String city;
    
    @NotBlank(message = "State is required")
    @Size(min = 2, max = 50, message = "State must be between 2 and 50 characters")
    @Schema(description = "State/Province/Region", example = "NY", required = true)
    private String state;
    
    @NotBlank(message = "Zip code is required")
    @Size(min = 5, max = 10, message = "Zip code must be between 5 and 10 characters")
    @Schema(description = "Postal/Zip code", example = "12345", required = true)
    private String zipCode;
    
    @Schema(description = "Country", example = "USA")
    private String country;
    
    @Schema(description = "Whether this is the default address", example = "true", defaultValue = "false")
    private boolean isDefault;
    
    @Schema(description = "Record version for optimistic locking")
    private Long version;
    
    @Schema(description = "Date and time when the address was created")
    private LocalDateTime createdDate;
    
    @Schema(description = "Date and time when the address was last modified")
    private LocalDateTime lastModifiedDate;
}