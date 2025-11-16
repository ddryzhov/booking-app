package com.daniil.bookingapp.repository;

import com.daniil.bookingapp.model.Accommodation;
import com.daniil.bookingapp.model.enums.AccommodationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AccommodationRepository extends JpaRepository<Accommodation, Long> {
    Page<Accommodation> findAllByDeletedFalse(Pageable pageable);

    @Query("SELECT a FROM Accommodation a WHERE "
            + "(:location IS NULL OR LOWER(a.location) "
            + "LIKE LOWER(CONCAT('%', :location, '%'))) AND "
            + "(:type IS NULL OR a.type = :type) AND "
            + "a.deleted = false")
    Page<Accommodation> findByFilters(
            @Param("location") String location,
            @Param("type") AccommodationType type,
            Pageable pageable
    );
}
