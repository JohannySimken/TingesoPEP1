package com.travelagency.backend.services;

import com.travelagency.backend.entities.*;
import com.travelagency.backend.repositories.PaymentRepository;
import com.travelagency.backend.repositories.VoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final VoucherRepository voucherRepository;
    private final ReservationService reservationService;
    private final TourPackageService tourPackageService;
    private final UserService userService;

    @Transactional
    public PaymentEntity processPayment(Long reservationId, String cardNumber, String cardExpiry, String cvv){

        ReservationEntity reservation = reservationService.findById(reservationId);

        if(reservation.getStatus() == ReservationEntity.Status.CANCELLED){
            throw new RuntimeException("No se puede realizar un pago para una reserva cancelada");
        }
        if(reservation.getStatus() == ReservationEntity.Status.CONFIRMED){
            throw new RuntimeException("No se puede realizar un pago para una reserva confirmada");
        }
        if(reservation.getStatus() == ReservationEntity.Status.EXPIRED){
            throw new RuntimeException("No se puede realizar un pago para una reserva expirada");
        }
        if(paymentRepository.existsByReservationId(reservationId)){
            throw new RuntimeException("Ya se ha realizado un pago para esta reserva");
        }
        if(reservation.getFinalAmount().compareTo(BigDecimal.ZERO) <= 0){
            throw new RuntimeException("El precio total de la reserva debe ser mayor a 0");
        }

        // Simulacion de pago
        PaymentEntity payment = PaymentEntity.builder()
                .transactionCode(UUID.randomUUID().toString().substring(0,12).toUpperCase())
                .reservationId(reservationId)
                .amount(reservation.getFinalAmount())
                .paymentMethod(PaymentEntity.Method.CREDIT_CARD)
                .cardNumber(maskCard(cardNumber))
                .cardExpiry(cardExpiry)
                .cvv(cvv)
                .status(PaymentEntity.Status.APPROVED)
                .paidAt(LocalDateTime.now())
                .build();
        paymentRepository.save(payment);

        // Confirmacion de la reserva
        reservationService.confirm(reservationId);

        generateVoucher(reservation);

        return payment;
    }

    public PaymentEntity findByReservation(Long reservationId) {
        return paymentRepository.findByReservationId(reservationId)
                .orElseThrow(() -> new RuntimeException("Payment not found for reservation: " + reservationId));
    }

    private void generateVoucher(ReservationEntity reservation) {
        TourPackageEntity pkg  = tourPackageService.findById(reservation.getTourPackageId());
        UserEntity user        = userService.findById(reservation.getUserId());

        String content = String.format(
                "VOUCHER - TravelAgency\n" +
                        "Code: %s\n" +
                        "Client: %s\n" +
                        "Package: %s | Destination: %s\n" +
                        "Dates: %s to %s\n" +
                        "Passengers: %d\n" +
                        "Base amount: $%s\n" +
                        "Discounts: %s (-$%s)\n" +
                        "Total paid: $%s\n" +
                        "Generated: %s",
                reservation.getReservationCode(),
                user.getName(),
                pkg.getName(), pkg.getDestination(),
                pkg.getStartDate(), pkg.getEndDate(),
                reservation.getPassengerCount(),
                reservation.getBaseAmount(),
                reservation.getDiscountDetail(), reservation.getDiscountAmount(),
                reservation.getFinalAmount(),
                LocalDateTime.now()
        );

        voucherRepository.save(VoucherEntity.builder()
                .reservationId(reservation.getId())
                .generatedAt(LocalDateTime.now())
                .content(content)
                .build());
    }

    private String maskCard(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) return "****";
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }
}
