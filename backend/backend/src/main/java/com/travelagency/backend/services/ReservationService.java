package com.travelagency.backend.services;

import com.travelagency.backend.entities.*;
import com.travelagency.backend.repositories.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserService userService;
    private final TourPackageService tourPackageService;
    private final PromotionService promotionService;


    @Value("${app.reservation.expiry-hours:24}")
    private int expiryHours;

    @Value("${app.discount.group-threshold:4}")
    private int groupThreshold;

    @Value("${app.discount.group-percentage:10}")
    private BigDecimal groupPercentage;

    @Value("${app.discount.frequent-client-threshold:3}")
    private int frequentClientThreshold;

    @Value("${app.discount.frequent-client-percentage:5}")
    private BigDecimal frequentClientPercentage;

    @Value("${app.discount.multi-package-percentage:5}")
    private BigDecimal multiPackagePercentage;

    @Value("${app.discount.max-accumulated:20}")
    private BigDecimal maxAccumulated;

    @Value("${app.discount.accumulate:true}")
    private boolean accumulate;

    @Transactional
    public ReservationEntity create(Long userId, Long packageId, int passengerCount){
        // Validacion de que exista el usuario y el paquete
        userService.findById(userId);
        TourPackageEntity pkg = tourPackageService.findById(packageId);

        // Validacion de reglas operativas
        if(pkg.getStatus() == TourPackageEntity.Status.CANCELLED || pkg.getStatus() == TourPackageEntity.Status.NOT_VALID
                || pkg.getStatus() == TourPackageEntity.Status.SOLD_OUT){
            throw new RuntimeException("Paquete no disponible para reserva");
        }
        if(passengerCount <= 0){
            throw new RuntimeException("El numero de pasajeros debe ser mayor a 0");
        }
        if(pkg.getAvailableSlots() < passengerCount ){
            throw new RuntimeException("No hay asientos suficientes. Disponibles: " + pkg.getAvailableSlots());

        }

        // Calculo del precio
        BigDecimal basePrice = pkg.getPrice().multiply(BigDecimal.valueOf(passengerCount));
        List<String> details = new ArrayList<>();
        BigDecimal totalPct = BigDecimal.ZERO;

        // Descuentos por grupo
        if (passengerCount >= groupThreshold) {
            totalPct = totalPct.add(groupPercentage);
            details.add("Descuento por grupo:  " + groupPercentage + "%");
        }

        // Descuento por cliente frecuente
        long paid = reservationRepository.countByUserIdAndStatus(userId, ReservationEntity.Status.CONFIRMED);
        if(paid >= frequentClientThreshold){
            totalPct = accumulate ? totalPct.add(frequentClientPercentage) : totalPct.max(frequentClientPercentage);
            details.add("Descuento por cliente frecuente:  " + frequentClientPercentage + "%");
        }

        // Descuento por multiples paquetes reservados confirmados en los ultimos 30 dias
        List<ReservationEntity> recent = reservationRepository.findConfirmedByPeriod(
                LocalDateTime.now().minusDays(30), LocalDateTime.now());
        boolean hasRecent = recent.stream().anyMatch(r -> r.getUserId().equals(userId));
        if(hasRecent){
            totalPct = accumulate ? totalPct.add(multiPackagePercentage) : totalPct.max(multiPackagePercentage);
            details.add("Descuento por multiples paquetes reservados:  " + multiPackagePercentage + "%");
        }

        // Descuento por promocion activa
        List<PromotionEntity> promotions = promotionService.findActive(LocalDate.now());
        if(!promotions.isEmpty()){
            BigDecimal promoPct = promotions.get(0).getDiscountPercentage();
            totalPct = accumulate ? totalPct.add(promoPct) : totalPct.max(promoPct);
            details.add("Descuento por promoción activa: " + promotions.get(0).getName() + " - " + promoPct + "%");
        }

        // Limite maximo de descuento
        if(totalPct.compareTo(maxAccumulated) > 0) totalPct = maxAccumulated;

        BigDecimal discountAmount = basePrice.multiply(totalPct).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal finalPrice = basePrice.subtract(discountAmount).max(BigDecimal.ZERO);

        // Descontar cupos del paquete
        tourPackageService.reduceSlots(packageId, passengerCount);

        ReservationEntity reservation = ReservationEntity.builder()
                .reservationCode(UUID.randomUUID().toString().substring(0,8).toUpperCase())
                .userId(userId)
                .tourPackageId(packageId)
                .passengerCount(passengerCount)
                .baseAmount(basePrice)
                .discountAmount(discountAmount)
                .discountDetail(details.isEmpty() ? "No se aplicaron descuentos" : String.join(" | ", details))
                .finalAmount(finalPrice)
                .status(ReservationEntity.Status.PENDING)
                .expiresAt(LocalDateTime.now().plusHours(expiryHours))
                .build();
        return reservationRepository.save(reservation);
    }

    public ReservationEntity findById(Long id){
        return reservationRepository.findById(id).orElseThrow(() -> new RuntimeException("La reserva con id  " + id + " no ha sido encontrada"));
    }

    public List<ReservationEntity> findByUserId(Long userId){
        return reservationRepository.findByUserId(userId);
    }

    public List<ReservationEntity> findAll(){
        return reservationRepository.findAll();
    }

    @Transactional
    public ReservationEntity confirm(Long id){
        ReservationEntity reservation = findById(id);
        reservation.setStatus(ReservationEntity.Status.CONFIRMED);
        return reservationRepository.save(reservation);
    }

    @Transactional
    public ReservationEntity cancel(Long id){
        ReservationEntity reservation = findById(id);
        if(reservation.getStatus() == ReservationEntity.Status.CONFIRMED){
            throw new RuntimeException("No se puede cancelar una reserva confirmada y pagada");
        }
        reservation.setStatus(ReservationEntity.Status.CANCELLED);
        tourPackageService.releaseSlots(reservation.getTourPackageId(), reservation.getPassengerCount());
        return reservationRepository.save(reservation);
    }

    @Transactional
    public void expireReservation(){
        List<ReservationEntity> expired = reservationRepository.findByStatusAndExpiresAtBefore(ReservationEntity.Status.PENDING, LocalDateTime.now());
        for (ReservationEntity reservation : expired) {
            reservation.setStatus(ReservationEntity.Status.EXPIRED);
            tourPackageService.releaseSlots(reservation.getTourPackageId(), reservation.getPassengerCount());
            reservationRepository.save(reservation);
        }
    }
}
