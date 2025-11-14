package com.daniil.bookingapp.dto.accommodation;

import com.daniil.bookingapp.model.enums.AccommodationType;
import jakarta.validation.constraints.DecimalMin;
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
public class AccommodationUpdateRequestDto {
    private AccommodationType type;
    private String location;
    private String size;
    private List<String> amenities;

    @DecimalMin(value = "0.01", message = "Daily rate must be positive")
    private BigDecimal dailyRate;

    @Positive(message = "Availability must be positive")
    private Integer availability;
}
