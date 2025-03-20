package com.example.customerservice.cucumber;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TestContext {
    private String customerId;
    private String addressId;
    private List<String> addressIds = new ArrayList<>();
    private Response response;
    private RequestSpecification request;

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getAddressId() {
        return addressId;
    }

    public void setAddressId(String addressId) {
        this.addressId = addressId;
        if (addressId != null) {
            addressIds.add(addressId);
        }
    }

    public List<String> getAddressIds() {
        return addressIds;
    }

    public void setAddressIds(List<String> addressIds) {
        this.addressIds = addressIds;
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    public RequestSpecification getRequest() {
        return request;
    }

    public void setRequest(RequestSpecification request) {
        this.request = request;
    }
} 