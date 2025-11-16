package com.daniil.bookingapp.service;

import com.daniil.bookingapp.dto.booking.BookingRequestDto;
import com.daniil.bookingapp.dto.booking.BookingResponseDto;
import com.daniil.bookingapp.dto.booking.BookingUpdateRequestDto;
import com.daniil.bookingapp.model.Booking;
import com.daniil.bookingapp.model.User;
import com.daniil.bookingapp.model.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookingService {
    BookingResponseDto create(BookingRequestDto requestDto, User user);

    Page<BookingResponseDto> findMyBookings(User user, Pageable pageable);

    Page<BookingResponseDto> findAllByFilters(
            Long userId,
            BookingStatus status,
            Pageable pageable
    );

    BookingResponseDto findById(Long id, User user);

    BookingResponseDto update(Long id, BookingUpdateRequestDto requestDto, User user);

    void cancel(Long id, User user);

    Booking getBookingById(Long id);
}
