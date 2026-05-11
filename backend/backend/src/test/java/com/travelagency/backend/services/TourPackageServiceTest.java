package com.travelagency.backend.services;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.travelagency.backend.entities.TourPackageEntity;
import com.travelagency.backend.repositories.TourPackageRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TourPackageServiceTest {

    @Mock
    TourPackageRepository tourPackageRepository;

    @InjectMocks
    TourPackageService tourPackageService;

    // ── helpers ──────────────────────────────────────────────────────────────
    private TourPackageEntity validPackage() {
        TourPackageEntity p = new TourPackageEntity();
        p.setPrice(new BigDecimal("500"));
        p.setTotalSlots(10);
        p.setAvailableSlots(10);
        p.setStartDate(LocalDate.of(2026, 6, 1));
        p.setEndDate(LocalDate.of(2026, 6, 10));
        return p;
    }

    // ── validateAvailability branches ────────────────────────────────────────

    @Test
    void createPackage_throwsWhenPriceIsNull() {
        TourPackageEntity p = validPackage();
        p.setPrice(null);
        assertThatThrownBy(() -> tourPackageService.createPackage(p))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("precio");
    }

    @Test
    void createPackage_throwsWhenPriceIsZero() {
        TourPackageEntity p = validPackage();
        p.setPrice(BigDecimal.ZERO);
        assertThatThrownBy(() -> tourPackageService.createPackage(p))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("precio");
    }

    @Test
    void createPackage_throwsWhenTotalSlotsIsNull() {
        TourPackageEntity p = validPackage();
        p.setTotalSlots(null);
        assertThatThrownBy(() -> tourPackageService.createPackage(p))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("asientos");
    }

    @Test
    void createPackage_throwsWhenTotalSlotsIsZero() {
        TourPackageEntity p = validPackage();
        p.setTotalSlots(0);
        assertThatThrownBy(() -> tourPackageService.createPackage(p))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("asientos");
    }

    @Test
    void createPackage_throwsWhenEndDateBeforeStartDate() {
        TourPackageEntity p = validPackage();
        p.setEndDate(LocalDate.of(2026, 5, 1)); // antes del startDate
        assertThatThrownBy(() -> tourPackageService.createPackage(p))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("fecha");
    }

    @Test
    void createPackage_happyPath() {
        TourPackageEntity p = validPackage();
        when(tourPackageRepository.save(any())).thenReturn(p);
        TourPackageEntity result = tourPackageService.createPackage(p);
        assertThat(result).isNotNull();
    }

    // ── findById branches ─────────────────────────────────────────────────────

    @Test
    void findById_throwsWhenNotFound() {
        when(tourPackageRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> tourPackageService.findById(99L))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("no encontrado");
    }

    // ── update branches (null vs non-null) ────────────────────────────────────

    @Test
    void update_appliesOnlyNonNullFields() {
        TourPackageEntity existing = validPackage();
        existing.setName("Old Name");
        when(tourPackageRepository.findById(1L)).thenReturn(
            Optional.of(existing)
        );
        when(tourPackageRepository.save(any())).thenReturn(existing);

        TourPackageEntity patch = new TourPackageEntity(); // todos null excepto nombre
        patch.setName("New Name");

        TourPackageEntity result = tourPackageService.update(1L, patch);
        assertThat(result.getName()).isEqualTo("New Name");
    }

    // ── reduceSlots branches ──────────────────────────────────────────────────

    @Test
    void reduceSlots_throwsWhenNotEnoughSlots() {
        TourPackageEntity p = validPackage();
        p.setAvailableSlots(2);
        when(tourPackageRepository.findById(1L)).thenReturn(Optional.of(p));

        assertThatThrownBy(() -> tourPackageService.reduceSlots(1L, 5))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("asientos");
    }

    @Test
    void reduceSlots_setsStatusSoldOutWhenZeroRemaining() {
        TourPackageEntity p = validPackage();
        p.setAvailableSlots(3);
        when(tourPackageRepository.findById(1L)).thenReturn(Optional.of(p));
        when(tourPackageRepository.save(any())).thenReturn(p);

        tourPackageService.reduceSlots(1L, 3); // deja en 0

        assertThat(p.getStatus()).isEqualTo(TourPackageEntity.Status.SOLD_OUT);
    }

    @Test
    void reduceSlots_doesNotChangStatusWhenSlotsRemain() {
        TourPackageEntity p = validPackage();
        p.setAvailableSlots(5);
        p.setStatus(TourPackageEntity.Status.AVAILABLE);
        when(tourPackageRepository.findById(1L)).thenReturn(Optional.of(p));
        when(tourPackageRepository.save(any())).thenReturn(p);

        tourPackageService.reduceSlots(1L, 2);

        assertThat(p.getStatus()).isEqualTo(TourPackageEntity.Status.AVAILABLE);
    }

    // ── releaseSlots branches ─────────────────────────────────────────────────

    @Test
    void releaseSlots_whenSoldOut_statusStaysSoldOut() {
        TourPackageEntity p = validPackage();
        p.setAvailableSlots(0);
        p.setStatus(TourPackageEntity.Status.SOLD_OUT);
        when(tourPackageRepository.findById(1L)).thenReturn(Optional.of(p));
        when(tourPackageRepository.save(any())).thenReturn(p);

        tourPackageService.releaseSlots(1L, 2);

        assertThat(p.getStatus()).isEqualTo(TourPackageEntity.Status.SOLD_OUT);
    }

    @Test
    void releaseSlots_whenNotSoldOut_statusUnchanged() {
        TourPackageEntity p = validPackage();
        p.setAvailableSlots(3);
        p.setStatus(TourPackageEntity.Status.AVAILABLE);
        when(tourPackageRepository.findById(1L)).thenReturn(Optional.of(p));
        when(tourPackageRepository.save(any())).thenReturn(p);

        tourPackageService.releaseSlots(1L, 2);

        assertThat(p.getStatus()).isEqualTo(TourPackageEntity.Status.AVAILABLE);
    }

    @Test
    void update_appliesAllNonNullFields() {
        TourPackageEntity existing = validPackage();
        when(tourPackageRepository.findById(1L)).thenReturn(
            Optional.of(existing)
        );
        when(tourPackageRepository.save(any())).thenReturn(existing);

        TourPackageEntity patch = new TourPackageEntity();
        patch.setName("New Name");
        patch.setDestination("Paris");
        patch.setDescription("Descripción");
        patch.setPrice(new BigDecimal("999"));
        patch.setIncludedServices("Hotel + Vuelo");
        patch.setConditions("Sin mascotas");
        patch.setRestrictions("Mayores de 18");
        patch.setTripType("Aventura");
        patch.setSeason("Verano");
        patch.setCategory("Premium");

        TourPackageEntity result = tourPackageService.update(1L, patch);

        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getDestination()).isEqualTo("Paris");
        assertThat(result.getDescription()).isEqualTo("Descripción");
        assertThat(result.getPrice()).isEqualByComparingTo(
            new BigDecimal("999")
        );
        assertThat(result.getIncludedServices()).isEqualTo("Hotel + Vuelo");
        assertThat(result.getConditions()).isEqualTo("Sin mascotas");
        assertThat(result.getRestrictions()).isEqualTo("Mayores de 18");
        assertThat(result.getTripType()).isEqualTo("Aventura");
        assertThat(result.getSeason()).isEqualTo("Verano");
        assertThat(result.getCategory()).isEqualTo("Premium");
    }
}
