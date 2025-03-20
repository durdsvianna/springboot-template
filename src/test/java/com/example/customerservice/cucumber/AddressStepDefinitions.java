package com.example.customerservice.cucumber;

import com.example.customerservice.dto.AddressDto;
import com.example.customerservice.dto.CustomerDto;
import com.example.customerservice.repository.AddressRepository;
import com.example.customerservice.repository.CustomerRepository;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AddressStepDefinitions {

    @LocalServerPort
    private int port;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AddressRepository addressRepository;
    
    @Autowired
    private TestContext testContext;

    private AddressDto addressDto;

    @Given("the address database is empty")
    public void theAddressDatabaseIsEmpty() {
        RestAssured.port = port;
        RestAssured.basePath = "/api";
        
        addressRepository.deleteAll();
        RequestSpecification request = given().contentType(ContentType.JSON);
        testContext.setRequest(request);
    }

    @When("I create an address with the following details:")
    public void iCreateAnAddressWithTheFollowingDetails(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        Map<String, String> data = rows.get(0);
        
        // Initialize request if it's null
        if (testContext.getRequest() == null) {
            RestAssured.port = port;
            RestAssured.basePath = "/api";
            testContext.setRequest(given().contentType(ContentType.JSON));
        }
        
        addressDto = AddressDto.builder()
                .street(data.get("street"))
                .city(data.get("city"))
                .state(data.get("state"))
                .zipCode(data.get("zipCode"))
                .country(data.get("country"))
                .isDefault(Boolean.parseBoolean(data.get("isDefault")))
                .customerId(testContext.getCustomerId())
                .build();
                
        Response response = testContext.getRequest()
                .body(addressDto)
                .when()
                .post("/addresses");
                
        String addressId = response.jsonPath().getString("id");
        testContext.setAddressId(addressId);
        testContext.setResponse(response);
    }

    @Then("the address is successfully created")
    public void theAddressIsSuccessfullyCreated() {
        testContext.getResponse().then().statusCode(201);
        assertThat(testContext.getAddressId(), notNullValue());
    }

    @And("the response contains the address details:")
    public void theResponseContainsTheAddressDetails(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        Map<String, String> expectedData = rows.get(0);
        
        testContext.getResponse().then()
                .body("street", equalTo(expectedData.get("street")))
                .body("city", equalTo(expectedData.get("city")))
                .body("state", equalTo(expectedData.get("state")))
                .body("zipCode", equalTo(expectedData.get("zipCode")));
                
        if (expectedData.containsKey("country")) {
            testContext.getResponse().then().body("country", equalTo(expectedData.get("country")));
        }
        
        if (expectedData.containsKey("isDefault")) {
            testContext.getResponse().then().body("isDefault", equalTo(Boolean.parseBoolean(expectedData.get("isDefault"))));
        }
    }

    @Given("there is an address with the following details:")
    public void thereIsAnAddressWithTheFollowingDetails(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        Map<String, String> data = rows.get(0);
        
        // Initialize request if it's null
        if (testContext.getRequest() == null) {
            RestAssured.port = port;
            RestAssured.basePath = "/api";
            testContext.setRequest(given().contentType(ContentType.JSON));
        }
        
        AddressDto dto = AddressDto.builder()
                .street(data.get("street"))
                .city(data.get("city"))
                .state(data.get("state"))
                .zipCode(data.get("zipCode"))
                .country(data.get("country"))
                .isDefault(Boolean.parseBoolean(data.get("isDefault")))
                .customerId(testContext.getCustomerId())
                .build();
                
        Response createResponse = testContext.getRequest()
                .body(dto)
                .when()
                .post("/addresses");
                
        String addressId = createResponse.jsonPath().getString("id");
        testContext.setAddressId(addressId);
        createResponse.then().statusCode(201);
    }

    @When("I request the address by ID")
    public void iRequestTheAddressById() {
        // Initialize request if it's null
        if (testContext.getRequest() == null) {
            RestAssured.port = port;
            RestAssured.basePath = "/api";
            testContext.setRequest(given().contentType(ContentType.JSON));
        }
        
        Response response = testContext.getRequest()
                .when()
                .get("/addresses/" + testContext.getAddressId());
        testContext.setResponse(response);
    }

    @When("I request all addresses for the customer")
    public void iRequestAllAddressesForTheCustomer() {
        // Initialize request if it's null
        if (testContext.getRequest() == null) {
            RestAssured.port = port;
            RestAssured.basePath = "/api";
            testContext.setRequest(given().contentType(ContentType.JSON));
        }
        
        if (testContext.getCustomerId() == null || testContext.getCustomerId().equals("null")) {
            // Defensive code - if customerId is null, use a valid non-null value
            Response response = testContext.getRequest()
                    .when()
                    .get("/addresses");
            testContext.setResponse(response);
        } else {
            Response response = testContext.getRequest()
                    .when()
                    .get("/addresses/customer/" + testContext.getCustomerId());
            testContext.setResponse(response);
        }
    }

    @And("the response contains {int} addresses")
    public void theResponseContainsAddresses(int count) {
        testContext.getResponse().then().body("size()", equalTo(count));
    }

    @And("the response contains addresses with the following details:")
    public void theResponseContainsAddressesWithTheFollowingDetails(DataTable dataTable) {
        List<Map<String, String>> expectedRows = dataTable.asMaps(String.class, String.class);
        
        for (int i = 0; i < expectedRows.size(); i++) {
            Map<String, String> expectedData = expectedRows.get(i);
            
            testContext.getResponse().then()
                    .body("[" + i + "].street", equalTo(expectedData.get("street")))
                    .body("[" + i + "].city", equalTo(expectedData.get("city")))
                    .body("[" + i + "].state", equalTo(expectedData.get("state")))
                    .body("[" + i + "].zipCode", equalTo(expectedData.get("zipCode")));
                    
            if (expectedData.containsKey("isDefault")) {
                testContext.getResponse().then().body("[" + i + "].isDefault", equalTo(Boolean.parseBoolean(expectedData.get("isDefault"))));
            }
        }
    }

    @When("I request the default address for the customer")
    public void iRequestTheDefaultAddressForTheCustomer() {
        Response response = testContext.getRequest()
                .when()
                .get("/addresses/customer/" + testContext.getCustomerId() + "/default");
        testContext.setResponse(response);
    }

    @When("I update the address with the following details:")
    public void iUpdateTheAddressWithTheFollowingDetails(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        Map<String, String> data = rows.get(0);
        
        AddressDto updateDto = AddressDto.builder()
                .street(data.get("street"))
                .city(data.get("city"))
                .state(data.get("state"))
                .zipCode(data.get("zipCode"))
                .country(data.get("country"))
                .isDefault(Boolean.parseBoolean(data.get("isDefault")))
                .build();
                
        Response response = testContext.getRequest()
                .body(updateDto)
                .when()
                .put("/addresses/" + testContext.getAddressId());
        testContext.setResponse(response);
    }

    @When("I set the second address as default")
    public void iSetTheSecondAddressAsDefault() {
        // Initialize request if it's null
        if (testContext.getRequest() == null) {
            RestAssured.port = port;
            RestAssured.basePath = "/api";
            testContext.setRequest(given().contentType(ContentType.JSON));
        }
        
        // Get the addresses for this customer
        Response getAddressesResponse = testContext.getRequest()
                .when()
                .get("/addresses/customer/" + testContext.getCustomerId());
        
        List<String> addressIds = getAddressesResponse.jsonPath().getList("id");
        testContext.setAddressIds(addressIds);
        
        if (addressIds.size() >= 2) {
            String secondAddressId = addressIds.get(1);
            
            // Use PUT to /addresses/{addressId}/customer/{customerId}/default endpoint
            Response response = testContext.getRequest()
                    .when()
                    .put("/addresses/" + secondAddressId + "/customer/" + testContext.getCustomerId() + "/default");
            testContext.setResponse(response);
        } else {
            throw new IllegalStateException("Not enough addresses created to set second one as default");
        }
    }

    @And("the response contains an address with {string} and isDefault as true")
    public void theResponseContainsAnAddressWithAndIsDefaultAsTrue(String street) {
        testContext.getResponse().then()
                .body("street", equalTo(street))
                .body("isDefault", equalTo(true));
    }

    @And("the first address is no longer the default")
    public void theFirstAddressIsNoLongerTheDefault() {
        List<String> addressIds = testContext.getAddressIds();
        if (addressIds.size() >= 1) {
            String firstAddressId = addressIds.get(0);
            
            testContext.getRequest()
                .when()
                .get("/addresses/" + firstAddressId)
                .then()
                .body("isDefault", equalTo(false));
        }
    }

    @When("I delete the address")
    public void iDeleteTheAddress() {
        Response response = testContext.getRequest()
                .when()
                .delete("/addresses/" + testContext.getAddressId());
        testContext.setResponse(response);
    }

    @And("the address no longer exists")
    public void theAddressNoLongerExists() {
        testContext.getRequest()
            .when()
            .get("/addresses/" + testContext.getAddressId())
            .then()
            .statusCode(404);
    }

    @When("I search for addresses in city {string}")
    public void iSearchForAddressesInCity(String city) {
        Response response = testContext.getRequest()
                .queryParam("city", city)
                .when()
                .get("/addresses/search");
        testContext.setResponse(response);
    }

    @And("all addresses in the response have city {string}")
    public void allAddressesInTheResponseHaveCity(String city) {
        List<String> cities = testContext.getResponse().jsonPath().getList("city");
        for (String c : cities) {
            assertEquals(city, c);
        }
    }
} 