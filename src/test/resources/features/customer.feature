Feature: Customer Management
  As a user of the Customer Service API
  I want to be able to manage customers
  So that I can keep customer information up-to-date

  Background:
    Given the customer database is empty

  Scenario: Create a new customer
    When I create a customer with the following details:
      | firstName | lastName | email                | phone         |
      | John      | Doe      | john.doe@example.com | +1234567890   |
    Then the customer is successfully created
    And the response contains the customer details:
      | firstName | lastName | email                | phone         |
      | John      | Doe      | john.doe@example.com | +1234567890   |

  Scenario: Get a customer by ID
    Given there is a customer with the following details:
      | firstName | lastName | email                   | phone         |
      | Jane      | Smith    | jane.smith@example.com  | +1987654321   |
    When I request the customer by ID
    Then the response status is 200
    And the response contains the customer details:
      | firstName | lastName | email                   | phone         |
      | Jane      | Smith    | jane.smith@example.com  | +1987654321   |

  Scenario: Get a customer by email
    Given there is a customer with the following details:
      | firstName | lastName | email                  | phone         |
      | Bob       | Johnson  | bob.johnson@example.com| +1122334455   |
    When I request the customer by email "bob.johnson@example.com"
    Then the response status is 200
    And the response contains the customer details:
      | firstName | lastName | email                  | phone         |
      | Bob       | Johnson  | bob.johnson@example.com| +1122334455   |

  Scenario: Update a customer
    Given there is a customer with the following details:
      | firstName | lastName | email                   | phone         |
      | Original  | Customer | original@example.com    | +1555555555   |
    When I update the customer with the following details:
      | firstName | lastName    | email               | phone         |
      | Updated   | CustomerNew | updated@example.com | +1666666666   |
    Then the response status is 200
    And the response contains the customer details:
      | firstName | lastName    | email               | phone         |
      | Updated   | CustomerNew | updated@example.com | +1666666666   |

  Scenario: Delete a customer
    Given there is a customer with the following details:
      | firstName | lastName | email                   | phone         |
      | ToDelete  | User     | todelete@example.com    | +1777777777   |
    When I delete the customer
    Then the response status is 204
    And the customer no longer exists

  Scenario: Search customers by first name
    Given the following customers exist:
      | firstName | lastName | email                   | phone         |
      | Search    | User1    | search.user1@example.com| +1111111111   |
      | Search    | User2    | search.user2@example.com| +1222222222   |
      | Other     | User     | other.user@example.com  | +1333333333   |
    When I search for customers with first name "Search"
    Then the response status is 200
    And the response contains 2 customers
    And all customers in the response have first name "Search"

  Scenario: Search customers by last name
    Given the following customers exist:
      | firstName | lastName | email                   | phone         |
      | User1     | SearchLast| user1.search@example.com| +1444444444  |
      | User2     | SearchLast| user2.search@example.com| +1555555555  |
      | User3     | Other     | user3.other@example.com | +1666666666  |
    When I search for customers with last name "SearchLast"
    Then the response status is 200
    And the response contains 2 customers
    And all customers in the response have last name "SearchLast"

  Scenario: Create a customer with multiple addresses
    When I create a customer with the following details and addresses:
      | firstName | lastName | email                     | phone        |
      | Multi     | Address  | multi.address@example.com | +1999999999  |
    And the customer has the following addresses:
      | street       | city      | state | zipCode | isDefault |
      | 123 Main St  | Anytown   | NY    | 12345   | true      |
      | 456 Oak Ave  | Othertown | CA    | 67890   | false     |
    Then the customer is successfully created
    And the customer has 2 addresses
    And one of the addresses is marked as default 