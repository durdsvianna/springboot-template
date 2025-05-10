package com.example.customerservice.cucumber;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.customerservice.dto.CustomerDto;
import com.example.customerservice.model.Customer;
import com.example.customerservice.repository.AddressRepository;
import com.example.customerservice.repository.CustomerRepository;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class CustomerStepDefinitions {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AddressRepository addressRepository;

    private String baseUrl;
    private ResponseEntity<?> response;
    private CustomerDto customerDto;
    private String customerId;

    @Before
    public void setup() {
        baseUrl = "http://localhost:" + port;
        // Clear repositories before each scenario
        addressRepository.deleteAll();
        customerRepository.deleteAll();
    }

    @After
    public void cleanup() {
        // Clear repositories after each scenario
        addressRepository.deleteAll();
        customerRepository.deleteAll();
    }

    @Given("the following customer exists:")
    public void theFollowingCustomerExists(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        Map<String, String> customerData = rows.get(0);

        CustomerDto dto = CustomerDto.builder()
                .firstName(customerData.get("firstName"))
                .lastName(customerData.get("lastName"))
                .email(customerData.get("email"))
                .phone(customerData.get("phone"))
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CustomerDto> requestEntity = new HttpEntity<>(dto, headers);

        ResponseEntity<CustomerDto> response = restTemplate.exchange(
                baseUrl + "/customers",
                HttpMethod.POST,
                requestEntity,
                CustomerDto.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
        
        customerId = response.getBody().getId();
    }

    @Given("there are no customers in the system")
    public void thereAreNoCustomersInTheSystem() {
        customerRepository.deleteAll();
        addressRepository.deleteAll();
    }

    @When("I create a new customer with the following details:")
    public void iCreateANewCustomerWithTheFollowingDetails(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        Map<String, String> customerData = rows.get(0);

        customerDto = CustomerDto.builder()
                .firstName(customerData.get("firstName"))
                .lastName(customerData.get("lastName"))
                .email(customerData.get("email"))
                .phone(customerData.get("phone"))
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CustomerDto> requestEntity = new HttpEntity<>(customerDto, headers);

        response = restTemplate.exchange(
                baseUrl + "/customers",
                HttpMethod.POST,
                requestEntity,
                CustomerDto.class);
    }

    @When("I retrieve a customer with ID {string}")
    public void iRetrieveACustomerWithID(String id) {
        String url = id.equals("customerId") ? baseUrl + "/customers/" + customerId : baseUrl + "/customers/" + id;
        
        response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                CustomerDto.class);
    }

    @When("I retrieve a customer with email {string}")
    public void iRetrieveACustomerWithEmail(String email) {
        String url = baseUrl + "/customers/email/" + email;
        
        response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                CustomerDto.class);
    }

    @When("I update the customer with ID {string} with the following details:")
    public void iUpdateTheCustomerWithIDWithTheFollowingDetails(String id, DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        Map<String, String> customerData = rows.get(0);

        CustomerDto updateDto = CustomerDto.builder()
                .firstName(customerData.get("firstName"))
                .lastName(customerData.get("lastName"))
                .email(customerData.get("email"))
                .phone(customerData.get("phone"))
                .build();

        String url = id.equals("customerId") ? baseUrl + "/customers/" + customerId : baseUrl + "/customers/" + id;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CustomerDto> requestEntity = new HttpEntity<>(updateDto, headers);

        response = restTemplate.exchange(
                url,
                HttpMethod.PUT,
                requestEntity,
                CustomerDto.class);
    }

    @When("I delete the customer with ID {string}")
    public void iDeleteTheCustomerWithID(String id) {
        String url = id.equals("customerId") ? baseUrl + "/customers/" + customerId : baseUrl + "/customers/" + id;
        
        response = restTemplate.exchange(
                url,
                HttpMethod.DELETE,
                null,
                Void.class);
    }

    @When("I search for customers with first name {string}")
    public void iSearchForCustomersWithFirstName(String firstName) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/customers/search/firstname")
                .queryParam("firstName", firstName)
                .toUriString();
        
        response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                List.class);
    }

    @When("I search for customers with last name {string}")
    public void iSearchForCustomersWithLastName(String lastName) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/customers/search/lastname")
                .queryParam("lastName", lastName)
                .toUriString();
        
        response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                List.class);
    }

    @When("I retrieve all customers")
    public void iRetrieveAllCustomers() {
        response = restTemplate.exchange(
                baseUrl + "/customers",
                HttpMethod.GET,
                null,
                List.class);
    }

    @Then("the response status code should be {int}")
    public void theResponseStatusCodeShouldBe(Integer statusCode) {
        assertEquals(statusCode, response.getStatusCodeValue());
    }

    @Then("the customer should have the following details:")
    public void theCustomerShouldHaveTheFollowingDetails(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        Map<String, String> expectedData = rows.get(0);
        
        CustomerDto returnedCustomer = (CustomerDto) response.getBody();
        
        assertNotNull(returnedCustomer);
        assertEquals(expectedData.get("firstName"), returnedCustomer.getFirstName());
        assertEquals(expectedData.get("lastName"), returnedCustomer.getLastName());
        assertEquals(expectedData.get("email"), returnedCustomer.getEmail());
        assertEquals(expectedData.get("phone"), returnedCustomer.getPhone());
    }

    @Then("the customer should exist in the system")
    public void theCustomerShouldExistInTheSystem() {
        CustomerDto createdCustomer = (CustomerDto) response.getBody();
        assertNotNull(createdCustomer);
        assertNotNull(createdCustomer.getId());
        
        Optional<Customer> foundCustomer = customerRepository.findById(createdCustomer.getId());
        assertTrue(foundCustomer.isPresent());
    }

    @Then("the customer with ID {string} should not exist in the system")
    public void theCustomerWithIDShouldNotExistInTheSystem(String id) {
        String actualId = id.equals("customerId") ? customerId : id;
        Optional<Customer> customer = customerRepository.findById(actualId);
        assertFalse(customer.isPresent());
    }

    @Then("the response should contain {int} customers")
    public void theResponseShouldContainCustomers(Integer count) {
        List<?> customers = (List<?>) response.getBody();
        assertNotNull(customers);
        assertEquals(count, customers.size());
    }
}