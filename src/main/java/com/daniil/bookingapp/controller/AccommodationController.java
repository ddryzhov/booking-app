package com.daniil.bookingapp.controller;

import com.daniil.bookingapp.dto.accommodation.AccommodationRequestDto;
import com.daniil.bookingapp.dto.accommodation.AccommodationResponseDto;
import com.daniil.bookingapp.dto.accommodation.AccommodationUpdateRequestDto;
import com.daniil.bookingapp.model.enums.AccommodationType;
import com.daniil.bookingapp.service.AccommodationService;
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
@RequestMapping("/api/accommodations")
@RequiredArgsConstructor
@Tag(name = "Accommodation Management",
        description = "APIs for managing accommodation inventory (CRUD operations)")
public class AccommodationController {
    private final AccommodationService accommodationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create new accommodation",
            description = "Creates a new accommodation. Only accessible by ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Accommodation created successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AccommodationResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
    })
    public AccommodationResponseDto create(
            @Valid @RequestBody AccommodationRequestDto requestDto
    ) {
        return accommodationService.create(requestDto);
    }

    @GetMapping
    @Operation(summary = "Get all accommodations",
            description = "Retrieves a paginated list of accommodations with optional filters. "
                    + "Public endpoint - no authentication required.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Accommodations retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Page.class)))
    })
    public Page<AccommodationResponseDto> findAll(
            @Parameter(description = "Filter by location (partial match)")
            @RequestParam(required = false) String location,

            @Parameter(description = "Filter by accommodation type")
            @RequestParam(required = false) AccommodationType type,

            @Parameter(description = "Pagination parameters (page, size, sort)")
            @PageableDefault(size = 20, sort = "id") Pageable pageable
    ) {
        if (location != null || type != null) {
            return accommodationService.findByFilters(location, type, pageable);
        }
        return accommodationService.findAll(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get accommodation by ID",
            description = "Retrieves detailed information about a specific accommodation. "
                    + "Public endpoint - no authentication required.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Accommodation found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AccommodationResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Accommodation not found")
    })
    public AccommodationResponseDto findById(
            @Parameter(description = "Accommodation ID")
            @PathVariable Long id
    ) {
        return accommodationService.findById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Fully update accommodation",
            description = "Fully updates an existing accommodation. Only accessible by ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Accommodation updated successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AccommodationResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied - Admin role required"),
            @ApiResponse(responseCode = "404", description = "Accommodation not found")
    })
    public AccommodationResponseDto update(
            @Parameter(description = "Accommodation ID")
            @PathVariable Long id,
            @Valid @RequestBody AccommodationUpdateRequestDto requestDto
    ) {
        return accommodationService.update(id, requestDto);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Partially update accommodation",
            description = "Partially updates an existing accommodation. "
                    + "Only accessible by ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Accommodation updated successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AccommodationResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied - Admin role required"),
            @ApiResponse(responseCode = "404", description = "Accommodation not found")
    })
    public AccommodationResponseDto patch(
            @Parameter(description = "Accommodation ID")
            @PathVariable Long id,
            @Valid @RequestBody AccommodationUpdateRequestDto requestDto
    ) {
        return accommodationService.update(id, requestDto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete accommodation",
            description = "Soft deletes an accommodation. "
                    + "Cannot delete if there are active bookings. "
                    + "Only accessible by ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Accommodation deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Cannot delete - has active bookings"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied - Admin role required"),
            @ApiResponse(responseCode = "404", description = "Accommodation not found")
    })
    public void delete(
            @Parameter(description = "Accommodation ID")
            @PathVariable Long id
    ) {
        accommodationService.delete(id);
    }
}
