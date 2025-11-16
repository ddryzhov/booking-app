package com.daniil.bookingapp.controller;

import com.daniil.bookingapp.dto.booking.BookingRequestDto;
import com.daniil.bookingapp.dto.booking.BookingResponseDto;
import com.daniil.bookingapp.dto.booking.BookingUpdateRequestDto;
import com.daniil.bookingapp.model.User;
import com.daniil.bookingapp.model.enums.BookingStatus;
import com.daniil.bookingapp.service.BookingService;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Booking Management", description = "APIs for managing accommodation bookings")
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create new booking",
            description = "Creates a new booking for an accommodation. "
                    + "Validates availability, checks for overlaps, "
                    + "and requires no pending bookings.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Booking created successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BookingResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid dates or accommodation "
                    + "unavailable or has pending bookings"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Accommodation not found")
    })
    public BookingResponseDto create(
            @Valid @RequestBody BookingRequestDto requestDto,
            @AuthenticationPrincipal User user
    ) {
        return bookingService.create(requestDto, user);
    }

    @GetMapping("/my")
    @Operation(summary = "Get my bookings",
            description = "Retrieves all bookings for the current user with pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bookings retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public Page<BookingResponseDto> getMyBookings(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Pagination parameters")
            @PageableDefault(size = 20, sort = "id") Pageable pageable
    ) {
        return bookingService.findMyBookings(user, pageable);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get all bookings with filters",
            description = "Retrieves all bookings with optional filters. "
                    + "Only accessible by ADMIN or MANAGER roles.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bookings retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied - "
                    + "Admin or Manager role required")
    })
    public Page<BookingResponseDto> findAll(
            @Parameter(description = "Filter by user ID")
            @RequestParam(required = false) Long userId,

            @Parameter(description = "Filter by booking status")
            @RequestParam(required = false) BookingStatus status,

            @Parameter(description = "Pagination parameters")
            @PageableDefault(size = 20, sort = "id") Pageable pageable
    ) {
        return bookingService.findAllByFilters(userId, status, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get booking by ID",
            description = "Retrieves booking details. "
                    + "Users can only see their own bookings. "
                    + "Admins/Managers can see all bookings.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Booking found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BookingResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    public BookingResponseDto findById(
            @Parameter(description = "Booking ID")
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) {
        return bookingService.findById(id, user);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Fully update booking",
            description = "Updates booking details. Users can only update their own bookings.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Booking updated successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BookingResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid data or dates conflict"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    public BookingResponseDto update(
            @Parameter(description = "Booking ID")
            @PathVariable Long id,
            @Valid @RequestBody BookingUpdateRequestDto requestDto,
            @AuthenticationPrincipal User user
    ) {
        return bookingService.update(id, requestDto, user);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Partially update booking",
            description = "Partially updates booking details. "
                    + "Users can only update their own bookings.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Booking updated successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BookingResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid data or dates conflict"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    public BookingResponseDto patch(
            @Parameter(description = "Booking ID")
            @PathVariable Long id,
            @Valid @RequestBody BookingUpdateRequestDto requestDto,
            @AuthenticationPrincipal User user
    ) {
        return bookingService.update(id, requestDto, user);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Cancel booking",
            description = "Cancels a booking and releases accommodation availability. "
                    + "Only booking owner can cancel.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Booking cancelled successfully"),
            @ApiResponse(responseCode = "400", description = "Booking cannot be cancelled"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied - "
                    + "Only owner can cancel"),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    public void cancel(
            @Parameter(description = "Booking ID")
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) {
        bookingService.cancel(id, user);
    }
}
