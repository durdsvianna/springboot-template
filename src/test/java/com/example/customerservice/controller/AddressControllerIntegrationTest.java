package com.example.customerservice.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.example.customerservice.dto.AddressDto;
import com.example.customerservice.dto.CustomerDto;
import com.example.customerservice.model.Customer;
import com.example.customerservice.repository.AddressRepository;
import com.example.customerservice.repository.CustomerRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class AddressControllerIntegrationTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:5.0.15"));

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AddressRepository addressRepository;

    private String customerId;

    @BeforeEach
    void setUp() throws Exception {
        // Clean repositories
        addressRepository.deleteAll();
        customerRepository.deleteAll();

        // Create a customer to use in the tests
        CustomerDto customerDto = CustomerDto.builder()
                .firstName("Test")
                .lastName("Customer")
                .email("test.customer@example.com")
                .phone("+1234567890")
                .build();

        String createResponse = mockMvc.perform(post("/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customerDto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        CustomerDto createdCustomer = objectMapper.readValue(createResponse, CustomerDto.class);
        customerId = createdCustomer.getId();
    }

    @AfterEach
    void tearDown() {
        // Clean repositories
        addressRepository.deleteAll();
        customerRepository.deleteAll();
    }

    @Test
    void createAddress_ValidInput_ReturnsCreated() throws Exception {
        // Arrange
        AddressDto addressDto = AddressDto.builder()
                .street("123 Main St")
                .city("New York")
                .state("NY")
                .postalCode("10001")
                .country("USA")
                .isDefault(true)
                .build();

        // Act & Assert
        mockMvc.perform(post("/addresses/customer/{customerId}", customerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addressDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.street", is("123 Main St")))
                .andExpect(jsonPath("$.city", is("New York")))
                .andExpect(jsonPath("$.state", is("NY")))
                .andExpect(jsonPath("$.postalCode", is("10001")))
                .andExpect(jsonPath("$.country", is("USA")))
                .andExpect(jsonPath("$.isDefault", is(true)))
                .andExpect(jsonPath("$.customerId", is(customerId)));
    }

    @Test
    void createAddress_NonExistingCustomer_ReturnsNotFound() throws Exception {
        // Arrange
        AddressDto addressDto = AddressDto.builder()
                .street("123 Main St")
                .city("New York")
                .state("NY")
                .postalCode("10001")
                .country("USA")
                .isDefault(true)
                .build();

        // Act & Assert
        mockMvc.perform(post("/addresses/customer/{customerId}", "non-existing-id")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addressDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAddressById_ExistingId_ReturnsAddress() throws Exception {
        // Arrange
        AddressDto addressDto = AddressDto.builder()
                .street("456 Oak Ave")
                .city("Los Angeles")
                .state("CA")
                .postalCode("90001")
                .country("USA")
                .isDefault(true)
                .build();

        // Create an address
        String createResponse = mockMvc.perform(post("/addresses/customer/{customerId}", customerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addressDto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        AddressDto createdAddress = objectMapper.readValue(createResponse, AddressDto.class);

        // Act & Assert
        mockMvc.perform(get("/addresses/{id}", createdAddress.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(createdAddress.getId())))
                .andExpect(jsonPath("$.street", is("456 Oak Ave")))
                .andExpect(jsonPath("$.city", is("Los Angeles")))
                .andExpect(jsonPath("$.state", is("CA")))
                .andExpect(jsonPath("$.postalCode", is("90001")))
                .andExpect(jsonPath("$.country", is("USA")))
                .andExpect(jsonPath("$.isDefault", is(true)))
                .andExpect(jsonPath("$.customerId", is(customerId)));
    }

    @Test
    void getAddressById_NonExistingId_ReturnsNotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/addresses/{id}", "non-existing-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAddressesByCustomerId_ExistingCustomerId_ReturnsAddresses() throws Exception {
        // Arrange
        AddressDto address1 = AddressDto.builder()
                .street("789 Pine St")
                .city("Chicago")
                .state("IL")
                .postalCode("60601")
                .country("USA")
                .isDefault(true)
                .build();

        AddressDto address2 = AddressDto.builder()
                .street("101 Maple Ave")
                .city("Chicago")
                .state("IL")
                .postalCode("60602")
                .country("USA")
                .isDefault(false)
                .build();

        // Create two addresses
        mockMvc.perform(post("/addresses/customer/{customerId}", customerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(address1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/addresses/customer/{customerId}", customerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(address2)))
                .andExpect(status().isCreated());

        // Act & Assert
        mockMvc.perform(get("/addresses/customer/{customerId}", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].customerId", is(customerId)))
                .andExpect(jsonPath("$[1].customerId", is(customerId)));
    }

    @Test
    void getAddressesByCustomerId_NonExistingCustomerId_ReturnsEmptyList() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/addresses/customer/{customerId}", "non-existing-id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getDefaultAddress_ExistingCustomerId_ReturnsDefaultAddress() throws Exception {
        // Arrange
        AddressDto address1 = AddressDto.builder()
                .street("123 Default St")
                .city("Default City")
                .state("DC")
                .postalCode("12345")
                .country("USA")
                .isDefault(true)
                .build();

        AddressDto address2 = AddressDto.builder()
                .street("456 Non-Default St")
                .city("Other City")
                .state("OC")
                .postalCode("67890")
                .country("USA")
                .isDefault(false)
                .build();

        // Create two addresses, one default and one non-default
        mockMvc.perform(post("/addresses/customer/{customerId}", customerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(address1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/addresses/customer/{customerId}", customerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(address2)))
                .andExpect(status().isCreated());

        // Act & Assert
        mockMvc.perform(get("/addresses/customer/{customerId}/default", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.street", is("123 Default St")))
                .andExpect(jsonPath("$.city", is("Default City")))
                .andExpect(jsonPath("$.isDefault", is(true)));
    }

    @Test
    void getDefaultAddress_NoDefaultAddress_ReturnsNotFound() throws Exception {
        // Arrange
        AddressDto address = AddressDto.builder()
                .street("789 Non-Default St")
                .city("Another City")
                .state("AC")
                .postalCode("54321")
                .country("USA")
                .isDefault(false)
                .build();

        // Create a non-default address
        mockMvc.perform(post("/addresses/customer/{customerId}", customerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(address)))
                .andExpect(status().isCreated());

        // Act & Assert
        mockMvc.perform(get("/addresses/customer/{customerId}/default", customerId))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateAddress_ExistingId_ReturnsUpdatedAddress() throws Exception {
        // Arrange
        AddressDto addressDto = AddressDto.builder()
                .street("Original Street")
                .city("Original City")
                .state("OC")
                .postalCode("12345")
                .country("USA")
                .isDefault(true)
                .build();

        // Create an address
        String createResponse = mockMvc.perform(post("/addresses/customer/{customerId}", customerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addressDto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        AddressDto createdAddress = objectMapper.readValue(createResponse, AddressDto.class);

        // Prepare update data
        AddressDto updateDto = AddressDto.builder()
                .street("Updated Street")
                .city("Updated City")
                .state("UC")
                .postalCode("54321")
                .country("Canada")
                .isDefault(true)
                .build();

        // Act & Assert
        mockMvc.perform(put("/addresses/{id}", createdAddress.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(createdAddress.getId())))
                .andExpect(jsonPath("$.street", is("Updated Street")))
                .andExpect(jsonPath("$.city", is("Updated City")))
                .andExpect(jsonPath("$.state", is("UC")))
                .andExpect(jsonPath("$.postalCode", is("54321")))
                .andExpect(jsonPath("$.country", is("Canada")))
                .andExpect(jsonPath("$.isDefault", is(true)))
                .andExpect(jsonPath("$.customerId", is(customerId)));
    }

    @Test
    void setDefaultAddress_ExistingId_ReturnsUpdatedAddress() throws Exception {
        // Arrange
        AddressDto address1 = AddressDto.builder()
                .street("First Street")
                .city("First City")
                .state("FC")
                .postalCode("12345")
                .country("USA")
                .isDefault(true)
                .build();

        AddressDto address2 = AddressDto.builder()
                .street("Second Street")
                .city("Second City")
                .state("SC")
                .postalCode("67890")
                .country("USA")
                .isDefault(false)
                .build();

        // Create two addresses
        mockMvc.perform(post("/addresses/customer/{customerId}", customerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(address1)))
                .andExpect(status().isCreated());

        String response2 = mockMvc.perform(post("/addresses/customer/{customerId}", customerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(address2)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        AddressDto createdAddress2 = objectMapper.readValue(response2, AddressDto.class);

        // Act & Assert - Set the second address as default
        mockMvc.perform(put("/addresses/{addressId}/customer/{customerId}/default", createdAddress2.getId(), customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.street", is("Second Street")))
                .andExpect(jsonPath("$.city", is("Second City")))
                .andExpect(jsonPath("$.isDefault", is(true)));

        // Verify the first address is no longer default
        mockMvc.perform(get("/addresses/customer/{customerId}/default", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.street", is("Second Street")));
    }

    @Test
    void deleteAddress_ExistingId_ReturnsNoContent() throws Exception {
        // Arrange
        AddressDto addressDto = AddressDto.builder()
                .street("To Delete Street")
                .city("Delete City")
                .state("DC")
                .postalCode("99999")
                .country("USA")
                .isDefault(true)
                .build();

        // Create an address
        String createResponse = mockMvc.perform(post("/addresses/customer/{customerId}", customerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addressDto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        AddressDto createdAddress = objectMapper.readValue(createResponse, AddressDto.class);

        // Act & Assert
        mockMvc.perform(delete("/addresses/{id}", createdAddress.getId()))
                .andExpect(status().isNoContent());

        // Verify the address no longer exists
        mockMvc.perform(get("/addresses/{id}", createdAddress.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void searchAddressesByCity_ExistingCityForCustomer_ReturnsMatchingAddresses() throws Exception {
        // Arrange
        AddressDto address1 = AddressDto.builder()
                .street("123 Search St")
                .city("SearchCity")
                .state("SC")
                .postalCode("12345")
                .country("USA")
                .isDefault(true)
                .build();

        AddressDto address2 = AddressDto.builder()
                .street("456 Search Ave")
                .city("SearchCity")
                .state("SC")
                .postalCode("67890")
                .country("USA")
                .isDefault(false)
                .build();

        AddressDto address3 = AddressDto.builder()
                .street("789 Other Rd")
                .city("OtherCity")
                .state("OC")
                .postalCode("54321")
                .country("USA")
                .isDefault(false)
                .build();

        // Create three addresses
        mockMvc.perform(post("/addresses/customer/{customerId}", customerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(address1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/addresses/customer/{customerId}", customerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(address2)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/addresses/customer/{customerId}", customerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(address3)))
                .andExpect(status().isCreated());

        // Act & Assert
        mockMvc.perform(get("/addresses/customer/{customerId}/search", customerId)
                .param("city", "SearchCity"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].city", equalTo("SearchCity")))
                .andExpect(jsonPath("$[1].city", equalTo("SearchCity")));
    }
}