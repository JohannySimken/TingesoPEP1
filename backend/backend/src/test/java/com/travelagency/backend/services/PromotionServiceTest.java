package com.travelagency.backend.services;

import com.travelagency.backend.entities.PromotionEntity;
import com.travelagency.backend.repositories.PromotionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PromotionServiceTest {

    @Mock
    private PromotionRepository promotionRepository;

    @InjectMocks
    private PromotionService promotionService;

    private PromotionEntity promotion;

    @BeforeEach
    void setUp() {
        promotion = PromotionEntity.builder()
                .id(1L)
                .name("Promo Verano")
                .discountPercentage(new BigDecimal("15"))
                .startDate(LocalDate.now().minusDays(1))
                .endDate(LocalDate.now().plusDays(10))
                .active(true)
                .build();
    }

    @Test
    void createPromotion_success() {
        when(promotionRepository.save(promotion)).thenReturn(promotion);
        PromotionEntity result = promotionService.createPromotion(promotion);
        assertEquals("Promo Verano", result.getName());
    }

    @Test
    void createPromotion_endDateBeforeStartDate_throwsException() {
        promotion.setStartDate(LocalDate.now().plusDays(5));
        promotion.setEndDate(LocalDate.now());
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> promotionService.createPromotion(promotion));
        assertTrue(ex.getMessage().contains("fecha"));
    }

    @Test
    void findAll_returnsList() {
        when(promotionRepository.findAll()).thenReturn(List.of(promotion));
        assertEquals(1, promotionService.findAll().size());
    }

    @Test
    void findActive_returnsActivePromotions() {
        when(promotionRepository.findActivePromotions(LocalDate.now())).thenReturn(List.of(promotion));
        List result = promotionService.findActive(LocalDate.now());
        assertEquals(1, result.size());
    }

    @Test
    void findById_success() {
        when(promotionRepository.findById(1L)).thenReturn(Optional.of(promotion));
        assertEquals(promotion, promotionService.findById(1L));
    }

    @Test
    void findById_notFound_throwsException() {
        when(promotionRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> promotionService.findById(99L));
    }

    @Test
    void updatePromotion_success() {
        PromotionEntity updated = PromotionEntity.builder()
                .name("Promo Invierno")
                .discountPercentage(new BigDecimal("20"))
                .build();
        when(promotionRepository.findById(1L)).thenReturn(Optional.of(promotion));
        when(promotionRepository.save(any())).thenReturn(promotion);

        promotionService.updatePromotion(1L, updated);
        assertEquals("Promo Invierno", promotion.getName());
        assertEquals(new BigDecimal("20"), promotion.getDiscountPercentage());
    }

    @Test
    void updatePromotion_notFound_throwsException() {
        when(promotionRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> promotionService.updatePromotion(99L, promotion));
    }

    @Test
    void deletePromotion_callsRepository() {
        doNothing().when(promotionRepository).deleteById(1L);
        promotionService.deletePromotion(1L);
        verify(promotionRepository).deleteById(1L);
    }

    // ── Nuevos tests: branches null en updatePromotion ────────────────────────

    @Test
    void updatePromotion_withAllNullFields_keepsOriginalValues() {
        PromotionEntity patch = new PromotionEntity(); // todos los campos null
        when(promotionRepository.findById(1L)).thenReturn(Optional.of(promotion));
        when(promotionRepository.save(any())).thenReturn(promotion);

        PromotionEntity result = promotionService.updatePromotion(1L, patch);
        assertEquals("Promo Verano", result.getName());
        assertEquals(new BigDecimal("15"), result.getDiscountPercentage());
        assertTrue(result.getActive());
    }

    @Test
    void updatePromotion_withOnlyStartDate_updatesStartDate() {
        PromotionEntity patch = new PromotionEntity();
        patch.setStartDate(LocalDate.of(2026, 8, 1));
        when(promotionRepository.findById(1L)).thenReturn(Optional.of(promotion));
        when(promotionRepository.save(any())).thenReturn(promotion);

        promotionService.updatePromotion(1L, patch);
        assertEquals(LocalDate.of(2026, 8, 1), promotion.getStartDate());
    }

    @Test
    void updatePromotion_withOnlyEndDate_updatesEndDate() {
        PromotionEntity patch = new PromotionEntity();
        patch.setEndDate(LocalDate.of(2026, 12, 31));
        when(promotionRepository.findById(1L)).thenReturn(Optional.of(promotion));
        when(promotionRepository.save(any())).thenReturn(promotion);

        promotionService.updatePromotion(1L, patch);
        assertEquals(LocalDate.of(2026, 12, 31), promotion.getEndDate());
    }

    @Test
    void updatePromotion_withActiveFlag_updatesActive() {
        PromotionEntity patch = new PromotionEntity();
        patch.setActive(false);
        when(promotionRepository.findById(1L)).thenReturn(Optional.of(promotion));
        when(promotionRepository.save(any())).thenReturn(promotion);

        promotionService.updatePromotion(1L, patch);
        assertFalse(promotion.getActive());
    }
}