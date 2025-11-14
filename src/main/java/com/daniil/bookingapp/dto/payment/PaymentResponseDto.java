package com.daniil.bookingapp.dto.payment;

import com.daniil.bookingapp.model.enums.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponseDto {
    private Long id;
    private PaymentStatus status;
    private Long bookingId;
    private Long userId;
    private String sessionUrl;
    private String sessionId;
    private BigDecimal amountToPay;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
}
