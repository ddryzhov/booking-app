package com.daniil.bookingapp.service;

import com.daniil.bookingapp.dto.payment.PaymentCancelResponseDto;
import com.daniil.bookingapp.dto.payment.PaymentRequestDto;
import com.daniil.bookingapp.dto.payment.PaymentResponseDto;
import com.daniil.bookingapp.dto.payment.PaymentSuccessResponseDto;
import com.daniil.bookingapp.model.Payment;
import com.daniil.bookingapp.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentService {
    Page<PaymentResponseDto> getPayments(Long userId, User user, Pageable pageable);

    PaymentResponseDto createPaymentSession(PaymentRequestDto requestDto, User user);

    PaymentSuccessResponseDto handleSuccessPayment(String sessionId);

    PaymentCancelResponseDto handleCancelPayment(String sessionId);

    PaymentResponseDto renewPaymentSession(Long paymentId, User user);

    Payment getPaymentById(Long id);

    void checkExpiredSessions();
}
