package com.example.customerservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.customerservice.dto.CustomerDto;
import com.example.customerservice.exception.DuplicateResourceException;
import com.example.customerservice.exception.NotFoundException;
import com.example.customerservice.mapper.CustomerMapper;
import com.example.customerservice.model.Customer;
import com.example.customerservice.repository.CustomerRepository;
import com.example.customerservice.service.impl.CustomerServiceImpl;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerMapper customerMapper;

    @Mock
    private AddressService addressService;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private Customer customer;
    private CustomerDto customerDto;
    private final String CUSTOMER_ID = "64a1db45e7cb171cddc52804";

    @BeforeEach
    void setUp() {
        customer = Customer.builder()
                .id(CUSTOMER_ID)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("+1234567890")
                .build();

        customerDto = CustomerDto.builder()
                .id(CUSTOMER_ID)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("+1234567890")
                .build();
    }

    @Test
    void createCustomer_WhenEmailIsUnique_ShouldReturnCustomerDto() {
        // Arrange
        when(customerRepository.existsByEmail(anyString())).thenReturn(false);
        when(customerMapper.toEntity(any(CustomerDto.class))).thenReturn(customer);
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);
        when(customerMapper.toDto(any(Customer.class))).thenReturn(customerDto);

        // Act
        CustomerDto result = customerService.createCustomer(customerDto);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(CUSTOMER_ID);
        assertThat(result.getEmail()).isEqualTo("john.doe@example.com");
        verify(customerRepository, times(1)).existsByEmail(customerDto.getEmail());
        verify(customerMapper, times(1)).toEntity(customerDto);
        verify(customerRepository, times(1)).save(customer);
        verify(customerMapper, times(1)).toDto(customer);
    }

    @Test
    void createCustomer_WhenEmailExists_ShouldThrowDuplicateResourceException() {
        // Arrange
        when(customerRepository.existsByEmail(anyString())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> customerService.createCustomer(customerDto))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Customer with email 'john.doe@example.com' already exists");

        verify(customerRepository, times(1)).existsByEmail(customerDto.getEmail());
        verify(customerMapper, never()).toEntity(any(CustomerDto.class));
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void getCustomerById_WhenCustomerExists_ShouldReturnCustomerDto() {
        // Arrange
        when(customerRepository.findById(anyString())).thenReturn(Optional.of(customer));
        when(customerMapper.toDto(any(Customer.class))).thenReturn(customerDto);

        // Act
        CustomerDto result = customerService.getCustomerById(CUSTOMER_ID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(CUSTOMER_ID);
        verify(customerRepository, times(1)).findById(CUSTOMER_ID);
        verify(customerMapper, times(1)).toDto(customer);
    }

    @Test
    void getCustomerById_WhenCustomerDoesNotExist_ShouldThrowNotFoundException() {
        // Arrange
        when(customerRepository.findById(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> customerService.getCustomerById(CUSTOMER_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Customer with id '" + CUSTOMER_ID + "' not found");

        verify(customerRepository, times(1)).findById(CUSTOMER_ID);
        verify(customerMapper, never()).toDto(any(Customer.class));
    }

    @Test
    void getCustomerByEmail_WhenCustomerExists_ShouldReturnCustomerDto() {
        // Arrange
        String email = "john.doe@example.com";
        when(customerRepository.findByEmail(anyString())).thenReturn(Optional.of(customer));
        when(customerMapper.toDto(any(Customer.class))).thenReturn(customerDto);

        // Act
        CustomerDto result = customerService.getCustomerByEmail(email);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(email);
        verify(customerRepository, times(1)).findByEmail(email);
        verify(customerMapper, times(1)).toDto(customer);
    }

    @Test
    void getCustomerByEmail_WhenCustomerDoesNotExist_ShouldThrowNotFoundException() {
        // Arrange
        String email = "john.doe@example.com";
        when(customerRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> customerService.getCustomerByEmail(email))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Customer with email '" + email + "' not found");

        verify(customerRepository, times(1)).findByEmail(email);
        verify(customerMapper, never()).toDto(any(Customer.class));
    }

    @Test
    void updateCustomer_WhenCustomerExistsAndEmailNotChanged_ShouldReturnUpdatedCustomerDto() {
        // Arrange
        when(customerRepository.findById(anyString())).thenReturn(Optional.of(customer));
        when(customerMapper.updateEntity(any(CustomerDto.class), any(Customer.class))).thenReturn(customer);
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);
        when(customerMapper.toDto(any(Customer.class))).thenReturn(customerDto);

        // Act
        CustomerDto result = customerService.updateCustomer(CUSTOMER_ID, customerDto);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(CUSTOMER_ID);
        verify(customerRepository, times(1)).findById(CUSTOMER_ID);
        verify(customerMapper, times(1)).updateEntity(customerDto, customer);
        verify(customerRepository, times(1)).save(customer);
        verify(customerMapper, times(1)).toDto(customer);
    }

    @Test
    void updateCustomer_WhenCustomerDoesNotExist_ShouldThrowNotFoundException() {
        // Arrange
        when(customerRepository.findById(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> customerService.updateCustomer(CUSTOMER_ID, customerDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Customer with id '" + CUSTOMER_ID + "' not found");

        verify(customerRepository, times(1)).findById(CUSTOMER_ID);
        verify(customerMapper, never()).updateEntity(any(CustomerDto.class), any(Customer.class));
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void deleteCustomer_WhenCustomerExists_ShouldDeleteCustomerAndAddresses() {
        // Arrange
        when(customerRepository.existsById(anyString())).thenReturn(true);
        doNothing().when(addressService).deleteAddressesByCustomerId(anyString());
        doNothing().when(customerRepository).deleteById(anyString());

        // Act
        customerService.deleteCustomer(CUSTOMER_ID);

        // Assert
        verify(customerRepository, times(1)).existsById(CUSTOMER_ID);
        verify(addressService, times(1)).deleteAddressesByCustomerId(CUSTOMER_ID);
        verify(customerRepository, times(1)).deleteById(CUSTOMER_ID);
    }

    @Test
    void deleteCustomer_WhenCustomerDoesNotExist_ShouldThrowNotFoundException() {
        // Arrange
        when(customerRepository.existsById(anyString())).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> customerService.deleteCustomer(CUSTOMER_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Customer with id '" + CUSTOMER_ID + "' not found");

        verify(customerRepository, times(1)).existsById(CUSTOMER_ID);
        verify(addressService, never()).deleteAddressesByCustomerId(anyString());
        verify(customerRepository, never()).deleteById(anyString());
    }

    @Test
    void getAllCustomers_ShouldReturnListOfCustomerDtos() {
        // Arrange
        List<Customer> customers = Arrays.asList(customer, 
                Customer.builder().id("456").firstName("Jane").lastName("Smith").build());
        List<CustomerDto> customerDtos = Arrays.asList(customerDto, 
                CustomerDto.builder().id("456").firstName("Jane").lastName("Smith").build());

        when(customerRepository.findAll()).thenReturn(customers);
        when(customerMapper.toDtoList(any())).thenReturn(customerDtos);

        // Act
        List<CustomerDto> result = customerService.getAllCustomers();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(2);
        verify(customerRepository, times(1)).findAll();
        verify(customerMapper, times(1)).toDtoList(customers);
    }

    @Test
    void findCustomersByFirstName_ShouldReturnMatchingCustomers() {
        // Arrange
        String firstName = "John";
        List<Customer> customers = List.of(customer);
        List<CustomerDto> customerDtos = List.of(customerDto);

        when(customerRepository.findByFirstName(anyString())).thenReturn(customers);
        when(customerMapper.toDtoList(any())).thenReturn(customerDtos);

        // Act
        List<CustomerDto> result = customerService.findCustomersByFirstName(firstName);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getFirstName()).isEqualTo(firstName);
        verify(customerRepository, times(1)).findByFirstName(firstName);
        verify(customerMapper, times(1)).toDtoList(customers);
    }

    @Test
    void findCustomersByLastName_ShouldReturnMatchingCustomers() {
        // Arrange
        String lastName = "Doe";
        List<Customer> customers = List.of(customer);
        List<CustomerDto> customerDtos = List.of(customerDto);

        when(customerRepository.findByLastName(anyString())).thenReturn(customers);
        when(customerMapper.toDtoList(any())).thenReturn(customerDtos);

        // Act
        List<CustomerDto> result = customerService.findCustomersByLastName(lastName);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getLastName()).isEqualTo(lastName);
        verify(customerRepository, times(1)).findByLastName(lastName);
        verify(customerMapper, times(1)).toDtoList(customers);
    }
}