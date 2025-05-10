package com.example.customerservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFoundException extends RuntimeException {
    
    public NotFoundException(String message) {
        super(message);
    }
    
    public NotFoundException(String entityName, String id) {
        super(String.format("%s with id '%s' not found", entityName, id));
    }
    
    public NotFoundException(String entityName, String fieldName, String value) {
        super(String.format("%s with %s '%s' not found", entityName, fieldName, value));
    }
}