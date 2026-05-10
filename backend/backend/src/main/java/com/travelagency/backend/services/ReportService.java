package com.travelagency.backend.services;

import com.travelagency.backend.entities.ReservationEntity;
import com.travelagency.backend.entities.TourPackageEntity;
import com.travelagency.backend.repositories.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.List;




@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReservationRepository reservationRepository;
    private final TourPackageService tourPackageService;

    public List<ReservationEntity> getSalesBYPeriod(LocalDate startDate, LocalDate endDate){
        if(startDate.isAfter(endDate)){
            throw new RuntimeException("Fecha de inicio no puede ser mayor que la fecha de fin");
        }
        return reservationRepository.findConfirmedByPeriod(startDate.atStartOfDay(), endDate.atTime(23, 59, 59));
    }

    public List<Map<String, Object>> getPackageRanking(LocalDate startDate, LocalDate endDate){
        if(startDate.isAfter(endDate)){
            throw new RuntimeException("Fecha de inicio no puede ser mayor que la fecha de fin");
        }

        List<Object[]> rows = reservationRepository.findPackageRankingByPeriod(startDate.atStartOfDay(), endDate.atTime(23, 59, 59));

        return rows.stream().map(row -> {
            Long packageId = (Long) row[0];
            TourPackageEntity pkg = tourPackageService.findById(packageId);
            Map<String, Object> item = new HashMap<>();
            item.put("packageId", packageId);
            item.put("packageName", pkg.getName());
            item.put("destination", pkg.getDestination());
            item.put("totalReservations", row[1]);
            item.put("totalPassengers", row[2]);
            item.put("totalRevenue", row[3]);
            return item;
        }).toList();
    }
}
