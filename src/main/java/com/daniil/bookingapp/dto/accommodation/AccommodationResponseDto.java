package com.daniil.bookingapp.dto.accommodation;

import com.daniil.bookingapp.model.enums.AccommodationType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccommodationResponseDto {
    private Long id;
    private AccommodationType type;
    private String location;
    private String size;
    private List<String> amenities;
    private BigDecimal dailyRate;
    private Integer availability;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
