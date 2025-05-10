package com.example.customerservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
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

import com.example.customerservice.dto.AddressDto;
import com.example.customerservice.exception.NotFoundException;
import com.example.customerservice.mapper.AddressMapper;
import com.example.customerservice.model.Address;
import com.example.customerservice.repository.AddressRepository;
import com.example.customerservice.repository.CustomerRepository;
import com.example.customerservice.service.impl.AddressServiceImpl;

@ExtendWith(MockitoExtension.class)
class AddressServiceImplTest {

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private AddressMapper addressMapper;

    @InjectMocks
    private AddressServiceImpl addressService;

    private Address address;
    private AddressDto addressDto;
    private final String ADDRESS_ID = "64a1db45e7cb171cddc52805";
    private final String CUSTOMER_ID = "64a1db45e7cb171cddc52804";

    @BeforeEach
    void setUp() {
        address = Address.builder()
                .id(ADDRESS_ID)
                .customerId(CUSTOMER_ID)
                .street("123 Main St")
                .city("Anytown")
                .state("NY")
                .zipCode("12345")
                .country("USA")
                .isDefault(true)
                .build();

        addressDto = AddressDto.builder()
                .id(ADDRESS_ID)
                .customerId(CUSTOMER_ID)
                .street("123 Main St")
                .city("Anytown")
                .state("NY")
                .zipCode("12345")
                .country("USA")
                .isDefault(true)
                .build();
    }

    @Test
    void createAddress_WhenCustomerExistsAndNoCurrentDefault_ShouldReturnAddressDto() {
        // Arrange
        when(customerRepository.existsById(anyString())).thenReturn(true);
        when(addressRepository.findByCustomerId(anyString())).thenReturn(List.of());
        when(addressMapper.toEntity(any(AddressDto.class))).thenReturn(address);
        when(addressRepository.save(any(Address.class))).thenReturn(address);
        when(addressMapper.toDto(any(Address.class))).thenReturn(addressDto);

        // Act
        AddressDto result = addressService.createAddress(CUSTOMER_ID, addressDto);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(ADDRESS_ID);
        assertThat(result.getCustomerId()).isEqualTo(CUSTOMER_ID);
        assertThat(result.isDefault()).isTrue();
        verify(customerRepository, times(1)).existsById(CUSTOMER_ID);
        verify(addressRepository, times(1)).findByCustomerId(CUSTOMER_ID);
        verify(addressMapper, times(1)).toEntity(addressDto);
        verify(addressRepository, times(1)).save(address);
        verify(addressMapper, times(1)).toDto(address);
    }

    @Test
    void createAddress_WhenCustomerExistsWithCurrentDefault_ShouldClearPreviousDefault() {
        // Arrange
        Address existingAddress = Address.builder()
                .id("456")
                .customerId(CUSTOMER_ID)
                .isDefault(true)
                .build();
        
        when(customerRepository.existsById(anyString())).thenReturn(true);
        when(addressRepository.findByCustomerId(anyString())).thenReturn(List.of(existingAddress));
        when(addressRepository.findByCustomerIdAndIsDefault(anyString(), anyBoolean()))
                .thenReturn(Optional.of(existingAddress));
        when(addressRepository.save(any(Address.class))).thenReturn(address);
        when(addressMapper.toEntity(any(AddressDto.class))).thenReturn(address);
        when(addressMapper.toDto(any(Address.class))).thenReturn(addressDto);

        // Act
        AddressDto result = addressService.createAddress(CUSTOMER_ID, addressDto);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.isDefault()).isTrue();
        verify(addressRepository, times(1))
                .findByCustomerIdAndIsDefault(CUSTOMER_ID, true);
        verify(addressRepository, times(2)).save(any(Address.class));
    }

    @Test
    void createAddress_WhenCustomerDoesNotExist_ShouldThrowNotFoundException() {
        // Arrange
        when(customerRepository.existsById(anyString())).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> addressService.createAddress(CUSTOMER_ID, addressDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Customer with id '" + CUSTOMER_ID + "' not found");

        verify(customerRepository, times(1)).existsById(CUSTOMER_ID);
        verify(addressMapper, never()).toEntity(any(AddressDto.class));
        verify(addressRepository, never()).save(any(Address.class));
    }

    @Test
    void getAddressById_WhenAddressExists_ShouldReturnAddressDto() {
        // Arrange
        when(addressRepository.findById(anyString())).thenReturn(Optional.of(address));
        when(addressMapper.toDto(any(Address.class))).thenReturn(addressDto);

        // Act
        AddressDto result = addressService.getAddressById(ADDRESS_ID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(ADDRESS_ID);
        verify(addressRepository, times(1)).findById(ADDRESS_ID);
        verify(addressMapper, times(1)).toDto(address);
    }

    @Test
    void getAddressById_WhenAddressDoesNotExist_ShouldThrowNotFoundException() {
        // Arrange
        when(addressRepository.findById(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> addressService.getAddressById(ADDRESS_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Address with id '" + ADDRESS_ID + "' not found");

        verify(addressRepository, times(1)).findById(ADDRESS_ID);
        verify(addressMapper, never()).toDto(any(Address.class));
    }

    @Test
    void getAddressesByCustomerId_WhenCustomerExists_ShouldReturnAddressList() {
        // Arrange
        List<Address> addresses = Arrays.asList(address, 
                Address.builder().id("456").customerId(CUSTOMER_ID).build());
        List<AddressDto> addressDtos = Arrays.asList(addressDto, 
                AddressDto.builder().id("456").customerId(CUSTOMER_ID).build());

        when(customerRepository.existsById(anyString())).thenReturn(true);
        when(addressRepository.findByCustomerId(anyString())).thenReturn(addresses);
        when(addressMapper.toDtoList(any())).thenReturn(addressDtos);

        // Act
        List<AddressDto> result = addressService.getAddressesByCustomerId(CUSTOMER_ID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(2);
        verify(customerRepository, times(1)).existsById(CUSTOMER_ID);
        verify(addressRepository, times(1)).findByCustomerId(CUSTOMER_ID);
        verify(addressMapper, times(1)).toDtoList(addresses);
    }

    @Test
    void getAddressesByCustomerId_WhenCustomerDoesNotExist_ShouldThrowNotFoundException() {
        // Arrange
        when(customerRepository.existsById(anyString())).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> addressService.getAddressesByCustomerId(CUSTOMER_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Customer with id '" + CUSTOMER_ID + "' not found");

        verify(customerRepository, times(1)).existsById(CUSTOMER_ID);
        verify(addressRepository, never()).findByCustomerId(anyString());
    }

    @Test
    void getDefaultAddress_WhenDefaultAddressExists_ShouldReturnAddressDto() {
        // Arrange
        when(customerRepository.existsById(anyString())).thenReturn(true);
        when(addressRepository.findByCustomerIdAndIsDefault(anyString(), anyBoolean()))
                .thenReturn(Optional.of(address));
        when(addressMapper.toDto(any(Address.class))).thenReturn(addressDto);

        // Act
        AddressDto result = addressService.getDefaultAddress(CUSTOMER_ID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.isDefault()).isTrue();
        verify(customerRepository, times(1)).existsById(CUSTOMER_ID);
        verify(addressRepository, times(1)).findByCustomerIdAndIsDefault(CUSTOMER_ID, true);
        verify(addressMapper, times(1)).toDto(address);
    }

    @Test
    void getDefaultAddress_WhenNoDefaultAddress_ShouldThrowNotFoundException() {
        // Arrange
        when(customerRepository.existsById(anyString())).thenReturn(true);
        when(addressRepository.findByCustomerIdAndIsDefault(anyString(), anyBoolean()))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> addressService.getDefaultAddress(CUSTOMER_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Default address for customer with id '" + CUSTOMER_ID + "' not found");

        verify(customerRepository, times(1)).existsById(CUSTOMER_ID);
        verify(addressRepository, times(1)).findByCustomerIdAndIsDefault(CUSTOMER_ID, true);
    }

    @Test
    void updateAddress_WhenAddressExists_ShouldReturnUpdatedAddressDto() {
        // Arrange
        when(addressRepository.findById(anyString())).thenReturn(Optional.of(address));
        when(addressMapper.updateEntity(any(AddressDto.class), any(Address.class))).thenReturn(address);
        when(addressRepository.save(any(Address.class))).thenReturn(address);
        when(addressMapper.toDto(any(Address.class))).thenReturn(addressDto);

        // Act
        AddressDto result = addressService.updateAddress(ADDRESS_ID, addressDto);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(ADDRESS_ID);
        verify(addressRepository, times(1)).findById(ADDRESS_ID);
        verify(addressMapper, times(1)).updateEntity(addressDto, address);
        verify(addressRepository, times(1)).save(address);
        verify(addressMapper, times(1)).toDto(address);
    }

    @Test
    void updateAddress_WhenAddressDoesNotExist_ShouldThrowNotFoundException() {
        // Arrange
        when(addressRepository.findById(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> addressService.updateAddress(ADDRESS_ID, addressDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Address with id '" + ADDRESS_ID + "' not found");

        verify(addressRepository, times(1)).findById(ADDRESS_ID);
        verify(addressMapper, never()).updateEntity(any(AddressDto.class), any(Address.class));
        verify(addressRepository, never()).save(any(Address.class));
    }

    @Test
    void setAddressAsDefault_WhenAddressExists_ShouldSetDefaultAndClearOthers() {
        // Arrange
        Address otherAddress = Address.builder()
                .id("456")
                .customerId(CUSTOMER_ID)
                .isDefault(true)
                .build();
        
        address.setDefault(false);
        
        when(customerRepository.existsById(anyString())).thenReturn(true);
        when(addressRepository.findById(anyString())).thenReturn(Optional.of(address));
        when(addressRepository.findByCustomerIdAndIsDefault(anyString(), anyBoolean()))
                .thenReturn(Optional.of(otherAddress));
        when(addressRepository.save(any(Address.class))).thenReturn(address);
        when(addressMapper.toDto(any(Address.class))).thenReturn(addressDto);

        // Act
        AddressDto result = addressService.setAddressAsDefault(ADDRESS_ID, CUSTOMER_ID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.isDefault()).isTrue();
        verify(customerRepository, times(1)).existsById(CUSTOMER_ID);
        verify(addressRepository, times(1)).findById(ADDRESS_ID);
        verify(addressRepository, times(1)).findByCustomerIdAndIsDefault(CUSTOMER_ID, true);
        verify(addressRepository, times(2)).save(any(Address.class));
        verify(addressMapper, times(1)).toDto(address);
    }

    @Test
    void setAddressAsDefault_WhenAddressDoesNotBelongToCustomer_ShouldThrowNotFoundException() {
        // Arrange
        Address wrongAddress = Address.builder()
                .id(ADDRESS_ID)
                .customerId("wrong-customer-id")
                .build();
        
        when(customerRepository.existsById(anyString())).thenReturn(true);
        when(addressRepository.findById(anyString())).thenReturn(Optional.of(wrongAddress));

        // Act & Assert
        assertThatThrownBy(() -> addressService.setAddressAsDefault(ADDRESS_ID, CUSTOMER_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Address " + ADDRESS_ID + " does not belong to customer " + CUSTOMER_ID);

        verify(customerRepository, times(1)).existsById(CUSTOMER_ID);
        verify(addressRepository, times(1)).findById(ADDRESS_ID);
    }

    @Test
    void deleteAddress_WhenAddressExists_ShouldDeleteAddress() {
        // Arrange
        when(addressRepository.findById(anyString())).thenReturn(Optional.of(address));
        doNothing().when(addressRepository).deleteById(anyString());

        // Act
        addressService.deleteAddress(ADDRESS_ID);

        // Assert
        verify(addressRepository, times(1)).findById(ADDRESS_ID);
        verify(addressRepository, times(1)).deleteById(ADDRESS_ID);
    }

    @Test
    void deleteAddress_WhenAddressDoesNotExist_ShouldThrowNotFoundException() {
        // Arrange
        when(addressRepository.findById(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> addressService.deleteAddress(ADDRESS_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Address with id '" + ADDRESS_ID + "' not found");

        verify(addressRepository, times(1)).findById(ADDRESS_ID);
        verify(addressRepository, never()).deleteById(anyString());
    }

    @Test
    void findAddressesByCity_WhenCustomerExists_ShouldReturnMatchingAddresses() {
        // Arrange
        String city = "Anytown";
        List<Address> addresses = List.of(address);
        List<AddressDto> addressDtos = List.of(addressDto);

        when(customerRepository.existsById(anyString())).thenReturn(true);
        when(addressRepository.findByCustomerIdAndCity(anyString(), anyString())).thenReturn(addresses);
        when(addressMapper.toDtoList(any())).thenReturn(addressDtos);

        // Act
        List<AddressDto> result = addressService.findAddressesByCity(CUSTOMER_ID, city);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getCity()).isEqualTo(city);
        verify(customerRepository, times(1)).existsById(CUSTOMER_ID);
        verify(addressRepository, times(1)).findByCustomerIdAndCity(CUSTOMER_ID, city);
        verify(addressMapper, times(1)).toDtoList(addresses);
    }

    @Test
    void deleteAddressesByCustomerId_ShouldDeleteAllCustomerAddresses() {
        // Arrange
        doNothing().when(addressRepository).deleteByCustomerId(anyString());

        // Act
        addressService.deleteAddressesByCustomerId(CUSTOMER_ID);

        // Assert
        verify(addressRepository, times(1)).deleteByCustomerId(CUSTOMER_ID);
    }
}