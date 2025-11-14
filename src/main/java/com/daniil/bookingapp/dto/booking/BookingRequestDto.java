package com.daniil.bookingapp.dto.booking;

import com.daniil.bookingapp.validation.ValidDateRange;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ValidDateRange
public class BookingRequestDto {

    @NotNull(message = "Accommodation ID cannot be null")
    private Long accommodationId;

    @NotNull(message = "Check-in date cannot be null")
    @Future(message = "Check-in date must be in the future")
    private LocalDate checkInDate;

    @NotNull(message = "Check-out date cannot be null")
    @Future(message = "Check-out date must be in the future")
    private LocalDate checkOutDate;
}
