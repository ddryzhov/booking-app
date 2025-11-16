package com.daniil.bookingapp.mapper;

import com.daniil.bookingapp.dto.accommodation.AccommodationRequestDto;
import com.daniil.bookingapp.dto.accommodation.AccommodationResponseDto;
import com.daniil.bookingapp.dto.accommodation.AccommodationUpdateRequestDto;
import com.daniil.bookingapp.model.Accommodation;
import java.util.ArrayList;
import java.util.List;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface AccommodationMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "bookings", ignore = true)
    @Mapping(target = "amenities", expression = "java(parseAmenities(dto.getAmenities()))")
    Accommodation toEntity(AccommodationRequestDto dto);

    @Mapping(target = "amenities",
            expression = "java(splitAmenities(accommodation.getAmenities()))")
    AccommodationResponseDto toDto(Accommodation accommodation);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "bookings", ignore = true)
    @Mapping(target = "amenities",
            expression = "java(dto.getAmenities() != null "
                    + "? parseAmenities(dto.getAmenities()) : "
                    + "accommodation.getAmenities())")
    void updateEntity(@MappingTarget Accommodation accommodation,
                      AccommodationUpdateRequestDto dto);

    default String parseAmenities(List<String> amenities) {
        if (amenities == null || amenities.isEmpty()) {
            return "";
        }
        return String.join(",", amenities);
    }

    default List<String> splitAmenities(String amenities) {
        if (amenities == null || amenities.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return List.of(amenities.split(","));
    }
}
