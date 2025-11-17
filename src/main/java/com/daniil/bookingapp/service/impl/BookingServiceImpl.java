package com.daniil.bookingapp.service.impl;

import com.daniil.bookingapp.dto.booking.BookingRequestDto;
import com.daniil.bookingapp.dto.booking.BookingResponseDto;
import com.daniil.bookingapp.dto.booking.BookingUpdateRequestDto;
import com.daniil.bookingapp.exception.BookingException;
import com.daniil.bookingapp.exception.BookingNotAvailableException;
import com.daniil.bookingapp.exception.BookingOverlapException;
import com.daniil.bookingapp.exception.EntityNotFoundException;
import com.daniil.bookingapp.exception.PendingPaymentException;
import com.daniil.bookingapp.mapper.BookingMapper;
import com.daniil.bookingapp.model.Accommodation;
import com.daniil.bookingapp.model.Booking;
import com.daniil.bookingapp.model.User;
import com.daniil.bookingapp.model.enums.BookingStatus;
import com.daniil.bookingapp.model.enums.RoleName;
import com.daniil.bookingapp.repository.BookingRepository;
import com.daniil.bookingapp.service.AccommodationService;
import com.daniil.bookingapp.service.BookingService;
import com.daniil.bookingapp.service.NotificationService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final AccommodationService accommodationService;
    private final NotificationService notificationService;
    private final BookingMapper bookingMapper;

    @Override
    @Transactional
    public BookingResponseDto create(BookingRequestDto requestDto, User user) {
        long pendingBookings = bookingRepository.countPendingBookingsByUserId(user.getId());
        if (pendingBookings > 0) {
            throw new PendingPaymentException(
                    "Cannot create booking. You have " + pendingBookings
                            + " pending booking(s). Please complete payment first"
            );
        }

        validateDates(requestDto.getCheckInDate(), requestDto.getCheckOutDate());

        Accommodation accommodation = accommodationService
                .getAccommodationById(requestDto.getAccommodationId());

        if (!accommodation.isAvailable()) {
            throw new BookingNotAvailableException(
                    "Accommodation is not available (current availability: "
                            + accommodation.getAvailability() + ")"
            );
        }

        List<Booking> overlapping = bookingRepository.findOverlappingBookings(
                accommodation.getId(),
                requestDto.getCheckInDate(),
                requestDto.getCheckOutDate()
        );

        if (!overlapping.isEmpty()) {
            throw new BookingOverlapException(
                    "Accommodation is already booked for the selected dates. "
                            + "Found " + overlapping.size() + " conflicting booking(s)"
            );
        }

        Booking booking = bookingMapper.toEntity(requestDto, user, accommodation);
        booking.setTotalPrice(booking.calculateTotalPrice());
        accommodation.decreaseAvailability();
        Booking saved = bookingRepository.save(booking);

        notificationService.sendBookingCreatedNotification(saved);

        return bookingMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponseDto> findMyBookings(User user, Pageable pageable) {
        return bookingRepository.findAllByUserId(user.getId(), pageable)
                .map(bookingMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponseDto> findAllByFilters(
            Long userId,
            BookingStatus status,
            Pageable pageable
    ) {
        if (userId != null && status != null) {
            return bookingRepository.findAllByUserIdAndStatus(userId, status, pageable)
                    .map(bookingMapper::toDto);
        } else if (userId != null) {
            return bookingRepository.findAllByUserId(userId, pageable)
                    .map(bookingMapper::toDto);
        } else if (status != null) {
            return bookingRepository.findAllByStatus(status, pageable)
                    .map(bookingMapper::toDto);
        } else {
            return bookingRepository.findAll(pageable)
                    .map(bookingMapper::toDto);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponseDto findById(Long id, User user) {
        Booking booking = getBookingById(id);

        if (!hasAccessToBooking(booking, user)) {
            throw new BookingException("Access denied to this booking");
        }

        return bookingMapper.toDto(booking);
    }

    @Override
    @Transactional
    public BookingResponseDto update(Long id, BookingUpdateRequestDto requestDto, User user) {
        Booking booking = getBookingById(id);

        if (!hasAccessToBooking(booking, user)) {
            throw new BookingException("Access denied to this booking");
        }

        if (requestDto.getStatus() != null) {
            validateStatusChange(booking, requestDto.getStatus());
        }

        if (requestDto.getCheckInDate() != null || requestDto.getCheckOutDate() != null) {
            LocalDate newCheckIn = requestDto.getCheckInDate() != null
                    ? requestDto.getCheckInDate() : booking.getCheckInDate();
            LocalDate newCheckOut = requestDto.getCheckOutDate() != null
                    ? requestDto.getCheckOutDate() : booking.getCheckOutDate();

            validateDates(newCheckIn, newCheckOut);

            List<Booking> overlapping = bookingRepository.findOverlappingBookings(
                    booking.getAccommodation().getId(),
                    newCheckIn,
                    newCheckOut
            );
            overlapping.removeIf(b -> b.getId().equals(booking.getId()));

            if (!overlapping.isEmpty()) {
                throw new BookingOverlapException(
                        "Selected dates conflict with existing booking(s)"
                );
            }
        }

        bookingMapper.updateEntity(booking, requestDto);
        Booking updated = bookingRepository.save(booking);

        return bookingMapper.toDto(updated);
    }

    @Override
    @Transactional
    public void cancel(Long id, User user) {
        Booking booking = getBookingById(id);

        if (!booking.getUser().getId().equals(user.getId())) {
            throw new BookingException("Only booking owner can cancel it");
        }

        if (!booking.canBeCancelled()) {
            throw new BookingException(
                    "Booking cannot be cancelled. Status: " + booking.getStatus()
                            + ", Check-in date: " + booking.getCheckInDate()
            );
        }

        booking.cancel();
        booking.getAccommodation().increaseAvailability();
        bookingRepository.save(booking);

        notificationService.sendBookingCancelledNotification(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public Booking getBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Booking not found with id: " + id
                ));
    }

    private void validateDates(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn.isBefore(LocalDate.now())) {
            throw new BookingException("Check-in date cannot be in the past");
        }

        if (!checkOut.isAfter(checkIn)) {
            throw new BookingException("Check-out date must be after check-in date");
        }
    }

    private void validateStatusChange(Booking booking, BookingStatus newStatus) {
        if (booking.getStatus() == newStatus) {
            throw new BookingException("Booking already has status: " + newStatus);
        }

        if (booking.getStatus() == BookingStatus.CANCELED) {
            throw new BookingException("Cannot change status of cancelled booking");
        }

        if (booking.getStatus() == BookingStatus.EXPIRED) {
            throw new BookingException("Cannot change status of expired booking");
        }
    }

    private boolean hasAccessToBooking(Booking booking, User user) {
        if (booking.getUser().getId().equals(user.getId())) {
            return true;
        }

        return user.getRoles().stream()
                .anyMatch(role -> role.getName() == RoleName.ROLE_ADMIN
                        || role.getName() == RoleName.ROLE_MANAGER);
    }
}
