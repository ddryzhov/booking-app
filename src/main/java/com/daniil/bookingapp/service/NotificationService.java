package com.daniil.bookingapp.service;

import com.daniil.bookingapp.model.Accommodation;
import com.daniil.bookingapp.model.Booking;
import com.daniil.bookingapp.model.Payment;

public interface NotificationService {
    void sendBookingCreatedNotification(Booking booking);

    void sendBookingCancelledNotification(Booking booking);

    void sendAccommodationCreatedNotification(Accommodation accommodation);

    void sendAccommodationReleasedNotification(Accommodation accommodation);

    void sendPaymentSuccessNotification(Payment payment);

    void sendPaymentCreatedNotification(Payment payment);

    void sendExpiredBookingsNotification(int count);

    void sendNoExpiredBookingsNotification();
}
