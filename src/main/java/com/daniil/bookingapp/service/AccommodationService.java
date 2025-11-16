package com.daniil.bookingapp.service;

import com.daniil.bookingapp.dto.accommodation.AccommodationRequestDto;
import com.daniil.bookingapp.dto.accommodation.AccommodationResponseDto;
import com.daniil.bookingapp.dto.accommodation.AccommodationUpdateRequestDto;
import com.daniil.bookingapp.model.Accommodation;
import com.daniil.bookingapp.model.enums.AccommodationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AccommodationService {
    AccommodationResponseDto create(AccommodationRequestDto requestDto);

    Page<AccommodationResponseDto> findAll(Pageable pageable);

    Page<AccommodationResponseDto> findByFilters(
            String location,
            AccommodationType type,
            Pageable pageable
    );

    AccommodationResponseDto findById(Long id);

    AccommodationResponseDto update(Long id, AccommodationUpdateRequestDto requestDto);

    void delete(Long id);

    Accommodation getAccommodationById(Long id);
}
