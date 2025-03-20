package com.example.customerservice.service;

import com.example.customerservice.dto.AddressDto;
import com.example.customerservice.dto.CustomerDto;
import com.example.customerservice.model.Customer;
import com.example.customerservice.repository.CustomerRepository;
import com.example.customerservice.service.impl.CustomerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private AddressService addressService;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private Customer testCustomer;
    private CustomerDto testCustomerDto;
    private AddressDto testAddressDto;
    private List<AddressDto> addressList;

    @BeforeEach
    void setUp() {
        testCustomer = Customer.builder()
                .id("1")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("+1234567890")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .addressIds(new ArrayList<>(Arrays.asList("addr1")))
                .build();

        testCustomerDto = CustomerDto.builder()
                .id("1")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("+1234567890")
                .build();

        testAddressDto = AddressDto.builder()
                .id("addr1")
                .street("123 Main St")
                .city("Anytown")
                .state("NY")
                .zipCode("12345")
                .customerId("1")
                .isDefault(true)
                .build();

        addressList = new ArrayList<>();
        addressList.add(testAddressDto);
    }

    @Test
    void createCustomer_ShouldCreateCustomerWithoutAddresses() {
        // Given
        when(customerRepository.existsByEmail(anyString())).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);

        // When
        CustomerDto result = customerService.createCustomer(testCustomerDto);

        // Then
        assertNotNull(result);
        assertEquals(testCustomer.getId(), result.getId());
        assertEquals(testCustomer.getFirstName(), result.getFirstName());
        assertEquals(testCustomer.getEmail(), result.getEmail());
        verify(customerRepository).existsByEmail(testCustomerDto.getEmail());
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void createCustomer_WithExistingEmail_ShouldThrowException() {
        // Given
        when(customerRepository.existsByEmail(anyString())).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> customerService.createCustomer(testCustomerDto)
        );
        assertTrue(exception.getMessage().contains("already exists"));
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void createCustomer_WithAddresses_ShouldCreateCustomerAndAddresses() {
        // Given
        testCustomerDto.setAddresses(addressList);
        when(customerRepository.existsByEmail(anyString())).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);
        when(addressService.createAddress(any(AddressDto.class))).thenReturn(testAddressDto);

        // When
        CustomerDto result = customerService.createCustomer(testCustomerDto);

        // Then
        assertNotNull(result);
        verify(addressService).createAddress(any(AddressDto.class));
        verify(customerRepository, times(2)).save(any(Customer.class)); // First save and then update with address IDs
    }

    @Test
    void getCustomerById_ShouldReturnCustomer() {
        // Given
        when(customerRepository.findById(anyString())).thenReturn(Optional.of(testCustomer));
        when(addressService.getAddressesByCustomerId(anyString())).thenReturn(addressList);

        // When
        CustomerDto result = customerService.getCustomerById("1");

        // Then
        assertNotNull(result);
        assertEquals(testCustomer.getId(), result.getId());
        assertEquals(testCustomer.getEmail(), result.getEmail());
        assertNotNull(result.getAddresses());
        verify(customerRepository).findById("1");
        verify(addressService).getAddressesByCustomerId("1");
    }

    @Test
    void getCustomerById_WithNonExistentId_ShouldThrowException() {
        // Given
        when(customerRepository.findById(anyString())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NoSuchElementException.class, () -> customerService.getCustomerById("nonexistent"));
        verify(customerRepository).findById("nonexistent");
    }

    @Test
    void getCustomerByEmail_ShouldReturnCustomer() {
        // Given
        when(customerRepository.findByEmail(anyString())).thenReturn(Optional.of(testCustomer));
        when(addressService.getAddressesByCustomerId(anyString())).thenReturn(addressList);

        // When
        CustomerDto result = customerService.getCustomerByEmail("john.doe@example.com");

        // Then
        assertNotNull(result);
        assertEquals(testCustomer.getEmail(), result.getEmail());
        verify(customerRepository).findByEmail("john.doe@example.com");
    }

    @Test
    void getCustomerByEmail_WithNonExistentEmail_ShouldThrowException() {
        // Given
        when(customerRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NoSuchElementException.class, 
                () -> customerService.getCustomerByEmail("nonexistent@example.com"));
        verify(customerRepository).findByEmail("nonexistent@example.com");
    }

    @Test
    void getAllCustomers_ShouldReturnAllCustomers() {
        // Given
        List<Customer> customers = Arrays.asList(testCustomer, 
                Customer.builder()
                        .id("2")
                        .firstName("Jane")
                        .lastName("Smith")
                        .email("jane.smith@example.com")
                        .build());
        when(customerRepository.findAll()).thenReturn(customers);
        when(addressService.getAddressesByCustomerId(anyString())).thenReturn(addressList);

        // When
        List<CustomerDto> result = customerService.getAllCustomers();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(customerRepository).findAll();
        verify(addressService, times(2)).getAddressesByCustomerId(anyString());
    }

    @Test
    void updateCustomer_ShouldUpdateCustomer() {
        // Given
        CustomerDto updateDto = CustomerDto.builder()
                .firstName("John Updated")
                .lastName("Doe Updated")
                .phone("+9876543210")
                .build();

        Customer updatedCustomer = Customer.builder()
                .id("1")
                .firstName("John Updated")
                .lastName("Doe Updated")
                .email("john.doe@example.com") // Email stays the same
                .phone("+9876543210")
                .addressIds(new ArrayList<>(Arrays.asList("addr1")))
                .build();

        when(customerRepository.findById(anyString())).thenReturn(Optional.of(testCustomer));
        when(customerRepository.save(any(Customer.class))).thenReturn(updatedCustomer);
        when(addressService.getAddressesByCustomerId(anyString())).thenReturn(addressList);

        // When
        CustomerDto result = customerService.updateCustomer("1", updateDto);

        // Then
        assertNotNull(result);
        assertEquals("John Updated", result.getFirstName());
        assertEquals("Doe Updated", result.getLastName());
        assertEquals("+9876543210", result.getPhone());
        assertEquals("john.doe@example.com", result.getEmail()); // Email shouldn't change
        verify(customerRepository).findById("1");
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void updateCustomer_WithNonExistentId_ShouldThrowException() {
        // Given
        CustomerDto updateDto = CustomerDto.builder()
                .firstName("John Updated")
                .build();
        when(customerRepository.findById(anyString())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NoSuchElementException.class, 
                () -> customerService.updateCustomer("nonexistent", updateDto));
        verify(customerRepository).findById("nonexistent");
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void deleteCustomer_ShouldDeleteCustomerAndAddresses() {
        // Given
        when(customerRepository.findById(anyString())).thenReturn(Optional.of(testCustomer));
        doNothing().when(customerRepository).deleteById(anyString());
        doNothing().when(addressService).deleteAddressesByCustomerId(anyString());

        // When
        customerService.deleteCustomer("1");

        // Then
        verify(customerRepository).findById("1");
        verify(customerRepository).deleteById("1");
        verify(addressService).deleteAddressesByCustomerId("1");
    }

    @Test
    void deleteCustomer_WithNonExistentId_ShouldThrowException() {
        // Given
        when(customerRepository.findById(anyString())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NoSuchElementException.class, 
                () -> customerService.deleteCustomer("nonexistent"));
        verify(customerRepository).findById("nonexistent");
        verify(customerRepository, never()).deleteById(anyString());
        verify(addressService, never()).deleteAddressesByCustomerId(anyString());
    }

    @Test
    void searchCustomersByFirstName_ShouldReturnMatchingCustomers() {
        // Given
        List<Customer> customers = Arrays.asList(testCustomer);
        when(customerRepository.findByFirstNameContainingIgnoreCase(anyString())).thenReturn(customers);
        when(addressService.getAddressesByCustomerId(anyString())).thenReturn(addressList);

        // When
        List<CustomerDto> result = customerService.searchCustomersByFirstName("John");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("John", result.get(0).getFirstName());
        verify(customerRepository).findByFirstNameContainingIgnoreCase("John");
    }

    @Test
    void searchCustomersByLastName_ShouldReturnMatchingCustomers() {
        // Given
        List<Customer> customers = Arrays.asList(testCustomer);
        when(customerRepository.findByLastNameContainingIgnoreCase(anyString())).thenReturn(customers);
        when(addressService.getAddressesByCustomerId(anyString())).thenReturn(addressList);

        // When
        List<CustomerDto> result = customerService.searchCustomersByLastName("Doe");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Doe", result.get(0).getLastName());
        verify(customerRepository).findByLastNameContainingIgnoreCase("Doe");
    }
}