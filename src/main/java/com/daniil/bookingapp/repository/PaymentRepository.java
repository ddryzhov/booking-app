package com.daniil.bookingapp.repository;

import com.daniil.bookingapp.model.Payment;
import com.daniil.bookingapp.model.enums.PaymentStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findBySessionId(String sessionId);

    Page<Payment> findAllByUserId(Long userId, Pageable pageable);

    @Query("SELECT p FROM Payment p WHERE "
            + "p.status = :status AND "
            + "p.expiresAt < :expirationTime")
    List<Payment> findAllByStatusAndExpiresAtBefore(
            @Param("status") PaymentStatus status,
            @Param("expirationTime") LocalDateTime expirationTime
    );
}
