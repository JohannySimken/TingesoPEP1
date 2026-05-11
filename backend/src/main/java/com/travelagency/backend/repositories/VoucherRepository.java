package com.travelagency.backend.repositories;

import com.travelagency.backend.entities.VoucherEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface VoucherRepository extends JpaRepository<VoucherEntity, Long> {
    Optional<VoucherEntity> findByReservationId(Long reservationId);
}
