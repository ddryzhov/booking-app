package com.daniil.bookingapp.service.impl;

import com.daniil.bookingapp.dto.payment.PaymentCancelResponseDto;
import com.daniil.bookingapp.dto.payment.PaymentRequestDto;
import com.daniil.bookingapp.dto.payment.PaymentResponseDto;
import com.daniil.bookingapp.dto.payment.PaymentSuccessResponseDto;
import com.daniil.bookingapp.exception.BookingException;
import com.daniil.bookingapp.exception.EntityNotFoundException;
import com.daniil.bookingapp.mapper.PaymentMapper;
import com.daniil.bookingapp.model.Booking;
import com.daniil.bookingapp.model.Payment;
import com.daniil.bookingapp.model.User;
import com.daniil.bookingapp.model.enums.BookingStatus;
import com.daniil.bookingapp.model.enums.PaymentStatus;
import com.daniil.bookingapp.model.enums.RoleName;
import com.daniil.bookingapp.repository.PaymentRepository;
import com.daniil.bookingapp.service.BookingService;
import com.daniil.bookingapp.service.NotificationService;
import com.daniil.bookingapp.service.PaymentService;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private static final String PAYMENT_CURRENCY = "usd";
    private static final long SESSION_EXPIRATION_HOURS = 23;
    private static final int STRIPE_AMOUNT_MULTIPLIER = 100;

    private final PaymentRepository paymentRepository;
    private final BookingService bookingService;
    private final NotificationService notificationService;
    private final PaymentMapper paymentMapper;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentResponseDto> getPayments(Long userId, User user, Pageable pageable) {
        boolean isManagerOrAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getName() == RoleName.ROLE_ADMIN
                        || role.getName() == RoleName.ROLE_MANAGER);

        if (isManagerOrAdmin) {
            if (userId != null) {
                return paymentRepository.findAllByUserId(userId, pageable)
                        .map(paymentMapper::toDto);
            }
            return paymentRepository.findAll(pageable)
                    .map(paymentMapper::toDto);
        }

        return paymentRepository.findAllByUserId(user.getId(), pageable)
                .map(paymentMapper::toDto);
    }

    @Override
    @Transactional
    public PaymentResponseDto createPaymentSession(PaymentRequestDto requestDto, User user) {
        Booking booking = bookingService.getBookingById(requestDto.getBookingId());

        if (!booking.getUser().getId().equals(user.getId())) {
            throw new BookingException("You can only create payments for your own bookings");
        }

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BookingException(
                    "Payment can only be created for pending bookings. "
                            + "Current status: " + booking.getStatus()
            );
        }

        if (booking.getPayment() != null) {
            Payment existingPayment = booking.getPayment();
            if (existingPayment.getStatus() == PaymentStatus.PAID) {
                throw new BookingException("Booking already has a paid payment");
            }
            if (existingPayment.getStatus() == PaymentStatus.PENDING
                    && !existingPayment.isExpired()) {
                throw new BookingException(
                        "Booking already has an active payment session. "
                                + "Please use the existing session or wait for it to expire"
                );
            }
        }

        try {
            Session stripeSession = createStripeSession(booking);

            Payment payment = Payment.builder()
                    .booking(booking)
                    .user(user)
                    .amountToPay(booking.getTotalPrice())
                    .sessionUrl(stripeSession.getUrl())
                    .sessionId(stripeSession.getId())
                    .status(PaymentStatus.PENDING)
                    .expiresAt(LocalDateTime.now().plusHours(SESSION_EXPIRATION_HOURS))
                    .build();

            Payment saved = paymentRepository.save(payment);

            notificationService.sendPaymentCreatedNotification(saved);

            return paymentMapper.toDto(saved);
        } catch (StripeException e) {
            log.error("Failed to create Stripe session for booking {}: {}",
                    booking.getId(), e.getMessage(), e);
            throw new BookingException("Failed to create payment session: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public PaymentSuccessResponseDto handleSuccessPayment(String sessionId) {
        Payment payment = paymentRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Payment not found for session: " + sessionId
                ));

        if (payment.getStatus() == PaymentStatus.PAID) {
            throw new BookingException("Payment already processed");
        }

        try {
            Session session = Session.retrieve(sessionId);

            if (!"complete".equals(session.getStatus())
                    || !"paid".equals(session.getPaymentStatus())) {
                throw new BookingException("Payment was not completed successfully");
            }

            payment.markAsPaid();
            payment.setStripePaymentIntentId(session.getPaymentIntent());

            Booking booking = payment.getBooking();
            booking.confirm();

            paymentRepository.save(payment);

            notificationService.sendPaymentSuccessNotification(payment);

            return PaymentSuccessResponseDto.builder()
                    .paymentId(payment.getId())
                    .bookingId(booking.getId())
                    .amountPaid(payment.getAmountToPay())
                    .paidAt(payment.getPaidAt())
                    .build();

        } catch (StripeException e) {
            log.error("Failed to verify Stripe session {}: {}",
                    sessionId, e.getMessage(), e);
            throw new BookingException("Failed to verify payment: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public PaymentCancelResponseDto handleCancelPayment(String sessionId) {
        Payment payment = paymentRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Payment not found for session: " + sessionId
                ));

        String renewUrl = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path("/api/payments/renew")
                .queryParam("paymentId", payment.getId())
                .toUriString();

        return PaymentCancelResponseDto.builder()
                .paymentId(payment.getId())
                .renewUrl(renewUrl)
                .build();
    }

    @Override
    @Transactional
    public PaymentResponseDto renewPaymentSession(Long paymentId, User user) {
        Payment payment = getPaymentById(paymentId);

        if (!payment.getUser().getId().equals(user.getId())) {
            throw new BookingException("You can only renew your own payments");
        }

        if (payment.getStatus() != PaymentStatus.EXPIRED
                && payment.getStatus() != PaymentStatus.CANCELED) {
            throw new BookingException(
                    "Only expired or canceled payments can be renewed. Current status: "
                            + payment.getStatus()
            );
        }

        Booking booking = payment.getBooking();
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BookingException(
                    "Cannot renew payment for non-pending booking. Booking status: "
                            + booking.getStatus()
            );
        }

        try {
            Session stripeSession = createStripeSession(booking);

            payment.setSessionUrl(stripeSession.getUrl());
            payment.setSessionId(stripeSession.getId());
            payment.setStatus(PaymentStatus.PENDING);
            payment.setExpiresAt(LocalDateTime.now().plusHours(SESSION_EXPIRATION_HOURS));

            Payment renewed = paymentRepository.save(payment);

            return paymentMapper.toDto(renewed);

        } catch (StripeException e) {
            log.error("Failed to renew Stripe session for payment {}: {}",
                    paymentId, e.getMessage(), e);
            throw new BookingException("Failed to renew payment session: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Payment getPaymentById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Payment not found with id: " + id
                ));
    }

    @Override
    @Transactional
    @Scheduled(cron = "0 * * * * *")
    public void checkExpiredSessions() {
        log.debug("Checking for expired payment sessions...");

        List<Payment> expiredPayments = paymentRepository
                .findAllByStatusAndExpiresAtBefore(
                        PaymentStatus.PENDING,
                        LocalDateTime.now()
                );

        if (expiredPayments.isEmpty()) {
            log.debug("No expired payment sessions found");
            return;
        }

        log.info("Found {} expired payment sessions", expiredPayments.size());

        for (Payment payment : expiredPayments) {
            try {
                payment.markAsExpired();
                paymentRepository.save(payment);
                log.info("Marked payment {} as expired", payment.getId());
            } catch (Exception e) {
                log.error("Failed to mark payment {} as expired: {}",
                        payment.getId(), e.getMessage(), e);
            }
        }
    }

    private Session createStripeSession(Booking booking) throws StripeException {
        String successUrl = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path("/api/payments/success")
                .queryParam("session_id", "{CHECKOUT_SESSION_ID}")
                .toUriString();

        String cancelUrl = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path("/api/payments/cancel")
                .queryParam("session_id", "{CHECKOUT_SESSION_ID}")
                .toUriString();

        long amountInCents = booking.getTotalPrice()
                .multiply(BigDecimal.valueOf(STRIPE_AMOUNT_MULTIPLIER))
                .longValue();

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .setExpiresAt(LocalDateTime.now()
                        .plusHours(SESSION_EXPIRATION_HOURS)
                        .toEpochSecond(java.time.ZoneOffset.UTC))
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency(PAYMENT_CURRENCY)
                                                .setUnitAmount(amountInCents)
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData
                                                                .ProductData.builder()
                                                                .setName(String.format(
                                                                        "Booking #%d - %s",
                                                                        booking.getId(),
                                                                        booking.getAccommodation()
                                                                                .getType()
                                                                ))
                                                                .setDescription(String.format(
                                                                        "%s at %s (%d nights)",
                                                                        booking.getAccommodation()
                                                                                .getSize(),
                                                                        booking.getAccommodation()
                                                                                .getLocation(),
                                                                        booking.getNumberOfDays()
                                                                ))
                                                                .build()
                                                )
                                                .build()
                                )
                                .setQuantity(1L)
                                .build()
                )
                .setCustomerEmail(booking.getUser().getEmail())
                .putMetadata("bookingId", booking.getId().toString())
                .putMetadata("userId", booking.getUser().getId().toString())
                .build();

        return Session.create(params);
    }
}
