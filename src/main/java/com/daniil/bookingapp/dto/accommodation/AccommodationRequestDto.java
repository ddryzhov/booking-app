package com.daniil.bookingapp.dto.accommodation;

import com.daniil.bookingapp.model.enums.AccommodationType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccommodationRequestDto {

    @NotNull(message = "Type cannot be null")
    private AccommodationType type;

    @NotBlank(message = "Location cannot be blank")
    private String location;

    @NotBlank(message = "Size cannot be blank")
    private String size;

    private List<String> amenities;

    @NotNull(message = "Daily rate cannot be null")
    @DecimalMin(value = "0.01", message = "Daily rate must be positive")
    private BigDecimal dailyRate;

    @NotNull(message = "Availability cannot be null")
    @Positive(message = "Availability must be positive")
    private Integer availability;
}
