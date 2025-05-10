package com.example.customerservice.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.customerservice.dto.AddressDto;
import com.example.customerservice.exception.NotFoundException;
import com.example.customerservice.mapper.AddressMapper;
import com.example.customerservice.model.Address;
import com.example.customerservice.repository.AddressRepository;
import com.example.customerservice.repository.CustomerRepository;
import com.example.customerservice.service.AddressService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final CustomerRepository customerRepository;
    private final AddressMapper addressMapper;

    @Override
    @Transactional
    public AddressDto createAddress(String customerId, AddressDto addressDto) {
        log.debug("Creating address for customer ID: {}", customerId);
        
        if (!customerRepository.existsById(customerId)) {
            throw new NotFoundException("Customer", customerId);
        }
        
        Address address = addressMapper.toEntity(addressDto);
        address.setCustomerId(customerId);
        
        // If this is the first address or is marked as default, handle default flag
        if (address.isDefault() || addressRepository.findByCustomerId(customerId).isEmpty()) {
            clearDefaultAddressForCustomer(customerId);
            address.setDefault(true);
        }
        
        Address savedAddress = addressRepository.save(address);
        
        return addressMapper.toDto(savedAddress);
    }

    @Override
    @Transactional(readOnly = true)
    public AddressDto getAddressById(String id) {
        log.debug("Getting address with ID: {}", id);
        
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Address", id));
        
        return addressMapper.toDto(address);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddressDto> getAddressesByCustomerId(String customerId) {
        log.debug("Getting addresses for customer ID: {}", customerId);
        
        if (!customerRepository.existsById(customerId)) {
            throw new NotFoundException("Customer", customerId);
        }
        
        List<Address> addresses = addressRepository.findByCustomerId(customerId);
        
        return addressMapper.toDtoList(addresses);
    }

    @Override
    @Transactional(readOnly = true)
    public AddressDto getDefaultAddress(String customerId) {
        log.debug("Getting default address for customer ID: {}", customerId);
        
        if (!customerRepository.existsById(customerId)) {
            throw new NotFoundException("Customer", customerId);
        }
        
        Address address = addressRepository.findByCustomerIdAndIsDefault(customerId, true)
                .orElseThrow(() -> new NotFoundException("Default address for customer", customerId));
        
        return addressMapper.toDto(address);
    }

    @Override
    @Transactional
    public AddressDto updateAddress(String id, AddressDto addressDto) {
        log.debug("Updating address with ID: {}", id);
        
        Address existingAddress = addressRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Address", id));
        
        boolean wasDefault = existingAddress.isDefault();
        boolean willBeDefault = addressDto.isDefault();
        
        Address updatedAddress = addressMapper.updateEntity(addressDto, existingAddress);
        
        // Handle the default flag logic
        if (!wasDefault && willBeDefault) {
            clearDefaultAddressForCustomer(existingAddress.getCustomerId());
            updatedAddress.setDefault(true);
        } else if (wasDefault && !willBeDefault) {
            // If this was the default and now it's not, ensure at least one address is default
            List<Address> otherAddresses = addressRepository.findByCustomerId(existingAddress.getCustomerId());
            if (otherAddresses.size() > 1) {
                updatedAddress.setDefault(false);
                // Set another address as default if needed
                boolean anyDefault = otherAddresses.stream()
                        .filter(a -> !a.getId().equals(id))
                        .anyMatch(Address::isDefault);
                
                if (!anyDefault) {
                    // Set first other address as default
                    Address newDefault = otherAddresses.stream()
                            .filter(a -> !a.getId().equals(id))
                            .findFirst()
                            .orElse(null);
                    
                    if (newDefault != null) {
                        newDefault.setDefault(true);
                        addressRepository.save(newDefault);
                    }
                }
            } else {
                // If this is the only address, keep it as default
                updatedAddress.setDefault(true);
            }
        }
        
        Address savedAddress = addressRepository.save(updatedAddress);
        
        return addressMapper.toDto(savedAddress);
    }

    @Override
    @Transactional
    public AddressDto setAddressAsDefault(String addressId, String customerId) {
        log.debug("Setting address {} as default for customer ID: {}", addressId, customerId);
        
        if (!customerRepository.existsById(customerId)) {
            throw new NotFoundException("Customer", customerId);
        }
        
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new NotFoundException("Address", addressId));
        
        if (!address.getCustomerId().equals(customerId)) {
            throw new NotFoundException(
                    String.format("Address %s does not belong to customer %s", addressId, customerId));
        }
        
        // Clear current default
        clearDefaultAddressForCustomer(customerId);
        
        // Set as default
        address.setDefault(true);
        Address savedAddress = addressRepository.save(address);
        
        return addressMapper.toDto(savedAddress);
    }

    @Override
    @Transactional
    public void deleteAddress(String id) {
        log.debug("Deleting address with ID: {}", id);
        
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Address", id));
        
        // If this is a default address, we may need to set another as default
        if (address.isDefault()) {
            List<Address> otherAddresses = addressRepository.findByCustomerId(address.getCustomerId());
            if (otherAddresses.size() > 1) {
                // Find another address to set as default
                Address newDefault = otherAddresses.stream()
                        .filter(a -> !a.getId().equals(id))
                        .findFirst()
                        .orElse(null);
                
                if (newDefault != null) {
                    newDefault.setDefault(true);
                    addressRepository.save(newDefault);
                }
            }
        }
        
        addressRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddressDto> findAddressesByCity(String customerId, String city) {
        log.debug("Finding addresses in city: {} for customer ID: {}", city, customerId);
        
        if (!customerRepository.existsById(customerId)) {
            throw new NotFoundException("Customer", customerId);
        }
        
        List<Address> addresses = addressRepository.findByCustomerIdAndCity(customerId, city);
        
        return addressMapper.toDtoList(addresses);
    }

    @Override
    @Transactional
    public void deleteAddressesByCustomerId(String customerId) {
        log.debug("Deleting all addresses for customer ID: {}", customerId);
        
        addressRepository.deleteByCustomerId(customerId);
    }
    
    /**
     * Helper method to clear any existing default addresses for a customer
     * 
     * @param customerId The customer ID
     */
    private void clearDefaultAddressForCustomer(String customerId) {
        Optional<Address> currentDefault = addressRepository.findByCustomerIdAndIsDefault(customerId, true);
        
        if (currentDefault.isPresent()) {
            Address address = currentDefault.get();
            address.setDefault(false);
            addressRepository.save(address);
        }
    }
}