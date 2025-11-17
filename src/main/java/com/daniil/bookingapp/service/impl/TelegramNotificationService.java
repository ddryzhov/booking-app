package com.daniil.bookingapp.service.impl;

import com.daniil.bookingapp.model.Accommodation;
import com.daniil.bookingapp.model.Booking;
import com.daniil.bookingapp.model.Payment;
import com.daniil.bookingapp.service.NotificationService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Slf4j
@Service
public class TelegramNotificationService extends TelegramLongPollingBot
        implements NotificationService {

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Value("${telegram.chat.id}")
    private String chatId;

    @PostConstruct
    public void init() {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(this);
            log.info("Telegram bot successfully registered: {}", botUsername);
        } catch (TelegramApiException e) {
            log.error("Failed to register Telegram bot: {}", e.getMessage(), e);
        }
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            Long userId = update.getMessage().getChatId();

            log.debug("Received message from user {}: {}", userId, messageText);

            if ("/start".equals(messageText)) {
                sendMessageToUser(userId, "Welcome to Booking App Notifications!\n\n"
                        + "Your Chat ID: " + userId + "\n\n"
                        + "Use this Chat ID in your .env file to receive notifications.");
            }
        }
    }

    @Override
    public void sendBookingCreatedNotification(Booking booking) {
        String message = String.format(
                "*New Booking Created*\n\n"
                        + "Booking ID: `%d`\n"
                        + "User: %s (%s)\n"
                        + "Accommodation: %s\n"
                        + "Location: %s\n"
                        + "Check-in: %s\n"
                        + "Check-out: %s\n"
                        + "Total Price: $%.2f\n"
                        + "Status: %s",
                booking.getId(),
                booking.getUser().getFullName(),
                booking.getUser().getEmail(),
                booking.getAccommodation().getType(),
                booking.getAccommodation().getLocation(),
                booking.getCheckInDate(),
                booking.getCheckOutDate(),
                booking.getTotalPrice(),
                booking.getStatus()
        );

        sendNotification(message);
    }

    @Override
    public void sendBookingCancelledNotification(Booking booking) {
        String message = String.format(
                "*Booking Cancelled*\n\n"
                        + "Booking ID: `%d`\n"
                        + "User: %s\n"
                        + "Accommodation: %s at %s\n"
                        + "Dates: %s - %s\n"
                        + "Amount: $%.2f\n\n"
                        + "Accommodation availability increased",
                booking.getId(),
                booking.getUser().getFullName(),
                booking.getAccommodation().getType(),
                booking.getAccommodation().getLocation(),
                booking.getCheckInDate(),
                booking.getCheckOutDate(),
                booking.getTotalPrice()
        );

        sendNotification(message);
    }

    @Override
    public void sendAccommodationCreatedNotification(Accommodation accommodation) {
        String message = String.format(
                "*New Accommodation Added*\n\n"
                        + "ID: `%d`\n"
                        + "Type: %s\n"
                        + "Location: %s\n"
                        + "Size: %s\n"
                        + "Daily Rate: $%.2f\n"
                        + "Availability: %d units\n"
                        + "Amenities: %s",
                accommodation.getId(),
                accommodation.getType(),
                accommodation.getLocation(),
                accommodation.getSize(),
                accommodation.getDailyRate(),
                accommodation.getAvailability(),
                accommodation.getAmenities().isEmpty()
                        ? "None" : accommodation.getAmenities()
        );

        sendNotification(message);
    }

    @Override
    public void sendAccommodationReleasedNotification(Accommodation accommodation) {
        String message = String.format(
                "*Accommodation Released*\n\n"
                        + "ID: `%d`\n"
                        + "Type: %s\n"
                        + "Location: %s\n"
                        + "New Availability: %d units\n\n"
                        + "This accommodation is now available for booking!",
                accommodation.getId(),
                accommodation.getType(),
                accommodation.getLocation(),
                accommodation.getAvailability()
        );

        sendNotification(message);
    }

    @Override
    public void sendPaymentSuccessNotification(Payment payment) {
        String message = String.format(
                "*Payment Successful*\n\n"
                        + "Payment ID: `%d`\n"
                        + "Booking ID: `%d`\n"
                        + "User: %s (%s)\n"
                        + "Amount Paid: $%.2f\n"
                        + "Paid At: %s\n"
                        + "Booking Status: CONFIRMED",
                payment.getId(),
                payment.getBooking().getId(),
                payment.getUser().getFullName(),
                payment.getUser().getEmail(),
                payment.getAmountToPay(),
                payment.getPaidAt()
        );

        sendNotification(message);
    }

    @Override
    public void sendPaymentCreatedNotification(Payment payment) {
        String message = String.format(
                "*Payment Session Created*\n\n"
                        + "Payment ID: `%d`\n"
                        + "Booking ID: `%d`\n"
                        + "User: %s\n"
                        + "Amount: $%.2f\n"
                        + "Expires At: %s\n"
                        + "Status: PENDING",
                payment.getId(),
                payment.getBooking().getId(),
                payment.getUser().getFullName(),
                payment.getAmountToPay(),
                payment.getExpiresAt()
        );

        sendNotification(message);
    }

    @Override
    public void sendExpiredBookingsNotification(int count) {
        String message = String.format(
                "*Expired Bookings Report*\n\n"
                        + "Found %d expired booking%s today.\n"
                        + "All expired bookings have been marked as EXPIRED.",
                count,
                count == 1 ? "" : "s"
        );

        sendNotification(message);
    }

    @Override
    public void sendNoExpiredBookingsNotification() {
        String message = "*No expired bookings today!*";
        sendNotification(message);
    }

    private void sendNotification(String message) {
        try {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId);
            sendMessage.setText(message);
            sendMessage.setParseMode("Markdown");

            execute(sendMessage);
            log.debug("Notification sent successfully");
        } catch (TelegramApiException e) {
            log.error("Failed to send Telegram notification: {}", e.getMessage(), e);
        }
    }

    private void sendMessageToUser(Long userId, String message) {
        try {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(userId.toString());
            sendMessage.setText(message);
            sendMessage.setParseMode("Markdown");

            execute(sendMessage);
            log.debug("Message sent to user {}", userId);
        } catch (TelegramApiException e) {
            log.error("Failed to send message to user {}: {}", userId, e.getMessage(), e);
        }
    }
}
