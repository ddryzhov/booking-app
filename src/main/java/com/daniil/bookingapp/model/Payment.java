package com.daniil.bookingapp.model;

import com.daniil.bookingapp.model.enums.PaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"user", "booking"})
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "session_url", length = 500)
    private String sessionUrl;

    @Column(name = "session_id", length = 100)
    private String sessionId;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Column(name = "amount_to_pay", nullable = false, precision = 10, scale = 2)
    private BigDecimal amountToPay;

    @Column(name = "stripe_payment_intent_id", length = 100)
    private String stripePaymentIntentId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isPending() {
        return status == PaymentStatus.PENDING;
    }

    public boolean isPaid() {
        return status == PaymentStatus.PAID;
    }

    public boolean isCanceled() {
        return status == PaymentStatus.CANCELED;
    }

    public void markAsPaid() {
        if (status != PaymentStatus.PENDING) {
            throw new IllegalStateException("Only pending payments can be marked as paid");
        }
        this.status = PaymentStatus.PAID;
        this.paidAt = LocalDateTime.now();
    }

    public void markAsExpired() {
        if (status != PaymentStatus.PENDING) {
            throw new IllegalStateException("Only pending payments can expire");
        }
        this.status = PaymentStatus.EXPIRED;
    }

    public void markAsCanceled() {
        if (status != PaymentStatus.PENDING) {
            throw new IllegalStateException("Only pending payments can be canceled");
        }
        this.status = PaymentStatus.CANCELED;
    }
}
