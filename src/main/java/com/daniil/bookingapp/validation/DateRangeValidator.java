package com.daniil.bookingapp.validation;

import com.daniil.bookingapp.dto.booking.BookingRequestDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DateRangeValidator implements ConstraintValidator<ValidDateRange, BookingRequestDto> {
    @Override
    public boolean isValid(BookingRequestDto dto, ConstraintValidatorContext context) {
        if (dto.getCheckInDate() == null || dto.getCheckOutDate() == null) {
            return true;
        }
        return dto.getCheckOutDate().isAfter(dto.getCheckInDate());
    }
}
