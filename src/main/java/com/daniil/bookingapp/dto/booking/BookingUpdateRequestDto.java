package com.daniil.bookingapp.dto.booking;

import com.daniil.bookingapp.model.enums.BookingStatus;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingUpdateRequestDto {
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private BookingStatus status;
}
