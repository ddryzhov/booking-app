package com.daniil.bookingapp.service.impl;

import com.daniil.bookingapp.dto.accommodation.AccommodationRequestDto;
import com.daniil.bookingapp.dto.accommodation.AccommodationResponseDto;
import com.daniil.bookingapp.dto.accommodation.AccommodationUpdateRequestDto;
import com.daniil.bookingapp.exception.EntityNotFoundException;
import com.daniil.bookingapp.mapper.AccommodationMapper;
import com.daniil.bookingapp.model.Accommodation;
import com.daniil.bookingapp.model.Booking;
import com.daniil.bookingapp.model.enums.AccommodationType;
import com.daniil.bookingapp.repository.AccommodationRepository;
import com.daniil.bookingapp.service.AccommodationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccommodationServiceImpl implements AccommodationService {
    private final AccommodationRepository accommodationRepository;
    private final AccommodationMapper accommodationMapper;

    @Override
    @Transactional
    public AccommodationResponseDto create(AccommodationRequestDto requestDto) {
        Accommodation accommodation = accommodationMapper.toEntity(requestDto);
        Accommodation saved = accommodationRepository.save(accommodation);
        return accommodationMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AccommodationResponseDto> findAll(Pageable pageable) {
        return accommodationRepository.findAllByDeletedFalse(pageable)
                .map(accommodationMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AccommodationResponseDto> findByFilters(
            String location,
            AccommodationType type,
            Pageable pageable
    ) {
        return accommodationRepository.findByFilters(location, type, pageable)
                .map(accommodationMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public AccommodationResponseDto findById(Long id) {
        Accommodation accommodation = getAccommodationById(id);
        return accommodationMapper.toDto(accommodation);
    }

    @Override
    @Transactional
    public AccommodationResponseDto update(Long id, AccommodationUpdateRequestDto requestDto) {
        Accommodation accommodation = getAccommodationById(id);
        accommodationMapper.updateEntity(accommodation, requestDto);
        Accommodation updated = accommodationRepository.save(accommodation);
        return accommodationMapper.toDto(updated);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Accommodation accommodation = getAccommodationById(id);

        long activeBookings = accommodation.getBookings().stream()
                .filter(Booking::isActive)
                .count();

        if (activeBookings > 0) {
            throw new IllegalStateException(
                    "Cannot delete accommodation with active bookings"
            );
        }

        accommodationRepository.delete(accommodation);
    }

    @Override
    @Transactional(readOnly = true)
    public Accommodation getAccommodationById(Long id) {
        return accommodationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Accommodation not found with id: " + id
                ));
    }
}
