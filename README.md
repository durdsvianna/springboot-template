# Customer Service Microservice

A Spring Boot microservice for managing customers and their addresses.

## Technologies

- Java 21
- Spring Boot 3.2.3
- Spring Data MongoDB
- MongoDB
- Lombok
- SpringDoc OpenAPI (Swagger UI)
- Validation
- JUnit 5
- Cucumber for BDD testing
- TestContainers for MongoDB integration testing

## Features

- Customer management (CRUD operations)
- Address management (CRUD operations)
- Support for multiple addresses per customer
- Default address management
- Search functionality for customers and addresses
- Swagger documentation
- Comprehensive validation
- Error handling

## API Endpoints

### Customer API

- `POST /customers` - Create a new customer
- `GET /customers/{id}` - Get customer by ID
- `GET /customers/email/{email}` - Get customer by email
- `GET /customers` - Get all customers
- `GET /customers/search/firstname?firstName={firstName}` - Search customers by first name
- `GET /customers/search/lastname?lastName={lastName}` - Search customers by last name
- `PUT /customers/{id}` - Update an existing customer
- `DELETE /customers/{id}` - Delete a customer

### Address API

- `POST /addresses/customer/{customerId}` - Create a new address for a customer
- `GET /addresses/{id}` - Get address by ID
- `GET /addresses/customer/{customerId}` - Get all addresses for a customer
- `GET /addresses/customer/{customerId}/default` - Get default address for a customer
- `GET /addresses/customer/{customerId}/search?city={city}` - Search addresses by city for a customer
- `PUT /addresses/{id}` - Update an existing address
- `PUT /addresses/{addressId}/customer/{customerId}/default` - Set an address as default
- `DELETE /addresses/{id}` - Delete an address

## Getting Started

### Prerequisites

- Java 21
- Docker (for running MongoDB)
- Maven

### Running MongoDB

```bash
docker run -d -p 27017:27017 --name mongodb mongo:latest
```

### Building the Application

```bash
mvn clean package
```

### Running the Application

```bash
java -jar target/customer-service-0.0.1-SNAPSHOT.jar
```

### Accessing the API Documentation

Once the application is running, you can access the Swagger UI at:

```
http://localhost:8080/swagger-ui.html
```

## Testing Strategy

This project employs a comprehensive testing approach with multiple layers:

### Unit Tests

Unit tests verify the functionality of individual components in isolation:

- **Service Layer**: Tests for `CustomerServiceImpl` and `AddressServiceImpl` ensure that all business logic is working correctly, with dependencies mocked using Mockito.
- **Coverage**: Focus on testing business logic, validations, edge cases, and exception scenarios.

Run unit tests:
```bash
mvn test -Dtest=*Test
```

### Integration Tests

Integration tests verify the interaction between different components:

- **Controller Layer**: Tests for `CustomerController` and `AddressController` ensure that the API endpoints function correctly.
- **Database Integration**: Tests use TestContainers to spin up a real MongoDB instance, ensuring that database operations work as expected.
- **End-to-End Flow**: Tests cover the entire request-response cycle from the controller through the service layer to the repository.

Run integration tests:
```bash
mvn test -Dtest=*IntegrationTest
```

### BDD Tests

Behavior-Driven Development tests ensure that the application meets business requirements:

- **Cucumber Features**: Written in Gherkin syntax (Given-When-Then), these tests describe the expected behavior from a user's perspective.
- **Step Definitions**: Java code that maps the Gherkin steps to actual test code.
- **Scenarios**: Cover key user journeys such as creating customers/addresses, updating them, searching, etc.

Run BDD tests:
```bash
mvn test -Dtest=CucumberIntegrationTest
```

### Test Coverage

- **Services**: 90%+ coverage for core business logic
- **Controllers**: 80%+ coverage for API endpoints
- **Exception Handling**: Comprehensive testing of error scenarios and responses

### Running All Tests

```bash
mvn test
```

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/my-feature`)
3. Commit your changes (`git commit -am 'Add my feature'`)
4. Push to the branch (`git push origin feature/my-feature`)
5. Create a Pull Request