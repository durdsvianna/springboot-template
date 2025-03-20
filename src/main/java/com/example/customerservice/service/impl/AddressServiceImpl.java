package com.example.customerservice.service.impl;

import com.example.customerservice.dto.AddressDto;
import com.example.customerservice.model.Address;
import com.example.customerservice.repository.AddressRepository;
import com.example.customerservice.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;

    @Override
    @Transactional
    public AddressDto createAddress(AddressDto addressDto) {
        Address address = toEntity(addressDto);
        
        // If this is set as default address, unset any existing default
        if (Boolean.TRUE.equals(address.getIsDefault())) {
            addressRepository.findByCustomerIdAndIsDefaultTrue(address.getCustomerId())
                    .ifPresent(existingDefault -> {
                        existingDefault.setIsDefault(false);
                        addressRepository.save(existingDefault);
                    });
        }
        
        Address savedAddress = addressRepository.save(address);
        return toDto(savedAddress);
    }

    @Override
    public AddressDto getAddressById(String id) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Address not found with id: " + id));
        
        return toDto(address);
    }

    @Override
    public List<AddressDto> getAddressesByCustomerId(String customerId) {
        List<Address> addresses = addressRepository.findByCustomerId(customerId);
        return addresses.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public AddressDto getDefaultAddress(String customerId) {
        return addressRepository.findByCustomerIdAndIsDefaultTrue(customerId)
                .map(this::toDto)
                .orElseThrow(() -> new NoSuchElementException("No default address found for customer id: " + customerId));
    }

    @Override
    public List<AddressDto> searchAddresses(String city, String state, String zipCode) {
        if (city != null) {
            return addressRepository.findByCity(city).stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());
        } else if (state != null) {
            return addressRepository.findByState(state).stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());
        } else if (zipCode != null) {
            return addressRepository.findByZipCode(zipCode).stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());
        }
        
        return Collections.emptyList();
    }

    @Override
    @Transactional
    public AddressDto updateAddress(String id, AddressDto addressDto) {
        Address existingAddress = addressRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Address not found with id: " + id));
        
        // If setting as default, unset any existing default
        if (Boolean.TRUE.equals(addressDto.getIsDefault()) && 
                (existingAddress.getIsDefault() == null || !existingAddress.getIsDefault())) {
            addressRepository.findByCustomerIdAndIsDefaultTrue(existingAddress.getCustomerId())
                    .ifPresent(defaultAddress -> {
                        defaultAddress.setIsDefault(false);
                        addressRepository.save(defaultAddress);
                    });
        }
        
        existingAddress.setStreet(addressDto.getStreet());
        existingAddress.setCity(addressDto.getCity());
        existingAddress.setState(addressDto.getState());
        existingAddress.setZipCode(addressDto.getZipCode());
        existingAddress.setCountry(addressDto.getCountry());
        existingAddress.setIsDefault(addressDto.getIsDefault());
        
        Address updatedAddress = addressRepository.save(existingAddress);
        return toDto(updatedAddress);
    }

    @Override
    @Transactional
    public void deleteAddress(String id) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Address not found with id: " + id));
        
        addressRepository.delete(address);
    }

    @Override
    @Transactional
    public void deleteCustomerAddresses(String customerId) {
        List<Address> addresses = addressRepository.findByCustomerId(customerId);
        addressRepository.deleteAll(addresses);
    }

    @Override
    @Transactional
    public AddressDto setDefaultAddress(String addressId, String customerId) {
        Address newDefaultAddress = addressRepository.findById(addressId)
                .orElseThrow(() -> new NoSuchElementException("Address not found with id: " + addressId));
        
        if (!customerId.equals(newDefaultAddress.getCustomerId())) {
            throw new IllegalArgumentException("Address does not belong to the specified customer");
        }
        
        // Reset existing default
        Optional<Address> existingDefault = addressRepository.findByCustomerIdAndIsDefaultTrue(customerId);
        if (existingDefault.isPresent() && !existingDefault.get().getId().equals(addressId)) {
            Address oldDefault = existingDefault.get();
            oldDefault.setIsDefault(false);
            addressRepository.save(oldDefault);
        }
        
        // Set new default
        newDefaultAddress.setIsDefault(true);
        Address updatedAddress = addressRepository.save(newDefaultAddress);
        
        return toDto(updatedAddress);
    }

    private Address toEntity(AddressDto dto) {
        return Address.builder()
                .id(dto.getId())
                .street(dto.getStreet())
                .city(dto.getCity())
                .state(dto.getState())
                .zipCode(dto.getZipCode())
                .country(dto.getCountry())
                .isDefault(dto.getIsDefault())
                .customerId(dto.getCustomerId())
                .build();
    }

    private AddressDto toDto(Address entity) {
        return AddressDto.builder()
                .id(entity.getId())
                .street(entity.getStreet())
                .city(entity.getCity())
                .state(entity.getState())
                .zipCode(entity.getZipCode())
                .country(entity.getCountry())
                .isDefault(entity.getIsDefault())
                .customerId(entity.getCustomerId())
                .build();
    }
} 