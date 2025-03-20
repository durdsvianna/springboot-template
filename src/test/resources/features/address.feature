Feature: Address Management
  As a user of the Customer Service API
  I want to be able to manage customer addresses
  So that I can keep address information up-to-date

  Background:
    Given the address database is empty
    And there is a customer with the following details:
      | firstName | lastName | email                 | phone         |
      | Address   | Test     | address.test@example.com | +1111222333 |

  Scenario: Create a new address for a customer
    When I create an address with the following details:
      | street      | city    | state | zipCode | country | isDefault |
      | 123 Main St | Anytown | NY    | 12345   | USA     | true      |
    Then the address is successfully created
    And the response contains the address details:
      | street      | city    | state | zipCode | country | isDefault |
      | 123 Main St | Anytown | NY    | 12345   | USA     | true      |

  Scenario: Get an address by ID
    Given there is an address with the following details:
      | street      | city    | state | zipCode | country | isDefault |
      | 456 Oak Ave | Othertown | CA  | 67890   | USA     | true      |
    When I request the address by ID
    Then the response status is 200
    And the response contains the address details:
      | street      | city    | state | zipCode | country | isDefault |
      | 456 Oak Ave | Othertown | CA  | 67890   | USA     | true      |

  Scenario: Get addresses by customer ID
    Given the customer has the following addresses:
      | street          | city      | state | zipCode | isDefault |
      | 123 Customer St | CustCity1 | NY    | 12345   | true      |
      | 456 Customer Ave| CustCity2 | CA    | 67890   | false     |
    When I request all addresses for the customer
    Then the response status is 200
    And the response contains 2 addresses
    And the response contains addresses with the following details:
      | street          | city      | state | zipCode | isDefault |
      | 123 Customer St | CustCity1 | NY    | 12345   | true      |
      | 456 Customer Ave| CustCity2 | CA    | 67890   | false     |

  Scenario: Get default address for a customer
    Given the customer has the following addresses:
      | street            | city       | state | zipCode | isDefault |
      | 123 Default St    | DefaultCity| NY    | 12345   | true      |
      | 456 NonDefault Ave| OtherCity  | CA    | 67890   | false     |
    When I request the default address for the customer
    Then the response status is 200
    And the response contains the address details:
      | street            | city       | state | zipCode | isDefault |
      | 123 Default St    | DefaultCity| NY    | 12345   | true      |

  Scenario: Update an address
    Given there is an address with the following details:
      | street           | city       | state | zipCode | country | isDefault |
      | 123 Original St  | OrigCity   | NY    | 12345   | USA     | false     |
    When I update the address with the following details:
      | street           | city       | state | zipCode | country | isDefault |
      | 123 Updated St   | UpdatedCity| TX    | 54321   | USA     | true      |
    Then the response status is 200
    And the response contains the address details:
      | street           | city       | state | zipCode | country | isDefault |
      | 123 Updated St   | UpdatedCity| TX    | 54321   | USA     | true      |

  Scenario: Set an address as default
    Given the customer has the following addresses:
      | street            | city        | state | zipCode | isDefault |
      | 123 First St      | FirstCity   | NY    | 12345   | true      |
      | 456 Second Ave    | SecondCity  | CA    | 67890   | false     |
    When I set the second address as default
    Then the response status is 200
    And the response contains an address with "456 Second Ave" and isDefault as true
    And the first address is no longer the default

  Scenario: Delete an address
    Given there is an address with the following details:
      | street           | city       | state | zipCode | country | isDefault |
      | 123 Delete St    | DeleteCity | NY    | 12345   | USA     | false     |
    When I delete the address
    Then the response status is 204
    And the address no longer exists

  Scenario: Search addresses by city
    Given the customer has the following addresses:
      | street            | city        | state | zipCode | isDefault |
      | 123 Search St     | SearchCity  | NY    | 12345   | true      |
      | 456 Search Ave    | SearchCity  | CA    | 67890   | false     |
      | 789 Other Blvd    | OtherCity   | TX    | 54321   | false     |
    When I search for addresses in city "SearchCity"
    Then the response status is 200
    And the response contains 2 addresses
    And all addresses in the response have city "SearchCity" 