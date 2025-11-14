package com.daniil.bookingapp.dto.payment;

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
public class PaymentSuccessResponseDto {
    private Long paymentId;
    private Long bookingId;
    private BigDecimal amountPaid;
    private LocalDateTime paidAt;
}
