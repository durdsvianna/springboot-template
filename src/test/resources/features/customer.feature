Feature: Customer Management
  As a user of the Customer Service API
  I want to be able to manage customers
  So that I can keep customer information up-to-date

  Background:
    Given there are no customers in the system

  Scenario: Create a new customer
    When I create a new customer with the following details:
      | firstName | lastName | email                | phone         |
      | John      | Doe      | john.doe@example.com | +1234567890   |
    Then the response status code should be 201
    And the customer should exist in the system
    And the customer should have the following details:
      | firstName | lastName | email                | phone         |
      | John      | Doe      | john.doe@example.com | +1234567890   |

  Scenario: Get a customer by ID
    Given the following customer exists:
      | firstName | lastName | email                   | phone         |
      | Jane      | Smith    | jane.smith@example.com  | +1987654321   |
    When I retrieve a customer with ID "customerId"
    Then the response status code should be 200
    And the customer should have the following details:
      | firstName | lastName | email                   | phone         |
      | Jane      | Smith    | jane.smith@example.com  | +1987654321   |

  Scenario: Get a customer by email
    Given the following customer exists:
      | firstName | lastName | email                  | phone         |
      | Bob       | Johnson  | bob.johnson@example.com| +1122334455   |
    When I retrieve a customer with email "bob.johnson@example.com"
    Then the response status code should be 200
    And the customer should have the following details:
      | firstName | lastName | email                  | phone         |
      | Bob       | Johnson  | bob.johnson@example.com| +1122334455   |

  Scenario: Update a customer
    Given the following customer exists:
      | firstName | lastName | email                   | phone         |
      | Original  | Customer | original@example.com    | +1555555555   |
    When I update the customer with ID "customerId" with the following details:
      | firstName | lastName    | email               | phone         |
      | Updated   | CustomerNew | updated@example.com | +1666666666   |
    Then the response status code should be 200
    And the customer should have the following details:
      | firstName | lastName    | email               | phone         |
      | Updated   | CustomerNew | updated@example.com | +1666666666   |

  Scenario: Delete a customer
    Given the following customer exists:
      | firstName | lastName | email                   | phone         |
      | ToDelete  | User     | todelete@example.com    | +1777777777   |
    When I delete the customer with ID "customerId"
    Then the response status code should be 204
    And the customer with ID "customerId" should not exist in the system

  Scenario: Search customers by first name
    Given the following customer exists:
      | firstName | lastName | email                   | phone         |
      | Search    | User1    | search.user1@example.com| +1111111111   |
    And the following customer exists:
      | firstName | lastName | email                   | phone         |
      | Search    | User2    | search.user2@example.com| +1222222222   |
    And the following customer exists:
      | firstName | lastName | email                   | phone         |
      | Other     | User     | other.user@example.com  | +1333333333   |
    When I search for customers with first name "Search"
    Then the response status code should be 200
    And the response should contain 2 customers

  Scenario: Search customers by last name
    Given the following customer exists:
      | firstName | lastName   | email                   | phone         |
      | User1     | SearchLast | user1.search@example.com| +1444444444   |
    And the following customer exists:
      | firstName | lastName   | email                   | phone         |
      | User2     | SearchLast | user2.search@example.com| +1555555555   |
    And the following customer exists:
      | firstName | lastName | email                   | phone         |
      | User3     | Other    | user3.other@example.com | +1666666666   |
    When I search for customers with last name "SearchLast"
    Then the response status code should be 200
    And the response should contain 2 customers

  Scenario: Get all customers
    Given the following customer exists:
      | firstName | lastName | email                | phone         |
      | First     | Customer | first@example.com    | +1888888888   |
    And the following customer exists:
      | firstName | lastName | email                | phone         |
      | Second    | Customer | second@example.com   | +1999999999   |
    When I retrieve all customers
    Then the response status code should be 200
    And the response should contain 2 customers