package com.example.customerservice.service;

import com.example.customerservice.dto.AddressDto;
import com.example.customerservice.model.Address;
import com.example.customerservice.repository.AddressRepository;
import com.example.customerservice.service.impl.AddressServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressServiceImplTest {

    @Mock
    private AddressRepository addressRepository;

    @InjectMocks
    private AddressServiceImpl addressService;

    private Address testAddress;
    private AddressDto testAddressDto;
    private Address defaultAddress;

    @BeforeEach
    void setUp() {
        testAddress = Address.builder()
                .id("addr1")
                .street("123 Main St")
                .city("Anytown")
                .state("NY")
                .zipCode("12345")
                .country("USA")
                .isDefault(false)
                .customerId("customer1")
                .build();

        defaultAddress = Address.builder()
                .id("addr2")
                .street("456 Oak St")
                .city("Othertown")
                .state("CA")
                .zipCode("67890")
                .country("USA")
                .isDefault(true)
                .customerId("customer1")
                .build();

        testAddressDto = AddressDto.builder()
                .id("addr1")
                .street("123 Main St")
                .city("Anytown")
                .state("NY")
                .zipCode("12345")
                .country("USA")
                .isDefault(false)
                .customerId("customer1")
                .build();
    }

    @Test
    void createAddress_ShouldCreateAddress() {
        // Given
        when(addressRepository.save(any(Address.class))).thenReturn(testAddress);

        // When
        AddressDto result = addressService.createAddress(testAddressDto);

        // Then
        assertNotNull(result);
        assertEquals(testAddress.getId(), result.getId());
        assertEquals(testAddress.getStreet(), result.getStreet());
        verify(addressRepository).save(any(Address.class));
    }

    @Test
    void createAddress_WithDefaultTrue_ShouldResetExistingDefault() {
        // Given
        testAddressDto.setIsDefault(true);
        when(addressRepository.findByCustomerIdAndIsDefaultTrue(anyString()))
                .thenReturn(Optional.of(defaultAddress));
        when(addressRepository.save(any(Address.class))).thenReturn(testAddress);

        // When
        addressService.createAddress(testAddressDto);

        // Then
        verify(addressRepository).findByCustomerIdAndIsDefaultTrue("customer1");
        // Verify the existing default address is updated to non-default
        verify(addressRepository).save(defaultAddress);
        // Verify the new address is saved
        verify(addressRepository, times(2)).save(any(Address.class));
    }

    @Test
    void getAddressById_ShouldReturnAddress() {
        // Given
        when(addressRepository.findById(anyString())).thenReturn(Optional.of(testAddress));

        // When
        AddressDto result = addressService.getAddressById("addr1");

        // Then
        assertNotNull(result);
        assertEquals(testAddress.getId(), result.getId());
        assertEquals(testAddress.getStreet(), result.getStreet());
        verify(addressRepository).findById("addr1");
    }

    @Test
    void getAddressById_WithNonExistentId_ShouldThrowException() {
        // Given
        when(addressRepository.findById(anyString())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NoSuchElementException.class, () -> addressService.getAddressById("nonexistent"));
        verify(addressRepository).findById("nonexistent");
    }

    @Test
    void getAddressesByCustomerId_ShouldReturnAddresses() {
        // Given
        List<Address> addresses = Arrays.asList(testAddress, defaultAddress);
        when(addressRepository.findByCustomerId(anyString())).thenReturn(addresses);

        // When
        List<AddressDto> result = addressService.getAddressesByCustomerId("customer1");

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(testAddress.getId(), result.get(0).getId());
        assertEquals(defaultAddress.getId(), result.get(1).getId());
        verify(addressRepository).findByCustomerId("customer1");
    }

    @Test
    void getDefaultAddress_ShouldReturnDefaultAddress() {
        // Given
        when(addressRepository.findByCustomerIdAndIsDefaultTrue(anyString()))
                .thenReturn(Optional.of(defaultAddress));

        // When
        AddressDto result = addressService.getDefaultAddress("customer1");

        // Then
        assertNotNull(result);
        assertEquals(defaultAddress.getId(), result.getId());
        assertTrue(result.getIsDefault());
        verify(addressRepository).findByCustomerIdAndIsDefaultTrue("customer1");
    }

    @Test
    void getDefaultAddress_WithNoDefault_ShouldThrowException() {
        // Given
        when(addressRepository.findByCustomerIdAndIsDefaultTrue(anyString()))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(NoSuchElementException.class, () -> addressService.getDefaultAddress("customer1"));
        verify(addressRepository).findByCustomerIdAndIsDefaultTrue("customer1");
    }

    @Test
    void searchAddresses_ByCity_ShouldReturnMatchingAddresses() {
        // Given
        List<Address> addresses = Arrays.asList(testAddress);
        when(addressRepository.findByCity(anyString())).thenReturn(addresses);

        // When
        List<AddressDto> result = addressService.searchAddresses("Anytown", null, null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testAddress.getId(), result.get(0).getId());
        verify(addressRepository).findByCity("Anytown");
    }

    @Test
    void searchAddresses_ByState_ShouldReturnMatchingAddresses() {
        // Given
        List<Address> addresses = Arrays.asList(testAddress);
        when(addressRepository.findByState(anyString())).thenReturn(addresses);

        // When
        List<AddressDto> result = addressService.searchAddresses(null, "NY", null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testAddress.getId(), result.get(0).getId());
        verify(addressRepository).findByState("NY");
    }

    @Test
    void searchAddresses_ByZipCode_ShouldReturnMatchingAddresses() {
        // Given
        List<Address> addresses = Arrays.asList(testAddress);
        when(addressRepository.findByZipCode(anyString())).thenReturn(addresses);

        // When
        List<AddressDto> result = addressService.searchAddresses(null, null, "12345");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testAddress.getId(), result.get(0).getId());
        verify(addressRepository).findByZipCode("12345");
    }

    @Test
    void updateAddress_ShouldUpdateAddress() {
        // Given
        AddressDto updateDto = AddressDto.builder()
                .street("Updated Street")
                .city("Updated City")
                .state("TX")
                .zipCode("54321")
                .country("USA")
                .isDefault(false)
                .build();

        Address updatedAddress = Address.builder()
                .id("addr1")
                .street("Updated Street")
                .city("Updated City")
                .state("TX")
                .zipCode("54321")
                .country("USA")
                .isDefault(false)
                .customerId("customer1")
                .build();

        when(addressRepository.findById(anyString())).thenReturn(Optional.of(testAddress));
        when(addressRepository.save(any(Address.class))).thenReturn(updatedAddress);

        // When
        AddressDto result = addressService.updateAddress("addr1", updateDto);

        // Then
        assertNotNull(result);
        assertEquals("Updated Street", result.getStreet());
        assertEquals("Updated City", result.getCity());
        assertEquals("TX", result.getState());
        verify(addressRepository).save(any(Address.class));
    }

    @Test
    void updateAddress_SettingAsDefault_ShouldResetExistingDefault() {
        // Given
        AddressDto updateDto = AddressDto.builder()
                .street("123 Main St")
                .city("Anytown")
                .state("NY")
                .zipCode("12345")
                .country("USA")
                .isDefault(true) // Setting as default
                .build();

        when(addressRepository.findById(anyString())).thenReturn(Optional.of(testAddress));
        when(addressRepository.findByCustomerIdAndIsDefaultTrue(anyString()))
                .thenReturn(Optional.of(defaultAddress));
        when(addressRepository.save(any(Address.class))).thenReturn(testAddress);

        // When
        addressService.updateAddress("addr1", updateDto);

        // Then
        verify(addressRepository).findByCustomerIdAndIsDefaultTrue("customer1");
        // Verify the existing default address is updated to non-default
        verify(addressRepository).save(defaultAddress);
        // Verify the updated address is saved
        verify(addressRepository, times(2)).save(any(Address.class));
    }

    @Test
    void deleteAddress_ShouldDeleteAddress() {
        // Given
        when(addressRepository.findById(anyString())).thenReturn(Optional.of(testAddress));
        doNothing().when(addressRepository).delete(any(Address.class));

        // When
        addressService.deleteAddress("addr1");

        // Then
        verify(addressRepository).findById("addr1");
        verify(addressRepository).delete(testAddress);
    }

    @Test
    void deleteCustomerAddresses_ShouldDeleteAllCustomerAddresses() {
        // Given
        List<Address> addresses = Arrays.asList(testAddress, defaultAddress);
        when(addressRepository.findByCustomerId(anyString())).thenReturn(addresses);
        doNothing().when(addressRepository).deleteAll(anyList());

        // When
        addressService.deleteCustomerAddresses("customer1");

        // Then
        verify(addressRepository).findByCustomerId("customer1");
        verify(addressRepository).deleteAll(addresses);
    }

    @Test
    void setDefaultAddress_ShouldSetAddressAsDefault() {
        // Given
        when(addressRepository.findById(anyString())).thenReturn(Optional.of(testAddress));
        when(addressRepository.findByCustomerIdAndIsDefaultTrue(anyString()))
                .thenReturn(Optional.of(defaultAddress));

        Address updatedAddress = Address.builder()
                .id("addr1")
                .street("123 Main St")
                .city("Anytown")
                .state("NY")
                .zipCode("12345")
                .country("USA")
                .isDefault(true)
                .customerId("customer1")
                .build();

        when(addressRepository.save(any(Address.class))).thenReturn(updatedAddress);

        // When
        AddressDto result = addressService.setDefaultAddress("addr1", "customer1");

        // Then
        assertNotNull(result);
        assertTrue(result.getIsDefault());
        verify(addressRepository).findById("addr1");
        verify(addressRepository).findByCustomerIdAndIsDefaultTrue("customer1");
        // Verify the existing default address is updated to non-default
        verify(addressRepository).save(defaultAddress);
        // Verify the new address is saved as default
        verify(addressRepository, times(2)).save(any(Address.class));
    }

    @Test
    void setDefaultAddress_WithAddressNotBelongingToCustomer_ShouldThrowException() {
        // Given
        Address wrongCustomerAddress = Address.builder()
                .id("addr1")
                .customerId("differentCustomer")
                .build();

        when(addressRepository.findById(anyString())).thenReturn(Optional.of(wrongCustomerAddress));

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> addressService.setDefaultAddress("addr1", "customer1")
        );
        assertTrue(exception.getMessage().contains("does not belong to the specified customer"));
        verify(addressRepository, never()).save(any(Address.class));
    }
} 