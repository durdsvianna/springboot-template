Feature: Address Management
  As a user of the Customer Service API
  I want to be able to manage customer addresses
  So that I can keep address information up-to-date

  Background:
    Given I have a base URL
    And I have a customer with the following details:
      | firstName | lastName | email                  | phone       |
      | Address   | Test     | address.test@example.com | +1111222333 |

  Scenario: Create a new address for a customer
    When I create a new address for the customer with the following details:
      | street      | city    | state | postalCode | country | isDefault |
      | 123 Main St | Anytown | NY    | 12345      | USA     | true      |
    Then the response status code should be 201
    And the address should exist in the system
    And the address should have the following details:
      | street      | city    | state | postalCode | country | isDefault |
      | 123 Main St | Anytown | NY    | 12345      | USA     | true      |
    And the address should belong to the customer

  Scenario: Get an address by ID
    Given the customer has the following address:
      | street      | city      | state | postalCode | country | isDefault |
      | 456 Oak Ave | Othertown | CA    | 67890      | USA     | true      |
    When I retrieve an address with ID "addressId"
    Then the response status code should be 200
    And the address should have the following details:
      | street      | city      | state | postalCode | country | isDefault |
      | 456 Oak Ave | Othertown | CA    | 67890      | USA     | true      |

  Scenario: Get addresses by customer ID
    Given the customer has the following address:
      | street          | city      | state | postalCode | country | isDefault |
      | 123 Customer St | CustCity1 | NY    | 12345      | USA     | true      |
    And the customer has the following address:
      | street           | city      | state | postalCode | country | isDefault |
      | 456 Customer Ave | CustCity2 | CA    | 67890      | USA     | false     |
    When I retrieve all addresses for the customer
    Then the response status code should be 200
    And the response should contain 2 addresses

  Scenario: Get default address for a customer
    Given the customer has the following address:
      | street            | city        | state | postalCode | country | isDefault |
      | 123 Default St    | DefaultCity | NY    | 12345      | USA     | true      |
    And the customer has the following address:
      | street             | city      | state | postalCode | country | isDefault |
      | 456 NonDefault Ave | OtherCity | CA    | 67890      | USA     | false     |
    When I retrieve the default address for the customer
    Then the response status code should be 200
    And the address should have the following details:
      | street            | city        | state | postalCode | country | isDefault |
      | 123 Default St    | DefaultCity | NY    | 12345      | USA     | true      |
    And the address should be marked as default

  Scenario: Update an address
    Given the customer has the following address:
      | street           | city      | state | postalCode | country | isDefault |
      | 123 Original St  | OrigCity  | NY    | 12345      | USA     | false     |
    When I update the address with ID "addressId" with the following details:
      | street           | city         | state | postalCode | country | isDefault |
      | 123 Updated St   | UpdatedCity  | TX    | 54321      | USA     | true      |
    Then the response status code should be 200
    And the address should have the following details:
      | street           | city         | state | postalCode | country | isDefault |
      | 123 Updated St   | UpdatedCity  | TX    | 54321      | USA     | true      |

  Scenario: Set an address as default
    Given the customer has the following address:
      | street        | city      | state | postalCode | country | isDefault |
      | 123 First St  | FirstCity | NY    | 12345      | USA     | true      |
    And the customer has the following address:
      | street         | city       | state | postalCode | country | isDefault |
      | 456 Second Ave | SecondCity | CA    | 67890      | USA     | false     |
    When I set the address with ID "addressId" as the default address
    Then the response status code should be 200
    And the address should be marked as default

  Scenario: Delete an address
    Given the customer has the following address:
      | street        | city       | state | postalCode | country | isDefault |
      | 123 Delete St | DeleteCity | NY    | 12345      | USA     | false     |
    When I delete the address with ID "addressId"
    Then the response status code should be 204
    And the address with ID "addressId" should not exist in the system

  Scenario: Search addresses by city
    Given the customer has the following address:
      | street         | city       | state | postalCode | country | isDefault |
      | 123 Search St  | SearchCity | NY    | 12345      | USA     | true      |
    And the customer has the following address:
      | street         | city       | state | postalCode | country | isDefault |
      | 456 Search Ave | SearchCity | CA    | 67890      | USA     | false     |
    And the customer has the following address:
      | street         | city      | state | postalCode | country | isDefault |
      | 789 Other Blvd | OtherCity | TX    | 54321      | USA     | false     |
    When I search for addresses in city "SearchCity" for the customer
    Then the response status code should be 200
    And the response should contain 2 addresses