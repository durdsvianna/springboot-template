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

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(MongoTestContainer.class)
@ActiveProfiles("test")
class AddressControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AddressRepository addressRepository;

    private String customerId;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/api";
        
        // Clean up database before each test
        addressRepository.deleteAll();
        customerRepository.deleteAll();
        
        // Create a test customer
        CustomerDto customerDto = CustomerDto.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe.address.test@example.com")
                .phone("+1 234 567 8900")
                .build();

        customerId = given()
                .contentType(ContentType.JSON)
                .body(customerDto)
            .when()
                .post("/customers")
            .then()
                .statusCode(201)
                .extract().path("id");
    }

    @Test
    void shouldCreateAddress() {
        // Given
        AddressDto addressDto = AddressDto.builder()
                .street("123 Main St")
                .city("Anytown")
                .state("NY")
                .zipCode("12345")
                .country("USA")
                .isDefault(true)
                .customerId(customerId)
                .build();

        // When & Then
        given()
            .contentType(ContentType.JSON)
            .body(addressDto)
        .when()
            .post("/addresses")
        .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("street", equalTo("123 Main St"))
            .body("city", equalTo("Anytown"))
            .body("state", equalTo("NY"))
            .body("zipCode", equalTo("12345"))
            .body("isDefault", equalTo(true))
            .body("customerId", equalTo(customerId));
    }

    @Test
    void shouldGetAddressById() {
        // Given
        AddressDto addressDto = AddressDto.builder()
                .street("456 Oak Dr")
                .city("Othertown")
                .state("CA")
                .zipCode("67890")
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

        // When & Then
        given()
            .pathParam("id", addressId)
        .when()
            .get("/addresses/{id}")
        .then()
            .statusCode(200)
            .body("id", equalTo(addressId))
            .body("street", equalTo("456 Oak Dr"))
            .body("city", equalTo("Othertown"))
            .body("state", equalTo("CA"))
            .body("zipCode", equalTo("67890"))
            .body("isDefault", equalTo(true));
    }

    @Test
    void shouldGetAddressesByCustomerId() {
        // Given - Create multiple addresses for the customer
        for (int i = 1; i <= 3; i++) {
            AddressDto addressDto = AddressDto.builder()
                    .street(i + " Test St")
                    .city("Test City " + i)
                    .state("TS")
                    .zipCode("1234" + i)
                    .country("USA")
                    .isDefault(i == 1) // Make first address the default
                    .customerId(customerId)
                    .build();

            given()
                .contentType(ContentType.JSON)
                .body(addressDto)
            .when()
                .post("/addresses")
            .then()
                .statusCode(201);
        }

        // When & Then
        given()
            .pathParam("customerId", customerId)
        .when()
            .get("/addresses/customer/{customerId}")
        .then()
            .statusCode(200)
            .body("size()", equalTo(3))
            .body("findAll { it.customerId == '" + customerId + "' }.size()", equalTo(3));
    }

    @Test
    void shouldGetDefaultAddress() {
        // Given
        AddressDto defaultAddressDto = AddressDto.builder()
                .street("Default Street")
                .city("Default City")
                .state("DS")
                .zipCode("12345")
                .country("USA")
                .isDefault(true)
                .customerId(customerId)
                .build();

        given()
            .contentType(ContentType.JSON)
            .body(defaultAddressDto)
        .when()
            .post("/addresses")
        .then()
            .statusCode(201);

        // Add a non-default address too
        AddressDto nonDefaultAddressDto = AddressDto.builder()
                .street("Non-Default Street")
                .city("Non-Default City")
                .state("ND")
                .zipCode("67890")
                .country("USA")
                .isDefault(false)
                .customerId(customerId)
                .build();

        given()
            .contentType(ContentType.JSON)
            .body(nonDefaultAddressDto)
        .when()
            .post("/addresses")
        .then()
            .statusCode(201);

        // When & Then
        given()
            .pathParam("customerId", customerId)
        .when()
            .get("/addresses/customer/{customerId}/default")
        .then()
            .statusCode(200)
            .body("street", equalTo("Default Street"))
            .body("city", equalTo("Default City"))
            .body("state", equalTo("DS"))
            .body("isDefault", equalTo(true));
    }

    @Test
    void shouldSearchAddressesByCity() {
        // Given
        AddressDto addressDto1 = AddressDto.builder()
                .street("123 Main St")
                .city("SearchCity")
                .state("NY")
                .zipCode("12345")
                .customerId(customerId)
                .build();

        AddressDto addressDto2 = AddressDto.builder()
                .street("456 Oak St")
                .city("OtherCity")
                .state("NY")
                .zipCode("67890")
                .customerId(customerId)
                .build();

        given()
            .contentType(ContentType.JSON)
            .body(addressDto1)
        .when()
            .post("/addresses")
        .then()
            .statusCode(201);

        given()
            .contentType(ContentType.JSON)
            .body(addressDto2)
        .when()
            .post("/addresses")
        .then()
            .statusCode(201);

        // When & Then
        given()
            .queryParam("city", "SearchCity")
        .when()
            .get("/addresses/search")
        .then()
            .statusCode(200)
            .body("size()", equalTo(1))
            .body("[0].city", equalTo("SearchCity"));
    }

    @Test
    void shouldUpdateAddress() {
        // Given
        AddressDto addressDto = AddressDto.builder()
                .street("Original Street")
                .city("Original City")
                .state("OS")
                .zipCode("12345")
                .country("USA")
                .isDefault(false)
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

        AddressDto updateDto = AddressDto.builder()
                .street("Updated Street")
                .city("Updated City")
                .state("US")
                .zipCode("54321")
                .country("USA")
                .isDefault(true)
                .build();

        // When & Then
        given()
            .contentType(ContentType.JSON)
            .body(updateDto)
            .pathParam("id", addressId)
        .when()
            .put("/addresses/{id}")
        .then()
            .statusCode(200)
            .body("id", equalTo(addressId))
            .body("street", equalTo("Updated Street"))
            .body("city", equalTo("Updated City"))
            .body("state", equalTo("US"))
            .body("zipCode", equalTo("54321"))
            .body("isDefault", equalTo(true));
    }

    @Test
    void shouldSetAddressAsDefault() {
        // Given - Create two addresses
        AddressDto defaultAddressDto = AddressDto.builder()
                .street("Default Street")
                .city("Default City")
                .state("DS")
                .zipCode("12345")
                .country("USA")
                .isDefault(true)
                .customerId(customerId)
                .build();

        given()
            .contentType(ContentType.JSON)
            .body(defaultAddressDto)
        .when()
            .post("/addresses")
        .then()
            .statusCode(201);

        AddressDto nonDefaultAddressDto = AddressDto.builder()
                .street("Non-Default Street")
                .city("Non-Default City")
                .state("ND")
                .zipCode("67890")
                .country("USA")
                .isDefault(false)
                .customerId(customerId)
                .build();

        String nonDefaultAddressId = given()
            .contentType(ContentType.JSON)
            .body(nonDefaultAddressDto)
        .when()
            .post("/addresses")
        .then()
            .statusCode(201)
            .extract().path("id");

        // When - Set the non-default address as default
        given()
            .pathParam("addressId", nonDefaultAddressId)
            .pathParam("customerId", customerId)
        .when()
            .put("/addresses/{addressId}/customer/{customerId}/default")
        .then()
            .statusCode(200)
            .body("id", equalTo(nonDefaultAddressId))
            .body("street", equalTo("Non-Default Street"))
            .body("isDefault", equalTo(true));

        // Then - Verify the original default is no longer default
        given()
            .pathParam("customerId", customerId)
        .when()
            .get("/addresses/customer/{customerId}/default")
        .then()
            .statusCode(200)
            .body("street", equalTo("Non-Default Street"));
    }

    @Test
    void shouldDeleteAddress() {
        // Given
        AddressDto addressDto = AddressDto.builder()
                .street("To Be Deleted")
                .city("Delete City")
                .state("DC")
                .zipCode("12345")
                .country("USA")
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

        // When & Then
        given()
            .pathParam("id", addressId)
        .when()
            .delete("/addresses/{id}")
        .then()
            .statusCode(204);

        // Verify it's gone
        given()
            .pathParam("id", addressId)
        .when()
            .get("/addresses/{id}")
        .then()
            .statusCode(404);
    }
} 