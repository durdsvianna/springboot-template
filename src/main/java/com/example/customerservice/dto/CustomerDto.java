package com.example.customerservice.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
@Schema(description = "Customer data transfer object")
public class CustomerDto {

    @Schema(description = "Customer unique identifier", example = "64a1db45e7cb171cddc52804")
    private String id;
    
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    @Schema(description = "Customer's first name", example = "John", required = true)
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    @Schema(description = "Customer's last name", example = "Doe", required = true)
    private String lastName;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Schema(description = "Customer's email address", example = "john.doe@example.com", required = true)
    private String email;
    
    @Pattern(regexp = "^\\+?[0-9]{8,15}$", message = "Phone number must be valid")
    @Schema(description = "Customer's phone number", example = "+1234567890")
    private String phone;
    
    @Schema(description = "List of customer addresses")
    @Builder.Default
    private List<AddressDto> addresses = new ArrayList<>();
    
    @Schema(description = "Record version for optimistic locking")
    private Long version;
    
    @Schema(description = "Date and time when the customer was created")
    private LocalDateTime createdDate;
    
    @Schema(description = "Date and time when the customer was last modified")
    private LocalDateTime lastModifiedDate;
}