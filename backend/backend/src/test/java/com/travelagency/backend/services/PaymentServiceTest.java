package com.travelagency.backend.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.travelagency.backend.entities.*;
import com.travelagency.backend.repositories.PaymentRepository;
import com.travelagency.backend.repositories.VoucherRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private VoucherRepository voucherRepository;

    @Mock
    private ReservationService reservationService;

    @Mock
    private TourPackageService tourPackageService;

    @Mock
    private UserService userService;

    @InjectMocks
    private PaymentService paymentService;

    private ReservationEntity reservation;
    private TourPackageEntity pkg;
    private UserEntity user;

    @BeforeEach
    void setUp() {
        reservation = ReservationEntity.builder()
            .id(1L)
            .userId(1L)
            .tourPackageId(1L)
            .passengerCount(2)
            .baseAmount(new BigDecimal("400"))
            .discountAmount(new BigDecimal("40"))
            .discountDetail("Descuento grupo")
            .finalAmount(new BigDecimal("360"))
            .status(ReservationEntity.Status.PENDING)
            .reservationCode("ABC12345")
            .build();

        pkg = TourPackageEntity.builder()
            .id(1L)
            .name("Tour Caribe")
            .destination("Caribe")
            .startDate(LocalDate.now().plusDays(10))
            .endDate(LocalDate.now().plusDays(20))
            .build();

        user = UserEntity.builder().id(1L).name("Cliente Test").build();
    }

    // Covered cases: processPayment, findByReservation

    @Test
    void processPayment_success() {
        when(reservationService.findById(1L)).thenReturn(reservation);
        when(paymentRepository.existsByReservationId(1L)).thenReturn(false);
        when(paymentRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(tourPackageService.findById(1L)).thenReturn(pkg);
        when(userService.findById(1L)).thenReturn(user);

        PaymentEntity result = paymentService.processPayment(
            1L,
            "4111111111111234",
            "12/27",
            "123"
        );

        assertNotNull(result);
        assertEquals(PaymentEntity.Status.APPROVED, result.getStatus());
        assertTrue(result.getCardNumber().startsWith("****"));
        verify(reservationService).confirm(1L);
        verify(voucherRepository).save(any());
    }

    @Test
    void processPayment_cancelledReservation_throwsException() {
        reservation.setStatus(ReservationEntity.Status.CANCELLED);
        when(reservationService.findById(1L)).thenReturn(reservation);
        assertThrows(RuntimeException.class, () ->
            paymentService.processPayment(1L, "4111", "12/27", "123")
        );
    }

    @Test
    void processPayment_confirmedReservation_throwsException() {
        reservation.setStatus(ReservationEntity.Status.CONFIRMED);
        when(reservationService.findById(1L)).thenReturn(reservation);
        assertThrows(RuntimeException.class, () ->
            paymentService.processPayment(1L, "4111", "12/27", "123")
        );
    }

    @Test
    void processPayment_expiredReservation_throwsException() {
        reservation.setStatus(ReservationEntity.Status.EXPIRED);
        when(reservationService.findById(1L)).thenReturn(reservation);
        assertThrows(RuntimeException.class, () ->
            paymentService.processPayment(1L, "4111", "12/27", "123")
        );
    }

    @Test
    void processPayment_alreadyPaid_throwsException() {
        when(reservationService.findById(1L)).thenReturn(reservation);
        when(paymentRepository.existsByReservationId(1L)).thenReturn(true);
        assertThrows(RuntimeException.class, () ->
            paymentService.processPayment(1L, "4111", "12/27", "123")
        );
    }

    @Test
    void processPayment_zeroAmount_throwsException() {
        reservation.setFinalAmount(BigDecimal.ZERO);
        when(reservationService.findById(1L)).thenReturn(reservation);
        when(paymentRepository.existsByReservationId(1L)).thenReturn(false);
        assertThrows(RuntimeException.class, () ->
            paymentService.processPayment(1L, "4111", "12/27", "123")
        );
    }

    @Test
    void processPayment_maskCard_shortNumber() {
        reservation.setFinalAmount(new BigDecimal("360"));
        when(reservationService.findById(1L)).thenReturn(reservation);
        when(paymentRepository.existsByReservationId(1L)).thenReturn(false);
        when(paymentRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(tourPackageService.findById(1L)).thenReturn(pkg);
        when(userService.findById(1L)).thenReturn(user);

        PaymentEntity result = paymentService.processPayment(
            1L,
            "123",
            "12/27",
            "123"
        );
        assertEquals("****", result.getCardNumber());
    }

    @Test
    void findByReservation_success() {
        PaymentEntity payment = PaymentEntity.builder()
            .id(1L)
            .reservationId(1L)
            .build();
        when(paymentRepository.findByReservationId(1L)).thenReturn(
            Optional.of(payment)
        );
        assertEquals(payment, paymentService.findByReservation(1L));
    }

    @Test
    void findByReservation_notFound_throwsException() {
        when(paymentRepository.findByReservationId(99L)).thenReturn(
            Optional.empty()
        );
        assertThrows(RuntimeException.class, () ->
            paymentService.findByReservation(99L)
        );
    }

    @Test
    void processPayment_nullCard_masksAsStars() {
        when(reservationService.findById(1L)).thenReturn(reservation);
        when(paymentRepository.existsByReservationId(1L)).thenReturn(false);
        when(paymentRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(tourPackageService.findById(1L)).thenReturn(pkg);
        when(userService.findById(1L)).thenReturn(user);

        PaymentEntity result = paymentService.processPayment(
            1L,
            null,
            "12/27",
            "123"
        );

        assertEquals("****", result.getCardNumber());
    }
}
