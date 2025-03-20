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
- RestAssured for API testing
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

- `POST /api/customers` - Create a new customer
- `GET /api/customers/{id}` - Get customer by ID
- `GET /api/customers/email/{email}` - Get customer by email
- `GET /api/customers` - Get all customers or search by name
- `PUT /api/customers/{id}` - Update an existing customer
- `DELETE /api/customers/{id}` - Delete a customer

### Address API

- `POST /api/addresses` - Create a new address
- `GET /api/addresses/{id}` - Get address by ID
- `GET /api/addresses/customer/{customerId}` - Get all addresses for a customer
- `GET /api/addresses/customer/{customerId}/default` - Get default address for a customer
- `GET /api/addresses/search` - Search addresses by city, state, or zip code
- `PUT /api/addresses/{id}` - Update an existing address
- `PUT /api/addresses/{addressId}/customer/{customerId}/default` - Set an address as default
- `DELETE /api/addresses/{id}` - Delete an address

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
http://localhost:8080/api/swagger-ui.html
```

## Testing

The application includes both unit tests and integration tests:

```bash
# Run all tests
mvn test

# Run only unit tests
mvn test -Dtest=*Test

# Run only integration tests
mvn test -Dtest=*IntegrationTest
``` 