package com.travelagency.backend.repositories;

import com.travelagency.backend.entities.ReservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<ReservationEntity, Long> {

    List<ReservationEntity> findByUserId(Long userId);

    long countByUserIdAndStatus(Long userId, ReservationEntity.Status status);

    List<ReservationEntity> findByStatusAndExpiresAtBefore(ReservationEntity.Status status, LocalDateTime dateTime);


    @Query(""" 
                SELECT r FROM ReservationEntity r 
                WHERE r.status = 'CONFIRMED' 
                 AND r.createdAt BETWEEN :startDate AND :endDate 
                ORDER BY r.createdAt DESC
        """)
    List<ReservationEntity> findConfirmedByPeriod(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);


    @Query("""
        SELECT r.tourPackageId, COUNT(r), SUM(r.passengerCount), SUM(r.finalAmount)
        FROM ReservationEntity r
        WHERE r.status = 'CONFIRMED'
          AND r.createdAt BETWEEN :startDate AND :endDate
        GROUP BY r.tourPackageId
        ORDER BY COUNT(r) DESC
    """)
    List<Object[]> findPackageRankingByPeriod(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
