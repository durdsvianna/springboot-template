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
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.customerservice.dto.AddressDto;
import com.example.customerservice.dto.CustomerDto;
import com.example.customerservice.model.Address;
import com.example.customerservice.model.Customer;
import com.example.customerservice.repository.AddressRepository;
import com.example.customerservice.repository.CustomerRepository;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class AddressStepDefinitions {

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
    private String customerId;
    private String addressId;
    private AddressDto addressDto;

    @Given("I have a base URL")
    public void iHaveABaseURL() {
        baseUrl = "http://localhost:" + port;
    }

    @Given("I have a customer with the following details:")
    public void iHaveACustomerWithTheFollowingDetails(DataTable dataTable) {
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

    @Given("the customer has the following address:")
    public void theCustomerHasTheFollowingAddress(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        Map<String, String> addressData = rows.get(0);

        AddressDto dto = AddressDto.builder()
                .street(addressData.get("street"))
                .city(addressData.get("city"))
                .state(addressData.get("state"))
                .postalCode(addressData.get("postalCode"))
                .country(addressData.get("country"))
                .isDefault(Boolean.parseBoolean(addressData.get("isDefault")))
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AddressDto> requestEntity = new HttpEntity<>(dto, headers);

        ResponseEntity<AddressDto> response = restTemplate.exchange(
                baseUrl + "/addresses/customer/" + customerId,
                HttpMethod.POST,
                requestEntity,
                AddressDto.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
        
        addressId = response.getBody().getId();
    }

    @When("I create a new address for the customer with the following details:")
    public void iCreateANewAddressForTheCustomerWithTheFollowingDetails(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        Map<String, String> addressData = rows.get(0);

        addressDto = AddressDto.builder()
                .street(addressData.get("street"))
                .city(addressData.get("city"))
                .state(addressData.get("state"))
                .postalCode(addressData.get("postalCode"))
                .country(addressData.get("country"))
                .isDefault(Boolean.parseBoolean(addressData.get("isDefault")))
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AddressDto> requestEntity = new HttpEntity<>(addressDto, headers);

        response = restTemplate.exchange(
                baseUrl + "/addresses/customer/" + customerId,
                HttpMethod.POST,
                requestEntity,
                AddressDto.class);
    }

    @When("I retrieve an address with ID {string}")
    public void iRetrieveAnAddressWithID(String id) {
        String url = id.equals("addressId") ? baseUrl + "/addresses/" + addressId : baseUrl + "/addresses/" + id;
        
        response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                AddressDto.class);
    }

    @When("I retrieve all addresses for the customer")
    public void iRetrieveAllAddressesForTheCustomer() {
        String url = baseUrl + "/addresses/customer/" + customerId;
        
        response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<AddressDto>>() {});
    }

    @When("I retrieve the default address for the customer")
    public void iRetrieveTheDefaultAddressForTheCustomer() {
        String url = baseUrl + "/addresses/customer/" + customerId + "/default";
        
        response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                AddressDto.class);
    }

    @When("I update the address with ID {string} with the following details:")
    public void iUpdateTheAddressWithIDWithTheFollowingDetails(String id, DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        Map<String, String> addressData = rows.get(0);

        AddressDto updateDto = AddressDto.builder()
                .street(addressData.get("street"))
                .city(addressData.get("city"))
                .state(addressData.get("state"))
                .postalCode(addressData.get("postalCode"))
                .country(addressData.get("country"))
                .isDefault(Boolean.parseBoolean(addressData.get("isDefault")))
                .build();

        String url = id.equals("addressId") ? baseUrl + "/addresses/" + addressId : baseUrl + "/addresses/" + id;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AddressDto> requestEntity = new HttpEntity<>(updateDto, headers);

        response = restTemplate.exchange(
                url,
                HttpMethod.PUT,
                requestEntity,
                AddressDto.class);
    }

    @When("I set the address with ID {string} as the default address")
    public void iSetTheAddressWithIDAsTheDefaultAddress(String id) {
        String url = id.equals("addressId") 
                ? baseUrl + "/addresses/" + addressId + "/customer/" + customerId + "/default" 
                : baseUrl + "/addresses/" + id + "/customer/" + customerId + "/default";
        
        response = restTemplate.exchange(
                url,
                HttpMethod.PUT,
                null,
                AddressDto.class);
    }

    @When("I delete the address with ID {string}")
    public void iDeleteTheAddressWithID(String id) {
        String url = id.equals("addressId") ? baseUrl + "/addresses/" + addressId : baseUrl + "/addresses/" + id;
        
        response = restTemplate.exchange(
                url,
                HttpMethod.DELETE,
                null,
                Void.class);
    }

    @When("I search for addresses in city {string} for the customer")
    public void iSearchForAddressesInCityForTheCustomer(String city) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/addresses/customer/" + customerId + "/search")
                .queryParam("city", city)
                .toUriString();
        
        response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<AddressDto>>() {});
    }

    @Then("the address should have the following details:")
    public void theAddressShouldHaveTheFollowingDetails(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        Map<String, String> expectedData = rows.get(0);
        
        AddressDto returnedAddress = (AddressDto) response.getBody();
        
        assertNotNull(returnedAddress);
        assertEquals(expectedData.get("street"), returnedAddress.getStreet());
        assertEquals(expectedData.get("city"), returnedAddress.getCity());
        assertEquals(expectedData.get("state"), returnedAddress.getState());
        assertEquals(expectedData.get("postalCode"), returnedAddress.getPostalCode());
        assertEquals(expectedData.get("country"), returnedAddress.getCountry());
        assertEquals(Boolean.parseBoolean(expectedData.get("isDefault")), returnedAddress.getIsDefault());
    }

    @Then("the address should exist in the system")
    public void theAddressShouldExistInTheSystem() {
        AddressDto createdAddress = (AddressDto) response.getBody();
        assertNotNull(createdAddress);
        assertNotNull(createdAddress.getId());
        
        Optional<Address> foundAddress = addressRepository.findById(createdAddress.getId());
        assertTrue(foundAddress.isPresent());
    }

    @Then("the address with ID {string} should not exist in the system")
    public void theAddressWithIDShouldNotExistInTheSystem(String id) {
        String actualId = id.equals("addressId") ? addressId : id;
        Optional<Address> address = addressRepository.findById(actualId);
        assertFalse(address.isPresent());
    }

    @Then("the response should contain {int} addresses")
    public void theResponseShouldContainAddresses(Integer count) {
        List<?> addresses = (List<?>) response.getBody();
        assertNotNull(addresses);
        assertEquals(count, addresses.size());
    }

    @Then("the address should belong to the customer")
    public void theAddressShouldBelongToTheCustomer() {
        AddressDto returnedAddress = (AddressDto) response.getBody();
        assertNotNull(returnedAddress);
        assertEquals(customerId, returnedAddress.getCustomerId());
    }

    @Then("the address should be marked as default")
    public void theAddressShouldBeMarkedAsDefault() {
        AddressDto returnedAddress = (AddressDto) response.getBody();
        assertNotNull(returnedAddress);
        assertTrue(returnedAddress.getIsDefault());
    }

    @Then("the address should not be marked as default")
    public void theAddressShouldNotBeMarkedAsDefault() {
        AddressDto returnedAddress = (AddressDto) response.getBody();
        assertNotNull(returnedAddress);
        assertFalse(returnedAddress.getIsDefault());
    }
}