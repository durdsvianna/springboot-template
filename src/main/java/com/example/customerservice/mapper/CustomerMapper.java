package com.example.customerservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import com.example.customerservice.dto.CustomerDto;
import com.example.customerservice.model.Customer;

import java.util.List;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = AddressMapper.class
)
public interface CustomerMapper {

    /**
     * Map from entity to DTO
     *
     * @param customer The customer entity
     * @return The customer DTO
     */
    CustomerDto toDto(Customer customer);

    /**
     * Map from DTO to entity
     *
     * @param customerDto The customer DTO
     * @return The customer entity
     */
    @Mapping(target = "addresses", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    Customer toEntity(CustomerDto customerDto);

    /**
     * Update entity from DTO
     *
     * @param customerDto The customer DTO with updates
     * @param customer The customer entity to update
     * @return The updated customer entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "addresses", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    Customer updateEntity(CustomerDto customerDto, @MappingTarget Customer customer);

    /**
     * Map from list of entities to list of DTOs
     *
     * @param customers List of customer entities
     * @return List of customer DTOs
     */
    List<CustomerDto> toDtoList(List<Customer> customers);
}