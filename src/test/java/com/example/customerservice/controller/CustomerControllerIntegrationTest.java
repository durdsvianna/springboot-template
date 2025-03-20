package com.example.customerservice.controller;

import com.example.customerservice.config.MongoTestContainer;
import com.example.customerservice.dto.AddressDto;
import com.example.customerservice.dto.CustomerDto;
import com.example.customerservice.repository.AddressRepository;
import com.example.customerservice.repository.CustomerRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(MongoTestContainer.class)
@ActiveProfiles("test")
class CustomerControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AddressRepository addressRepository;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/api";
        
        // Clean up database before each test
        addressRepository.deleteAll();
        customerRepository.deleteAll();
    }

    @Test
    void shouldCreateCustomer() {
        CustomerDto customerDto = CustomerDto.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("+1 234 567 8900")
                .build();

        given()
            .contentType(ContentType.JSON)
            .body(customerDto)
        .when()
            .post("/customers")
        .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("firstName", equalTo("John"))
            .body("lastName", equalTo("Doe"))
            .body("email", equalTo("john.doe@example.com"))
            .body("phone", equalTo("+1 234 567 8900"));
    }

    @Test
    void shouldCreateCustomerWithAddresses() {
        // Create customer with addresses
        List<AddressDto> addresses = new ArrayList<>();
        addresses.add(AddressDto.builder()
                .street("123 Main St")
                .city("Anytown")
                .state("NY")
                .zipCode("12345")
                .country("USA")
                .isDefault(true)
                .build());
        
        addresses.add(AddressDto.builder()
                .street("456 Oak Ave")
                .city("Othertown")
                .state("CA")
                .zipCode("67890")
                .country("USA")
                .isDefault(false)
                .build());
                
        CustomerDto customerDto = CustomerDto.builder()
                .firstName("Alice")
                .lastName("Smith")
                .email("alice.smith@example.com")
                .phone("+1 234 567 8901")
                .addresses(addresses)
                .build();

        String customerId = given()
            .contentType(ContentType.JSON)
            .body(customerDto)
        .when()
            .post("/customers")
        .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("firstName", equalTo("Alice"))
            .extract().path("id");
            
        // Verify addresses were created and associated
        given()
            .pathParam("customerId", customerId)
        .when()
            .get("/addresses/customer/{customerId}")
        .then()
            .statusCode(200)
            .body("size()", equalTo(2))
            .body("findAll { it.street == '123 Main St' }.size()", equalTo(1))
            .body("findAll { it.street == '456 Oak Ave' }.size()", equalTo(1));
    }

    @Test
    void shouldGetCustomerById() {
        // Create customer first
        CustomerDto customerDto = CustomerDto.builder()
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.smith@example.com")
                .phone("+1 234 567 8901")
                .build();

        String customerId = given()
                .contentType(ContentType.JSON)
                .body(customerDto)
            .when()
                .post("/customers")
            .then()
                .statusCode(201)
                .extract().path("id");

        // Then get by ID
        given()
            .pathParam("id", customerId)
        .when()
            .get("/customers/{id}")
        .then()
            .statusCode(200)
            .body("id", equalTo(customerId))
            .body("firstName", equalTo("Jane"))
            .body("lastName", equalTo("Smith"))
            .body("email", equalTo("jane.smith@example.com"));
    }

    @Test
    void shouldGetCustomerByEmail() {
        // Create customer first
        CustomerDto customerDto = CustomerDto.builder()
                .firstName("Email")
                .lastName("Test")
                .email("email.test@example.com")
                .phone("+1 234 567 8902")
                .build();

        given()
            .contentType(ContentType.JSON)
            .body(customerDto)
        .when()
            .post("/customers")
        .then()
            .statusCode(201);

        // Then get by email
        given()
            .pathParam("email", "email.test@example.com")
        .when()
            .get("/customers/email/{email}")
        .then()
            .statusCode(200)
            .body("firstName", equalTo("Email"))
            .body("lastName", equalTo("Test"))
            .body("email", equalTo("email.test@example.com"));
    }

    @Test
    void shouldGetAllCustomers() {
        // Create multiple customers
        List<String> emails = Arrays.asList(
                "customer1@example.com",
                "customer2@example.com",
                "customer3@example.com"
        );
        
        for (int i = 0; i < emails.size(); i++) {
            CustomerDto customerDto = CustomerDto.builder()
                    .firstName("First" + (i + 1))
                    .lastName("Last" + (i + 1))
                    .email(emails.get(i))
                    .phone("+1 234 567 890" + i)
                    .build();
                    
            given()
                .contentType(ContentType.JSON)
                .body(customerDto)
            .when()
                .post("/customers")
            .then()
                .statusCode(201);
        }
        
        // Get all customers and verify
        given()
        .when()
            .get("/customers")
        .then()
            .statusCode(200)
            .body("size()", greaterThanOrEqualTo(3))
            .body("findAll { it.email in ['customer1@example.com', 'customer2@example.com', 'customer3@example.com'] }.size()", equalTo(3));
    }

    @Test
    void shouldSearchCustomers() {
        // Create customers with specific names
        CustomerDto customer1 = CustomerDto.builder()
                .firstName("John")
                .lastName("Smith")
                .email("john.smith@example.com")
                .phone("+1 234 567 8903")
                .build();
                
        CustomerDto customer2 = CustomerDto.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("+1 234 567 8904")
                .build();
                
        CustomerDto customer3 = CustomerDto.builder()
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.smith@example.com")
                .phone("+1 234 567 8905")
                .build();
        
        // Create all customers
        Arrays.asList(customer1, customer2, customer3).forEach(customer -> {
            given()
                .contentType(ContentType.JSON)
                .body(customer)
            .when()
                .post("/customers")
            .then()
                .statusCode(201);
        });
        
        // Search by first name
        given()
            .queryParam("firstName", "John")
        .when()
            .get("/customers")
        .then()
            .statusCode(200)
            .body("size()", equalTo(2))
            .body("findAll { it.firstName == 'John' }.size()", equalTo(2));
            
        // Search by last name
        given()
            .queryParam("lastName", "Smith")
        .when()
            .get("/customers")
        .then()
            .statusCode(200)
            .body("size()", equalTo(2))
            .body("findAll { it.lastName == 'Smith' }.size()", equalTo(2));
            
        // Search by both first and last name
        given()
            .queryParam("firstName", "John")
            .queryParam("lastName", "Smith")
        .when()
            .get("/customers")
        .then()
            .statusCode(200)
            .body("size()", equalTo(1))
            .body("[0].firstName", equalTo("John"))
            .body("[0].lastName", equalTo("Smith"));
    }

    @Test
    void shouldUpdateCustomer() {
        // Create customer
        CustomerDto customerDto = CustomerDto.builder()
                .firstName("Original")
                .lastName("Customer")
                .email("original@example.com")
                .phone("+1 234 567 8906")
                .build();
                
        String customerId = given()
            .contentType(ContentType.JSON)
            .body(customerDto)
        .when()
            .post("/customers")
        .then()
            .statusCode(201)
            .extract().path("id");
            
        // Update customer
        CustomerDto updateDto = CustomerDto.builder()
                .firstName("Updated")
                .lastName("CustomerInfo")
                .email("updated@example.com")
                .phone("+1 234 567 8907")
                .build();
                
        given()
            .contentType(ContentType.JSON)
            .body(updateDto)
            .pathParam("id", customerId)
        .when()
            .put("/customers/{id}")
        .then()
            .statusCode(200)
            .body("id", equalTo(customerId))
            .body("firstName", equalTo("Updated"))
            .body("lastName", equalTo("CustomerInfo"))
            .body("email", equalTo("updated@example.com"))
            .body("phone", equalTo("+1 234 567 8907"));
            
        // Verify update persisted
        given()
            .pathParam("id", customerId)
        .when()
            .get("/customers/{id}")
        .then()
            .statusCode(200)
            .body("firstName", equalTo("Updated"));
    }

    @Test
    void shouldReturn404WhenCustomerNotFound() {
        given()
            .pathParam("id", "nonexistent-id")
        .when()
            .get("/customers/{id}")
        .then()
            .statusCode(404);
    }

    @Test
    void shouldDeleteCustomer() {
        // Create customer
        CustomerDto customerDto = CustomerDto.builder()
                .firstName("ToDelete")
                .lastName("Customer")
                .email("todelete@example.com")
                .phone("+1 234 567 8908")
                .build();
                
        String customerId = given()
            .contentType(ContentType.JSON)
            .body(customerDto)
        .when()
            .post("/customers")
        .then()
            .statusCode(201)
            .extract().path("id");
            
        // Create address for customer
        AddressDto addressDto = AddressDto.builder()
                .street("Delete Street")
                .city("Delete City")
                .state("DC")
                .zipCode("12345")
                .customerId(customerId)
                .build();
                
        given()
            .contentType(ContentType.JSON)
            .body(addressDto)
        .when()
            .post("/addresses")
        .then()
            .statusCode(201);
            
        // Delete customer
        given()
            .pathParam("id", customerId)
        .when()
            .delete("/customers/{id}")
        .then()
            .statusCode(204);
            
        // Verify customer is gone
        given()
            .pathParam("id", customerId)
        .when()
            .get("/customers/{id}")
        .then()
            .statusCode(404);
            
        // Verify customer's addresses are gone
        given()
            .pathParam("customerId", customerId)
        .when()
            .get("/addresses/customer/{customerId}")
        .then()
            .statusCode(200)
            .body("size()", equalTo(0));
    }

    @Test
    void shouldNotAllowDuplicateEmails() {
        // Create first customer
        CustomerDto customer1 = CustomerDto.builder()
                .firstName("Original")
                .lastName("Customer")
                .email("duplicate@example.com")
                .phone("+1 234 567 8909")
                .build();
                
        given()
            .contentType(ContentType.JSON)
            .body(customer1)
        .when()
            .post("/customers")
        .then()
            .statusCode(201);
            
        // Try to create second customer with same email
        CustomerDto customer2 = CustomerDto.builder()
                .firstName("Duplicate")
                .lastName("Customer")
                .email("duplicate@example.com")
                .phone("+1 234 567 8910")
                .build();
                
        given()
            .contentType(ContentType.JSON)
            .body(customer2)
        .when()
            .post("/customers")
        .then()
            .statusCode(400)
            .body("message", containsString("already exists"));
    }
} 