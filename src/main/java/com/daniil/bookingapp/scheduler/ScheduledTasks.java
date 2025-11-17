package com.daniil.bookingapp.scheduler;

import com.daniil.bookingapp.model.Booking;
import com.daniil.bookingapp.model.enums.BookingStatus;
import com.daniil.bookingapp.repository.BookingRepository;
import com.daniil.bookingapp.service.NotificationService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledTasks {
    private final BookingRepository bookingRepository;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 9 * * *")
    @Transactional
    public void checkExpiredBookings() {
        log.info("Starting daily expired bookings check");

        LocalDate today = LocalDate.now();

        List<Booking> expiredBookings = bookingRepository.findAll().stream()
                .filter(booking -> (booking.getStatus() == BookingStatus.CONFIRMED
                        || booking.getStatus() == BookingStatus.PENDING)
                        && booking.getCheckOutDate().isBefore(today))
                .toList();

        if (expiredBookings.isEmpty()) {
            log.info("No expired bookings found");
            notificationService.sendNoExpiredBookingsNotification();
            return;
        }

        log.info("Found {} expired bookings", expiredBookings.size());

        for (Booking booking : expiredBookings) {
            try {
                booking.expire();
                booking.getAccommodation().increaseAvailability();
                bookingRepository.save(booking);

                notificationService.sendAccommodationReleasedNotification(
                        booking.getAccommodation()
                );

                log.info("Marked booking {} as expired and released accommodation {}",
                        booking.getId(), booking.getAccommodation().getId());
            } catch (Exception e) {
                log.error("Failed to process expired booking {}: {}",
                        booking.getId(), e.getMessage(), e);
            }
        }

        notificationService.sendExpiredBookingsNotification(expiredBookings.size());
        log.info("Completed expired bookings check. Processed {} bookings",
                expiredBookings.size());
    }
}
