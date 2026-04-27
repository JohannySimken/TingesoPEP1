package com.travelagency.backend.repositories;

import com.travelagency.backend.entities.TourPackageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface TourPackageRepository extends JpaRepository<TourPackageEntity, Long> {
    List<TourPackageEntity> findByStatus(TourPackageEntity.Status status);

    @Query("""
        SELECT p FROM TourPackageEntity p
        WHERE p.status = 'AVAILABLE'
          AND (:destination IS NULL OR LOWER(p.destination) LIKE LOWER(CONCAT('%',:destination,'%')))
          AND (:minPrice IS NULL OR p.price >= :minPrice)
          AND (:maxPrice IS NULL OR p.price <= :maxPrice)
          AND (:startDate IS NULL OR p.startDate >= :startDate)
          AND (:endDate IS NULL OR p.endDate <= :endDate)
          AND (:tripType IS NULL OR p.tripType = :tripType)
    """)
    List<TourPackageEntity> searchPackages(
            @Param("destination") String destination,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("tripType") String tripType
    );
}
