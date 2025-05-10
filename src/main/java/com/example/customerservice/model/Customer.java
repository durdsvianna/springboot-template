package com.example.customerservice.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "customers")
public class Customer {

    @Id
    private String id;
    
    private String firstName;
    
    private String lastName;
    
    @Indexed(unique = true)
    private String email;
    
    private String phone;
    
    @DBRef
    @Builder.Default
    private List<Address> addresses = new ArrayList<>();
    
    @Version
    private Long version;
    
    @Builder.Default
    private LocalDateTime createdDate = LocalDateTime.now();
    
    @LastModifiedDate
    private LocalDateTime lastModifiedDate;
    
    /**
     * Add an address to the customer
     * 
     * @param address The address to add
     * @return true if the address was added, false otherwise
     */
    public boolean addAddress(Address address) {
        if (addresses == null) {
            addresses = new ArrayList<>();
        }
        return addresses.add(address);
    }
    
    /**
     * Remove an address from the customer
     * 
     * @param address The address to remove
     * @return true if the address was removed, false otherwise
     */
    public boolean removeAddress(Address address) {
        if (addresses == null) {
            return false;
        }
        return addresses.remove(address);
    }
    
    /**
     * Get the default address for the customer
     * 
     * @return The default address, or null if none is marked as default
     */
    public Address getDefaultAddress() {
        if (addresses == null || addresses.isEmpty()) {
            return null;
        }
        return addresses.stream()
                .filter(Address::isDefault)
                .findFirst()
                .orElse(null);
    }
}