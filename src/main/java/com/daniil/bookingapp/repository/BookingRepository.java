package com.daniil.bookingapp.repository;

import com.daniil.bookingapp.model.Booking;
import com.daniil.bookingapp.model.enums.BookingStatus;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    String ACTIVE_STATUSES =
            "com.daniil.bookingapp.model.enums.BookingStatus.PENDING, "
                    + "com.daniil.bookingapp.model.enums.BookingStatus.CONFIRMED";

    Page<Booking> findAllByUserId(Long userId, Pageable pageable);

    Page<Booking> findAllByUserIdAndStatus(Long userId, BookingStatus status, Pageable pageable);

    Page<Booking> findAllByStatus(BookingStatus status, Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE "
            + "b.accommodation.id = :accommodationId AND "
            + "b.status IN (" + ACTIVE_STATUSES + ") AND "
            + "b.checkInDate < :checkOut AND "
            + "b.checkOutDate > :checkIn")
    List<Booking> findOverlappingBookings(
            @Param("accommodationId") Long accommodationId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut
    );

    @Query("SELECT COUNT(b) FROM Booking b WHERE "
            + "b.user.id = :userId AND "
            + "b.status = 'PENDING'")
    long countPendingBookingsByUserId(@Param("userId") Long userId);
}
