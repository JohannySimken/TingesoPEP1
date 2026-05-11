package com.travelagency.backend.services;

import com.travelagency.backend.entities.ReservationEntity;
import com.travelagency.backend.entities.TourPackageEntity;
import com.travelagency.backend.repositories.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock private ReservationRepository reservationRepository;
    @Mock private TourPackageService tourPackageService;

    @InjectMocks
    private ReportService reportService;

    private LocalDate startDate;
    private LocalDate endDate;


    // Covered cases: getSalesByPeriod,invalidDates, getPackageRanking, emptyRanking

    @BeforeEach
    void setUp() {
        startDate = LocalDate.now().minusDays(30);
        endDate = LocalDate.now();
    }

    @Test
    void getSalesByPeriod_success() {
        ReservationEntity r = ReservationEntity.builder().id(1L).build();
        when(reservationRepository.findConfirmedByPeriod(any(), any())).thenReturn(List.of(r));

        List<ReservationEntity> result = reportService.getSalesBYPeriod(startDate, endDate);
        assertEquals(1, result.size());
    }

    @Test
    void getSalesByPeriod_invalidDates_throwsException() {
        assertThrows(RuntimeException.class,
                () -> reportService.getSalesBYPeriod(endDate, startDate));
    }

    @Test
    void getPackageRanking_success() {
        TourPackageEntity pkg = TourPackageEntity.builder()
                .id(1L).name("Tour Caribe").destination("Caribe").build();
        Object[] row = new Object[]{1L, 5L, 10L, new BigDecimal("5000")};

        when(reservationRepository.findPackageRankingByPeriod(
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(List.<Object[]>of(row));        when(tourPackageService.findById(1L)).thenReturn(pkg);

        List<Map<String, Object>> result = reportService.getPackageRanking(startDate, endDate);
        assertEquals(1, result.size());
        assertEquals("Tour Caribe", result.get(0).get("packageName"));
        assertEquals("Caribe", result.get(0).get("destination"));
    }

    @Test
    void getPackageRanking_invalidDates_throwsException() {
        assertThrows(RuntimeException.class,
                () -> reportService.getPackageRanking(endDate, startDate));
    }

    @Test
    void getPackageRanking_emptyResult_returnsEmptyList() {
        when(reservationRepository.findPackageRankingByPeriod(any(), any())).thenReturn(List.of());
        List<Map<String, Object>> result = reportService.getPackageRanking(startDate, endDate);
        assertTrue(result.isEmpty());
    }
}