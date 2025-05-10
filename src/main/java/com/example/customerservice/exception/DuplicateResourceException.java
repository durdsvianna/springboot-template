package com.example.customerservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateResourceException extends RuntimeException {
    
    public DuplicateResourceException(String message) {
        super(message);
    }
    
    public DuplicateResourceException(String entityName, String fieldName, String value) {
        super(String.format("%s with %s '%s' already exists", entityName, fieldName, value));
    }
}