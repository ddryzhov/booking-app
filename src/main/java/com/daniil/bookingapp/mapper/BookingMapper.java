package com.daniil.bookingapp.mapper;

import com.daniil.bookingapp.dto.booking.BookingRequestDto;
import com.daniil.bookingapp.dto.booking.BookingResponseDto;
import com.daniil.bookingapp.dto.booking.BookingUpdateRequestDto;
import com.daniil.bookingapp.model.Accommodation;
import com.daniil.bookingapp.model.Booking;
import com.daniil.bookingapp.model.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface BookingMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", source = "user")
    @Mapping(target = "accommodation", source = "accommodation")
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "totalPrice", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "payment", ignore = true)
    @Mapping(target = "checkInDate", source = "dto.checkInDate")
    @Mapping(target = "checkOutDate", source = "dto.checkOutDate")
    Booking toEntity(BookingRequestDto dto, User user, Accommodation accommodation);

    @Mapping(target = "accommodationId", source = "accommodation.id")
    @Mapping(target = "accommodationType", source = "accommodation.type")
    @Mapping(target = "accommodationLocation", source = "accommodation.location")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userEmail", source = "user.email")
    BookingResponseDto toDto(Booking booking);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "accommodation", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "payment", ignore = true)
    @Mapping(target = "totalPrice", ignore = true)
    void updateEntity(@MappingTarget Booking booking, BookingUpdateRequestDto dto);
}
