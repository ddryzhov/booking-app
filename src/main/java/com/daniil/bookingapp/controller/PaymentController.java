package com.daniil.bookingapp.controller;

import com.daniil.bookingapp.dto.payment.PaymentCancelResponseDto;
import com.daniil.bookingapp.dto.payment.PaymentRequestDto;
import com.daniil.bookingapp.dto.payment.PaymentResponseDto;
import com.daniil.bookingapp.dto.payment.PaymentSuccessResponseDto;
import com.daniil.bookingapp.model.User;
import com.daniil.bookingapp.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Payment Management",
        description = "APIs for managing payments through Stripe integration")
public class PaymentController {
    private final PaymentService paymentService;

    @GetMapping
    @Operation(summary = "Get payments",
            description = "Retrieves payments. Customers see only their own payments, "
                    + "managers/admins can filter by user ID or see all payments.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payments retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public Page<PaymentResponseDto> getPayments(
            @Parameter(description = "Filter by user ID (managers/admins only)")
            @RequestParam(required = false) Long userId,

            @AuthenticationPrincipal User user,

            @Parameter(description = "Pagination parameters")
            @PageableDefault(size = 20, sort = "id") Pageable pageable
    ) {
        return paymentService.getPayments(userId, user, pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create payment session",
            description = "Creates a new Stripe payment session for a booking. "
                    + "Returns session URL for payment and payment details.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Payment session created",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PaymentResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid booking or "
                    + "booking already has payment"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied - "
                    + "not booking owner"),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    public PaymentResponseDto createPaymentSession(
            @Valid @RequestBody PaymentRequestDto requestDto,
            @AuthenticationPrincipal User user
    ) {
        return paymentService.createPaymentSession(requestDto, user);
    }

    @GetMapping("/success")
    @Operation(summary = "Handle successful payment",
            description = "Stripe redirects here after successful payment. "
                    + "Verifies payment and updates booking status.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Payment processed successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(
                                    implementation = PaymentSuccessResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid session or "
                    + "payment already processed"),
            @ApiResponse(responseCode = "404", description = "Payment session not found")
    })
    public PaymentSuccessResponseDto handleSuccess(
            @Parameter(description = "Stripe session ID")
            @RequestParam("session_id") String sessionId
    ) {
        return paymentService.handleSuccessPayment(sessionId);
    }

    @GetMapping("/cancel")
    @Operation(summary = "Handle cancelled payment",
            description = "Stripe redirects here when user cancels payment. "
                    + "Returns message and option to renew payment session.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cancellation handled",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(
                                    implementation = PaymentCancelResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Payment session not found")
    })
    public PaymentCancelResponseDto handleCancel(
            @Parameter(description = "Stripe session ID")
            @RequestParam("session_id") String sessionId
    ) {
        return paymentService.handleCancelPayment(sessionId);
    }

    @PostMapping("/renew")
    @Operation(summary = "Renew expired payment session",
            description = "Creates a new Stripe session for an expired payment. "
                    + "Only available for EXPIRED payments.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Payment session renewed successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PaymentResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Payment cannot be renewed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    @PreAuthorize("hasAnyRole('CUSTOMER', 'MANAGER', 'ADMIN')")
    public PaymentResponseDto renewPaymentSession(
            @Parameter(description = "Payment ID to renew")
            @RequestParam Long paymentId,
            @AuthenticationPrincipal User user
    ) {
        return paymentService.renewPaymentSession(paymentId, user);
    }
}
