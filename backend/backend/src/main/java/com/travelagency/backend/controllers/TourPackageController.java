package com.travelagency.backend.controllers;

import com.travelagency.backend.services.TourPackageService;
import com.travelagency.backend.entities.TourPackageEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.math.BigDecimal;
import java.util.List;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/tour-packages")
@RequiredArgsConstructor
public class TourPackageController {
    private final TourPackageService tourPackageService;

    @PostMapping
    public ResponseEntity<TourPackageEntity> createTourPackage(@RequestBody TourPackageEntity tourPackage){
        return ResponseEntity.status(HttpStatus.CREATED).body(tourPackageService.createPackage(tourPackage));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TourPackageEntity> findById(@PathVariable Long id){
        return ResponseEntity.ok(tourPackageService.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<TourPackageEntity>> findAll(){
        return ResponseEntity.ok(tourPackageService.findAll());
    }

    @GetMapping("/available")
    public ResponseEntity<List<TourPackageEntity>> findAvailable(){
        return ResponseEntity.ok(tourPackageService.findAvailable());
    }

    @GetMapping("/search")
    public ResponseEntity<List<TourPackageEntity>> searchPackages(
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) String tripType){
        return ResponseEntity.ok(tourPackageService.searchPackages(destination, minPrice, maxPrice, startDate, endDate, tripType));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TourPackageEntity> update(@PathVariable Long id, @RequestBody TourPackageEntity tourPackageUpdated){
        return ResponseEntity.ok(tourPackageService.update(id, tourPackageUpdated));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TourPackageEntity> updateStatus(@PathVariable Long id, @RequestParam TourPackageEntity.Status status){
        return ResponseEntity.ok(tourPackageService.changeStatus(id, status));
    }

}
