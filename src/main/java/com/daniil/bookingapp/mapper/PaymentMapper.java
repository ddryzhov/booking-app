package com.daniil.bookingapp.mapper;

import com.daniil.bookingapp.dto.payment.PaymentResponseDto;
import com.daniil.bookingapp.model.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
    @Mapping(target = "bookingId", source = "booking.id")
    @Mapping(target = "userId", source = "user.id")
    PaymentResponseDto toDto(Payment payment);
}
