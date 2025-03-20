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

import com.example.customerservice.dto.CustomerDto;
import com.example.customerservice.repository.AddressRepository;
import com.example.customerservice.repository.CustomerRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class CustomerControllerIntegrationTest {

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

    @BeforeEach
    void setUp() {
        // Clean repositories before each test
        addressRepository.deleteAll();
        customerRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        // Clean repositories after each test
        addressRepository.deleteAll();
        customerRepository.deleteAll();
    }

    @Test
    void createCustomer_ValidInput_ReturnsCreated() throws Exception {
        // Arrange
        CustomerDto customerDto = CustomerDto.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("+1234567890")
                .build();

        // Act & Assert
        mockMvc.perform(post("/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customerDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.firstName", is("John")))
                .andExpect(jsonPath("$.lastName", is("Doe")))
                .andExpect(jsonPath("$.email", is("john.doe@example.com")))
                .andExpect(jsonPath("$.phone", is("+1234567890")))
                .andExpect(jsonPath("$.createdDate", notNullValue()));
    }

    @Test
    void createCustomer_DuplicateEmail_ReturnsConflict() throws Exception {
        // Arrange
        CustomerDto customerDto = CustomerDto.builder()
                .firstName("John")
                .lastName("Doe")
                .email("duplicate@example.com")
                .phone("+1234567890")
                .build();

        // Create the first customer
        mockMvc.perform(post("/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customerDto)))
                .andExpect(status().isCreated());

        // Try to create a customer with the same email
        mockMvc.perform(post("/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customerDto)))
                .andExpect(status().isConflict());
    }

    @Test
    void getCustomerById_ExistingId_ReturnsCustomer() throws Exception {
        // Arrange
        CustomerDto customerDto = CustomerDto.builder()
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.smith@example.com")
                .phone("+1987654321")
                .build();

        // Create a customer
        String createResponse = mockMvc.perform(post("/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customerDto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        CustomerDto createdCustomer = objectMapper.readValue(createResponse, CustomerDto.class);

        // Act & Assert
        mockMvc.perform(get("/customers/{id}", createdCustomer.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(createdCustomer.getId())))
                .andExpect(jsonPath("$.firstName", is("Jane")))
                .andExpect(jsonPath("$.lastName", is("Smith")))
                .andExpect(jsonPath("$.email", is("jane.smith@example.com")));
    }

    @Test
    void getCustomerById_NonExistingId_ReturnsNotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/customers/{id}", "non-existing-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCustomerByEmail_ExistingEmail_ReturnsCustomer() throws Exception {
        // Arrange
        CustomerDto customerDto = CustomerDto.builder()
                .firstName("Bob")
                .lastName("Johnson")
                .email("bob.johnson@example.com")
                .phone("+1122334455")
                .build();

        // Create a customer
        mockMvc.perform(post("/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customerDto)))
                .andExpect(status().isCreated());

        // Act & Assert
        mockMvc.perform(get("/customers/email/{email}", "bob.johnson@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is("Bob")))
                .andExpect(jsonPath("$.lastName", is("Johnson")))
                .andExpect(jsonPath("$.email", is("bob.johnson@example.com")));
    }

    @Test
    void updateCustomer_ExistingId_ReturnsUpdatedCustomer() throws Exception {
        // Arrange
        CustomerDto customerDto = CustomerDto.builder()
                .firstName("Original")
                .lastName("Customer")
                .email("original@example.com")
                .phone("+1555555555")
                .build();

        // Create a customer
        String createResponse = mockMvc.perform(post("/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customerDto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        CustomerDto createdCustomer = objectMapper.readValue(createResponse, CustomerDto.class);

        // Prepare update data
        CustomerDto updateDto = CustomerDto.builder()
                .firstName("Updated")
                .lastName("CustomerNew")
                .email("updated@example.com")
                .phone("+1666666666")
                .build();

        // Act & Assert
        mockMvc.perform(put("/customers/{id}", createdCustomer.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(createdCustomer.getId())))
                .andExpect(jsonPath("$.firstName", is("Updated")))
                .andExpect(jsonPath("$.lastName", is("CustomerNew")))
                .andExpect(jsonPath("$.email", is("updated@example.com")))
                .andExpect(jsonPath("$.phone", is("+1666666666")));
    }

    @Test
    void deleteCustomer_ExistingId_ReturnsNoContent() throws Exception {
        // Arrange
        CustomerDto customerDto = CustomerDto.builder()
                .firstName("ToDelete")
                .lastName("User")
                .email("todelete@example.com")
                .phone("+1777777777")
                .build();

        // Create a customer
        String createResponse = mockMvc.perform(post("/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customerDto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        CustomerDto createdCustomer = objectMapper.readValue(createResponse, CustomerDto.class);

        // Act & Assert
        mockMvc.perform(delete("/customers/{id}", createdCustomer.getId()))
                .andExpect(status().isNoContent());

        // Verify the customer no longer exists
        mockMvc.perform(get("/customers/{id}", createdCustomer.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllCustomers_ReturnsAllCustomers() throws Exception {
        // Arrange
        CustomerDto customer1 = CustomerDto.builder()
                .firstName("First")
                .lastName("Customer")
                .email("first@example.com")
                .build();

        CustomerDto customer2 = CustomerDto.builder()
                .firstName("Second")
                .lastName("Customer")
                .email("second@example.com")
                .build();

        // Create two customers
        mockMvc.perform(post("/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customer1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customer2)))
                .andExpect(status().isCreated());

        // Act & Assert
        mockMvc.perform(get("/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].email", notNullValue()))
                .andExpect(jsonPath("$[1].email", notNullValue()));
    }

    @Test
    void searchByFirstName_ExistingFirstName_ReturnsMatchingCustomers() throws Exception {
        // Arrange
        CustomerDto customer1 = CustomerDto.builder()
                .firstName("Search")
                .lastName("User1")
                .email("search.user1@example.com")
                .build();

        CustomerDto customer2 = CustomerDto.builder()
                .firstName("Search")
                .lastName("User2")
                .email("search.user2@example.com")
                .build();

        CustomerDto customer3 = CustomerDto.builder()
                .firstName("Other")
                .lastName("User")
                .email("other.user@example.com")
                .build();

        // Create three customers
        mockMvc.perform(post("/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customer1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customer2)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customer3)))
                .andExpect(status().isCreated());

        // Act & Assert
        mockMvc.perform(get("/customers/search/firstname")
                .param("firstName", "Search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].firstName", equalTo("Search")))
                .andExpect(jsonPath("$[1].firstName", equalTo("Search")));
    }

    @Test
    void searchByLastName_ExistingLastName_ReturnsMatchingCustomers() throws Exception {
        // Arrange
        CustomerDto customer1 = CustomerDto.builder()
                .firstName("User1")
                .lastName("SearchLast")
                .email("user1.search@example.com")
                .build();

        CustomerDto customer2 = CustomerDto.builder()
                .firstName("User2")
                .lastName("SearchLast")
                .email("user2.search@example.com")
                .build();

        CustomerDto customer3 = CustomerDto.builder()
                .firstName("User3")
                .lastName("Other")
                .email("user3.other@example.com")
                .build();

        // Create three customers
        mockMvc.perform(post("/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customer1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customer2)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customer3)))
                .andExpect(status().isCreated());

        // Act & Assert
        mockMvc.perform(get("/customers/search/lastname")
                .param("lastName", "SearchLast"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].lastName", equalTo("SearchLast")))
                .andExpect(jsonPath("$[1].lastName", equalTo("SearchLast")));
    }
}