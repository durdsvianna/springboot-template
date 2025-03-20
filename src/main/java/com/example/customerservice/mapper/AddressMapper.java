package com.example.customerservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import com.example.customerservice.dto.AddressDto;
import com.example.customerservice.model.Address;

import java.util.List;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface AddressMapper {

    /**
     * Map from entity to DTO
     *
     * @param address The address entity
     * @return The address DTO
     */
    AddressDto toDto(Address address);

    /**
     * Map from DTO to entity
     *
     * @param addressDto The address DTO
     * @return The address entity
     */
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    Address toEntity(AddressDto addressDto);

    /**
     * Update entity from DTO
     *
     * @param addressDto The address DTO with updates
     * @param address The address entity to update
     * @return The updated address entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customerId", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    Address updateEntity(AddressDto addressDto, @MappingTarget Address address);

    /**
     * Map from list of entities to list of DTOs
     *
     * @param addresses List of address entities
     * @return List of address DTOs
     */
    List<AddressDto> toDtoList(List<Address> addresses);
}