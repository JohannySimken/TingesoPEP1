package com.travelagency.backend.controllers;

import com.travelagency.backend.entities.PaymentEntity;
import com.travelagency.backend.services.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentEntity> processPayment(@RequestBody Map<String, Object> body) {
        Long reservationId = Long.valueOf(body.get("reservationId").toString());
        String cardNumber  = body.get("cardNumber").toString();
        String cardExpiry  = body.get("cardExpiry").toString();
        String cvv         = body.get("cvv").toString();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.processPayment(reservationId, cardNumber, cardExpiry, cvv));
    }

    @GetMapping("/reservation/{reservationId}")
    public ResponseEntity<PaymentEntity> findByReservation(@PathVariable Long reservationId) {
        return ResponseEntity.ok(paymentService.findByReservation(reservationId));
    }
}
