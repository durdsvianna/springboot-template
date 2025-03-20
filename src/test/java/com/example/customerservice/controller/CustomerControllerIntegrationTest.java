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
            .body("email", equalTo("jane.smith@example.com"))
            .body("phone", equalTo("+1 234 567 8901"));
    }
    
    @Test
    void shouldGetCustomerByEmail() {
        // Create customer first
        CustomerDto customerDto = CustomerDto.builder()
                .firstName("Bob")
                .lastName("Johnson")
                .email("bob.johnson@example.com")
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
            .pathParam("email", "bob.johnson@example.com")
        .when()
            .get("/customers/email/{email}")
        .then()
            .statusCode(200)
            .body("firstName", equalTo("Bob"))
            .body("lastName", equalTo("Johnson"))
            .body("email", equalTo("bob.johnson@example.com"))
            .body("phone", equalTo("+1 234 567 8902"));
    }
    
    @Test
    void shouldGetAllCustomers() {
        // Create multiple customers
        List<CustomerDto> customers = Arrays.asList(
            CustomerDto.builder()
                .firstName("User1")
                .lastName("Test")
                .email("user1.test@example.com")
                .phone("+1 111 111 1111")
                .build(),
            CustomerDto.builder()
                .firstName("User2")
                .lastName("Test")
                .email("user2.test@example.com")
                .phone("+1 222 222 2222")
                .build(),
            CustomerDto.builder()
                .firstName("User3")
                .lastName("Sample")
                .email("user3.sample@example.com")
                .phone("+1 333 333 3333")
                .build()
        );
        
        customers.forEach(customer -> {
            given()
                .contentType(ContentType.JSON)
                .body(customer)
            .when()
                .post("/customers")
            .then()
                .statusCode(201);
        });
        
        // Get all customers
        given()
        .when()
            .get("/customers")
        .then()
            .statusCode(200)
            .body("size()", equalTo(3))
            .body("findAll { it.firstName.startsWith('User') }.size()", equalTo(3));
    }
    
    @Test
    void shouldSearchCustomers() {
        // Create multiple customers for search testing
        List<CustomerDto> customers = Arrays.asList(
            CustomerDto.builder()
                .firstName("Search")
                .lastName("User1")
                .email("search.user1@example.com")
                .phone("+1 444 444 4444")
                .build(),
            CustomerDto.builder()
                .firstName("Search")
                .lastName("User2")
                .email("search.user2@example.com")
                .phone("+1 555 555 5555")
                .build(),
            CustomerDto.builder()
                .firstName("Other")
                .lastName("SearchLast")
                .email("other.searchlast@example.com")
                .phone("+1 666 666 6666")
                .build(),
            CustomerDto.builder()
                .firstName("Another")
                .lastName("Person")
                .email("another.person@example.com")
                .phone("+1 777 777 7777")
                .build()
        );
        
        customers.forEach(customer -> {
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
            .queryParam("firstName", "Search")
        .when()
            .get("/customers")
        .then()
            .statusCode(200)
            .body("size()", equalTo(2))
            .body("findAll { it.firstName == 'Search' }.size()", equalTo(2));
            
        // Search by last name
        given()
            .queryParam("lastName", "SearchLast")
        .when()
            .get("/customers")
        .then()
            .statusCode(200)
            .body("size()", equalTo(1))
            .body("findAll { it.lastName == 'SearchLast' }.size()", equalTo(1));
            
        // Search with no results
        given()
            .queryParam("firstName", "NonExistent")
        .when()
            .get("/customers")
        .then()
            .statusCode(200)
            .body("size()", equalTo(0));
    }
    
    @Test
    void shouldUpdateCustomer() {
        // Create customer first
        CustomerDto customerDto = CustomerDto.builder()
                .firstName("Original")
                .lastName("Customer")
                .email("original.customer@example.com")
                .phone("+1 888 888 8888")
                .build();

        String customerId = given()
                .contentType(ContentType.JSON)
                .body(customerDto)
            .when()
                .post("/customers")
            .then()
                .statusCode(201)
                .extract().path("id");
                
        // Now update the customer
        CustomerDto updateDto = CustomerDto.builder()
                .firstName("Updated")
                .lastName("CustomerNew")
                .email("updated.customer@example.com")
                .phone("+1 999 999 9999")
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
            .body("lastName", equalTo("CustomerNew"))
            .body("email", equalTo("updated.customer@example.com"))
            .body("phone", equalTo("+1 999 999 9999"));
            
        // Verify update worked - get the customer
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
            .pathParam("id", "nonexistentid")
        .when()
            .get("/customers/{id}")
        .then()
            .statusCode(404);
    }
    
    @Test
    void shouldDeleteCustomer() {
        // Create customer with address
        CustomerDto customerDto = CustomerDto.builder()
                .firstName("Delete")
                .lastName("Test")
                .email("delete.test@example.com")
                .phone("+1 000 000 0000")
                .build();

        String customerId = given()
                .contentType(ContentType.JSON)
                .body(customerDto)
            .when()
                .post("/customers")
            .then()
                .statusCode(201)
                .extract().path("id");
                
        // Add an address for this customer
        AddressDto addressDto = AddressDto.builder()
                .street("123 Delete St")
                .city("DeleteCity")
                .state("NY")
                .zipCode("12345")
                .country("USA")
                .isDefault(true)
                .customerId(customerId)
                .build();
                
        String addressId = given()
                .contentType(ContentType.JSON)
                .body(addressDto)
            .when()
                .post("/addresses")
            .then()
                .statusCode(201)
                .extract().path("id");
                
        // Verify address exists
        given()
            .pathParam("customerId", customerId)
        .when()
            .get("/addresses/customer/{customerId}")
        .then()
            .statusCode(200)
            .body("size()", equalTo(1));
                
        // Now delete the customer
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
            
        // Verify address was also deleted
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
                .firstName("Dup")
                .lastName("Test1")
                .email("duplicate@example.com")
                .phone("+1 111 222 3333")
                .build();
                
        given()
            .contentType(ContentType.JSON)
            .body(customer1)
        .when()
            .post("/customers")
        .then()
            .statusCode(201);
            
        // Try to create customer with same email
        CustomerDto customer2 = CustomerDto.builder()
                .firstName("Dup")
                .lastName("Test2")
                .email("duplicate@example.com")
                .phone("+1 444 555 6666")
                .build();
                
        given()
            .contentType(ContentType.JSON)
            .body(customer2)
        .when()
            .post("/customers")
        .then()
            .statusCode(400);
    }
}