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
            .body("findAll { it.city.startsWith('Test City') }.size()", equalTo(3));
    }

    @Test
    void shouldGetDefaultAddress() {
        // Given - Create multiple addresses with one default
        AddressDto defaultAddress = AddressDto.builder()
                .street("Default St")
                .city("Default City")
                .state("DC")
                .zipCode("12345")
                .country("USA")
                .isDefault(true)
                .customerId(customerId)
                .build();

        given()
            .contentType(ContentType.JSON)
            .body(defaultAddress)
        .when()
            .post("/addresses")
        .then()
            .statusCode(201);

        AddressDto nonDefaultAddress = AddressDto.builder()
                .street("Regular St")
                .city("Regular City")
                .state("RC")
                .zipCode("67890")
                .country("USA")
                .isDefault(false)
                .customerId(customerId)
                .build();

        given()
            .contentType(ContentType.JSON)
            .body(nonDefaultAddress)
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
            .body("street", equalTo("Default St"))
            .body("city", equalTo("Default City"))
            .body("isDefault", equalTo(true));
    }

    @Test
    void shouldSearchAddressesByCity() {
        // Given - Create addresses in different cities
        AddressDto address1 = AddressDto.builder()
                .street("1 Search St")
                .city("SearchCity")
                .state("SC")
                .zipCode("11111")
                .country("USA")
                .isDefault(true)
                .customerId(customerId)
                .build();

        given()
            .contentType(ContentType.JSON)
            .body(address1)
        .when()
            .post("/addresses")
        .then()
            .statusCode(201);

        AddressDto address2 = AddressDto.builder()
                .street("2 Search Ave")
                .city("SearchCity")
                .state("SC")
                .zipCode("22222")
                .country("USA")
                .isDefault(false)
                .customerId(customerId)
                .build();

        given()
            .contentType(ContentType.JSON)
            .body(address2)
        .when()
            .post("/addresses")
        .then()
            .statusCode(201);

        AddressDto address3 = AddressDto.builder()
                .street("3 Other Blvd")
                .city("OtherCity")
                .state("OC")
                .zipCode("33333")
                .country("USA")
                .isDefault(false)
                .customerId(customerId)
                .build();

        given()
            .contentType(ContentType.JSON)
            .body(address3)
        .when()
            .post("/addresses")
        .then()
            .statusCode(201);

        // When & Then - Search by city
        given()
            .queryParam("city", "SearchCity")
        .when()
            .get("/addresses/search")
        .then()
            .statusCode(200)
            .body("size()", equalTo(2))
            .body("findAll { it.city == 'SearchCity' }.size()", equalTo(2));
    }

    @Test
    void shouldUpdateAddress() {
        // Given - Create an address first
        AddressDto addressDto = AddressDto.builder()
                .street("Original St")
                .city("Original City")
                .state("OG")
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

        // When - Update the address
        AddressDto updateDto = AddressDto.builder()
                .street("Updated St")
                .city("Updated City")
                .state("UP")
                .zipCode("54321")
                .country("USA")
                .isDefault(true)
                .build();

        given()
            .contentType(ContentType.JSON)
            .body(updateDto)
            .pathParam("id", addressId)
        .when()
            .put("/addresses/{id}")
        .then()
            .statusCode(200)
            .body("street", equalTo("Updated St"))
            .body("city", equalTo("Updated City"))
            .body("state", equalTo("UP"))
            .body("zipCode", equalTo("54321"))
            .body("isDefault", equalTo(true));

        // Then - Verify update persisted
        given()
            .pathParam("id", addressId)
        .when()
            .get("/addresses/{id}")
        .then()
            .statusCode(200)
            .body("street", equalTo("Updated St"));
    }

    @Test
    void shouldSetAddressAsDefault() {
        // Given - Create two addresses
        AddressDto address1 = AddressDto.builder()
                .street("First St")
                .city("First City")
                .state("FC")
                .zipCode("11111")
                .country("USA")
                .isDefault(true)
                .customerId(customerId)
                .build();

        String address1Id = given()
                .contentType(ContentType.JSON)
                .body(address1)
            .when()
                .post("/addresses")
            .then()
                .statusCode(201)
                .extract().path("id");

        AddressDto address2 = AddressDto.builder()
                .street("Second St")
                .city("Second City")
                .state("SC")
                .zipCode("22222")
                .country("USA")
                .isDefault(false)
                .customerId(customerId)
                .build();

        String address2Id = given()
                .contentType(ContentType.JSON)
                .body(address2)
            .when()
                .post("/addresses")
            .then()
                .statusCode(201)
                .extract().path("id");

        // When - Set the second address as default
        given()
            .pathParam("addressId", address2Id)
            .pathParam("customerId", customerId)
        .when()
            .put("/addresses/{addressId}/customer/{customerId}/default")
        .then()
            .statusCode(200)
            .body("id", equalTo(address2Id))
            .body("street", equalTo("Second St"))
            .body("isDefault", equalTo(true));

        // Then - Verify first address is no longer default
        given()
            .pathParam("id", address1Id)
        .when()
            .get("/addresses/{id}")
        .then()
            .statusCode(200)
            .body("isDefault", equalTo(false));
    }

    @Test
    void shouldDeleteAddress() {
        // Given - Create an address
        AddressDto addressDto = AddressDto.builder()
                .street("Delete St")
                .city("Delete City")
                .state("DC")
                .zipCode("99999")
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

        // When - Delete the address
        given()
            .pathParam("id", addressId)
        .when()
            .delete("/addresses/{id}")
        .then()
            .statusCode(204);

        // Then - Verify address is gone
        given()
            .pathParam("id", addressId)
        .when()
            .get("/addresses/{id}")
        .then()
            .statusCode(404);
    }
}