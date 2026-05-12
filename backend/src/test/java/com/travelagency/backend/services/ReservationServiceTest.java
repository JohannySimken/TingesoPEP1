package com.travelagency.backend.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.travelagency.backend.entities.*;
import com.travelagency.backend.repositories.ReservationRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private UserService userService;

    @Mock
    private TourPackageService tourPackageService;

    @Mock
    private PromotionService promotionService;

    @InjectMocks
    private ReservationService reservationService;

    private TourPackageEntity pkg;
    private UserEntity user;
    private ReservationEntity reservation;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(reservationService, "expiryHours", 24);
        ReflectionTestUtils.setField(reservationService, "groupThreshold", 4);
        ReflectionTestUtils.setField(
            reservationService,
            "groupPercentage",
            new BigDecimal("10")
        );
        ReflectionTestUtils.setField(
            reservationService,
            "frequentClientThreshold",
            3
        );
        ReflectionTestUtils.setField(
            reservationService,
            "frequentClientPercentage",
            new BigDecimal("5")
        );
        ReflectionTestUtils.setField(
            reservationService,
            "multiPackagePercentage",
            new BigDecimal("5")
        );
        ReflectionTestUtils.setField(
            reservationService,
            "maxAccumulated",
            new BigDecimal("20")
        );
        ReflectionTestUtils.setField(reservationService, "accumulate", true);

        user = UserEntity.builder()
            .id(1L)
            .name("Cliente")
            .email("cliente@test.com")
            .build();

        pkg = TourPackageEntity.builder()
            .id(1L)
            .name("Tour Caribe")
            .price(new BigDecimal("200"))
            .availableSlots(10)
            .status(TourPackageEntity.Status.AVAILABLE)
            .build();

        reservation = ReservationEntity.builder()
            .id(1L)
            .userId(1L)
            .tourPackageId(1L)
            .passengerCount(2)
            .baseAmount(new BigDecimal("400"))
            .discountAmount(new BigDecimal("0"))
            .finalAmount(new BigDecimal("400"))
            .status(ReservationEntity.Status.PENDING)
            .expiresAt(LocalDateTime.now().plusHours(24))
            .build();
    }

    // Covered cases: Create, invalid passengers count, package sold out, discount exceeds max,

    // --- create ---
    @Test
    void create_success_noDiscounts() {
        when(userService.findById(1L)).thenReturn(user);
        when(tourPackageService.findById(1L)).thenReturn(pkg);
        when(
            reservationRepository.countByUserIdAndStatus(
                1L,
                ReservationEntity.Status.CONFIRMED
            )
        ).thenReturn(0L);
        when(
            reservationRepository.findConfirmedByPeriod(any(), any())
        ).thenReturn(List.of());
        when(promotionService.findActive(any())).thenReturn(List.of());
        when(reservationRepository.save(any())).thenReturn(reservation);

        ReservationEntity result = reservationService.create(1L, 1L, 2);
        assertNotNull(result);
        verify(tourPackageService).reduceSlots(1L, 2);
    }

    @Test
    void create_withGroupDiscount() {
        when(userService.findById(1L)).thenReturn(user);
        when(tourPackageService.findById(1L)).thenReturn(pkg);
        when(
            reservationRepository.countByUserIdAndStatus(any(), any())
        ).thenReturn(0L);
        when(
            reservationRepository.findConfirmedByPeriod(any(), any())
        ).thenReturn(List.of());
        when(promotionService.findActive(any())).thenReturn(List.of());
        when(reservationRepository.save(any())).thenAnswer(i ->
            i.getArgument(0)
        );

        ReservationEntity result = reservationService.create(1L, 1L, 4);
        assertTrue(result.getDiscountDetail().contains("grupo"));
    }

    @Test
    void create_withFrequentClientDiscount() {
        when(userService.findById(1L)).thenReturn(user);
        when(tourPackageService.findById(1L)).thenReturn(pkg);
        when(
            reservationRepository.countByUserIdAndStatus(
                1L,
                ReservationEntity.Status.CONFIRMED
            )
        ).thenReturn(3L);
        when(
            reservationRepository.findConfirmedByPeriod(any(), any())
        ).thenReturn(List.of());
        when(promotionService.findActive(any())).thenReturn(List.of());
        when(reservationRepository.save(any())).thenAnswer(i ->
            i.getArgument(0)
        );

        ReservationEntity result = reservationService.create(1L, 1L, 2);
        assertTrue(result.getDiscountDetail().contains("frecuente"));
    }

    @Test
    void create_withPromoDiscount() {
        PromotionEntity promo = PromotionEntity.builder()
            .name("Black Friday")
            .discountPercentage(new BigDecimal("10"))
            .build();
        when(userService.findById(1L)).thenReturn(user);
        when(tourPackageService.findById(1L)).thenReturn(pkg);
        when(
            reservationRepository.countByUserIdAndStatus(any(), any())
        ).thenReturn(0L);
        when(
            reservationRepository.findConfirmedByPeriod(any(), any())
        ).thenReturn(List.of());
        when(promotionService.findActive(any())).thenReturn(List.of(promo));
        when(reservationRepository.save(any())).thenAnswer(i ->
            i.getArgument(0)
        );

        ReservationEntity result = reservationService.create(1L, 1L, 2);
        assertTrue(result.getDiscountDetail().contains("Black Friday"));
    }

    @Test
    void create_withMultiPackageDiscount() {
        ReservationEntity recent = ReservationEntity.builder()
            .userId(1L)
            .status(ReservationEntity.Status.CONFIRMED)
            .build();
        when(userService.findById(1L)).thenReturn(user);
        when(tourPackageService.findById(1L)).thenReturn(pkg);
        when(
            reservationRepository.countByUserIdAndStatus(any(), any())
        ).thenReturn(0L);
        when(
            reservationRepository.findConfirmedByPeriod(any(), any())
        ).thenReturn(List.of(recent));
        when(promotionService.findActive(any())).thenReturn(List.of());
        when(reservationRepository.save(any())).thenAnswer(i ->
            i.getArgument(0)
        );

        ReservationEntity result = reservationService.create(1L, 1L, 2);
        assertTrue(result.getDiscountDetail().contains("multiples"));
    }

    @Test
    void create_discountExceedsMax_cappedAt20() {
        PromotionEntity promo = PromotionEntity.builder()
            .name("Mega Promo")
            .discountPercentage(new BigDecimal("15"))
            .build();
        ReservationEntity recent = ReservationEntity.builder()
            .userId(1L)
            .status(ReservationEntity.Status.CONFIRMED)
            .build();
        when(userService.findById(1L)).thenReturn(user);
        when(tourPackageService.findById(1L)).thenReturn(pkg);
        when(
            reservationRepository.countByUserIdAndStatus(
                1L,
                ReservationEntity.Status.CONFIRMED
            )
        ).thenReturn(3L);
        when(
            reservationRepository.findConfirmedByPeriod(any(), any())
        ).thenReturn(List.of(recent));
        when(promotionService.findActive(any())).thenReturn(List.of(promo));
        when(reservationRepository.save(any())).thenAnswer(i ->
            i.getArgument(0)
        );

        ReservationEntity result = reservationService.create(1L, 1L, 4);
        BigDecimal expectedMax = new BigDecimal("800")
            .multiply(new BigDecimal("20"))
            .divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
        assertEquals(expectedMax, result.getDiscountAmount());
    }

    @Test
    void create_packageCancelled_throwsException() {
        pkg.setStatus(TourPackageEntity.Status.CANCELLED);
        when(userService.findById(1L)).thenReturn(user);
        when(tourPackageService.findById(1L)).thenReturn(pkg);
        assertThrows(RuntimeException.class, () ->
            reservationService.create(1L, 1L, 2)
        );
    }

    @Test
    void create_packageSoldOut_throwsException() {
        pkg.setStatus(TourPackageEntity.Status.SOLD_OUT);
        when(userService.findById(1L)).thenReturn(user);
        when(tourPackageService.findById(1L)).thenReturn(pkg);
        assertThrows(RuntimeException.class, () ->
            reservationService.create(1L, 1L, 2)
        );
    }

    @Test
    void create_zeroPassengers_throwsException() {
        when(userService.findById(1L)).thenReturn(user);
        when(tourPackageService.findById(1L)).thenReturn(pkg);
        assertThrows(RuntimeException.class, () ->
            reservationService.create(1L, 1L, 0)
        );
    }

    @Test
    void create_notEnoughSlots_throwsException() {
        pkg.setAvailableSlots(1);
        when(userService.findById(1L)).thenReturn(user);
        when(tourPackageService.findById(1L)).thenReturn(pkg);
        assertThrows(RuntimeException.class, () ->
            reservationService.create(1L, 1L, 5)
        );
    }

    // --- findById ---
    @Test
    void findById_success() {
        when(reservationRepository.findById(1L)).thenReturn(
            Optional.of(reservation)
        );
        assertEquals(reservation, reservationService.findById(1L));
    }

    @Test
    void findById_notFound_throwsException() {
        when(reservationRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () ->
            reservationService.findById(99L)
        );
    }

    // --- findByUserId ---
    @Test
    void findByUserId_returnsList() {
        when(reservationRepository.findByUserId(1L)).thenReturn(
            List.of(reservation)
        );
        assertEquals(1, reservationService.findByUserId(1L).size());
    }

    // --- findAll ---
    @Test
    void findAll_returnsList() {
        when(reservationRepository.findAll()).thenReturn(List.of(reservation));
        assertEquals(1, reservationService.findAll().size());
    }

    // --- confirm ---
    @Test
    void confirm_success() {
        when(reservationRepository.findById(1L)).thenReturn(
            Optional.of(reservation)
        );
        when(reservationRepository.save(any())).thenReturn(reservation);
        reservationService.confirm(1L);
        assertEquals(
            ReservationEntity.Status.CONFIRMED,
            reservation.getStatus()
        );
    }

    // --- cancel ---
    @Test
    void cancel_success() {
        when(reservationRepository.findById(1L)).thenReturn(
            Optional.of(reservation)
        );
        when(reservationRepository.save(any())).thenReturn(reservation);
        reservationService.cancel(1L);
        assertEquals(
            ReservationEntity.Status.CANCELLED,
            reservation.getStatus()
        );
        verify(tourPackageService).releaseSlots(any(), anyInt());
    }

    @Test
    void cancel_confirmedReservation_throwsException() {
        reservation.setStatus(ReservationEntity.Status.CONFIRMED);
        when(reservationRepository.findById(1L)).thenReturn(
            Optional.of(reservation)
        );
        assertThrows(RuntimeException.class, () ->
            reservationService.cancel(1L)
        );
    }

    // --- expireReservation ---
    @Test
    void expireReservation_expiresPendingReservations() {
        reservation.setStatus(ReservationEntity.Status.PENDING);
        when(
            reservationRepository.findByStatusAndExpiresAtBefore(
                eq(ReservationEntity.Status.PENDING),
                any()
            )
        ).thenReturn(List.of(reservation));
        reservationService.expireReservation();
        assertEquals(ReservationEntity.Status.EXPIRED, reservation.getStatus());
        verify(tourPackageService).releaseSlots(any(), anyInt());
    }

    @Test
    void expireReservation_noExpired_doesNothing() {
        when(
            reservationRepository.findByStatusAndExpiresAtBefore(any(), any())
        ).thenReturn(List.of());
        reservationService.expireReservation();
        verify(tourPackageService, never()).releaseSlots(any(), anyInt());
    }

    @Test
    void create_packageNotValid_throwsException() {
        pkg.setStatus(TourPackageEntity.Status.NOT_VALID);
        when(userService.findById(1L)).thenReturn(user);
        when(tourPackageService.findById(1L)).thenReturn(pkg);
        assertThrows(RuntimeException.class, () ->
            reservationService.create(1L, 1L, 2)
        );
    }

    @Test
    void create_withAccumulateFalse_usesMaxInsteadOfAdd() {
        ReflectionTestUtils.setField(reservationService, "accumulate", false);

        PromotionEntity promo = PromotionEntity.builder()
            .name("Promo Test")
            .discountPercentage(new BigDecimal("10"))
            .build();
        ReservationEntity recent = ReservationEntity.builder()
            .userId(1L)
            .status(ReservationEntity.Status.CONFIRMED)
            .build();

        when(userService.findById(1L)).thenReturn(user);
        when(tourPackageService.findById(1L)).thenReturn(pkg);
        when(
            reservationRepository.countByUserIdAndStatus(
                1L,
                ReservationEntity.Status.CONFIRMED
            )
        ).thenReturn(3L);
        when(
            reservationRepository.findConfirmedByPeriod(any(), any())
        ).thenReturn(List.of(recent));
        when(promotionService.findActive(any())).thenReturn(List.of(promo));
        when(reservationRepository.save(any())).thenAnswer(i ->
            i.getArgument(0)
        );

        // accumulate=false → totalPct = max(group=10, frequent=5, multi=5, promo=10) = 10
        // basePrice = 200 * 4 = 800 → discount = 800 * 10/100 = 80.00
        ReservationEntity result = reservationService.create(1L, 1L, 4);

        assertEquals(new BigDecimal("80.00"), result.getDiscountAmount());
    }
}
