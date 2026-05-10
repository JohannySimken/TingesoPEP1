package com.travelagency.backend.repositories;


import com.travelagency.backend.entities.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {

    Optional<PaymentEntity> findByReservationId(Long reservationId);
    boolean existsByReservationId(Long reservationId);
}
