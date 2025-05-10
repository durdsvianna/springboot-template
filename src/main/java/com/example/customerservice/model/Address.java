package com.example.customerservice.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "addresses")
@CompoundIndexes({
    @CompoundIndex(name = "customer_city_idx", def = "{'customerId': 1, 'city': 1}")
})
public class Address {

    @Id
    private String id;
    
    private String customerId;
    
    private String street;
    
    private String city;
    
    private String state;
    
    private String zipCode;
    
    private String country;
    
    @Builder.Default
    private boolean isDefault = false;
    
    @Version
    private Long version;
    
    @Builder.Default
    private LocalDateTime createdDate = LocalDateTime.now();
    
    @LastModifiedDate
    private LocalDateTime lastModifiedDate;
}