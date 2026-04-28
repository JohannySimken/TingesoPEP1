package com.travelagency.backend.repositories;

import com.travelagency.backend.entities.PromotionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.time.LocalDate;



public interface PromotionRepository extends JpaRepository<PromotionEntity, Long> {

    @Query("""
        SELECT p FROM PromotionEntity p
        WHERE p.isActive = true
          AND p.startDate <= :date
          AND p.endDate >= :date
    """)
    List<PromotionEntity> findActivePromotions(@Param("date") LocalDate date);

}
