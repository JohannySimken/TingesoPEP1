package com.travelagency.backend.controllers;

import com.travelagency.backend.entities.ReservationEntity;
import com.travelagency.backend.entities.UserEntity;
import com.travelagency.backend.repositories.UserRepository;
import com.travelagency.backend.services.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> reservationData) {
        Long userId = Long.valueOf(reservationData.get("userId").toString());
        Long packageId = Long.valueOf(reservationData.get("packageId").toString());
        int passengerCount = Integer.parseInt(reservationData.get("passengerCount").toString());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reservationService.create(userId, packageId, passengerCount));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationEntity> findById(@PathVariable Long id){
        return ResponseEntity.ok(reservationService.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<ReservationEntity>> findAll(){
        return ResponseEntity.ok(reservationService.findAll());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReservationEntity>> findByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(reservationService.findByUserId(userId));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ReservationEntity> cancel(@PathVariable Long id){
        return ResponseEntity.ok(reservationService.cancel(id));
    }

    @PatchMapping("/{id}/confirm")
    public ResponseEntity<?> confirm(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.confirm(id));
    }
}
