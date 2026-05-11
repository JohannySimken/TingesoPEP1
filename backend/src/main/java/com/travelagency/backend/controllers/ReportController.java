package com.travelagency.backend.controllers;

import com.travelagency.backend.services.ReportService;
import com.travelagency.backend.entities.ReservationEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.Map;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/sales")
    public ResponseEntity<List<ReservationEntity>> getSales(@RequestParam("startDate") LocalDate startDate, @RequestParam("endDate") LocalDate endDate){
        return ResponseEntity.ok(reportService.getSalesBYPeriod(startDate, endDate));
    }

    @GetMapping("/ranking")
    public ResponseEntity<List<Map<String, Object>>> getRanking(@RequestParam LocalDate startDate, @RequestParam LocalDate endDate){
        return ResponseEntity.ok(reportService.getPackageRanking(startDate, endDate));
    }
}
