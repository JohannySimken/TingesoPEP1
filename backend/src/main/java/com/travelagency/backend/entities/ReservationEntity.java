package com.travelagency.backend.entities;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationEntity {

    public enum Status { PENDING, CONFIRMED, CANCELLED, EXPIRED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(nullable = false, unique = true)
    private String reservationCode;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long tourPackageId;

    @Column(nullable = false)
    private Integer passengerCount;

    @Column(nullable = false)
    private BigDecimal baseAmount;

    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(nullable = false)
    private String discountDetail;

    @Column(nullable = false)
    private BigDecimal finalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime expiresAt;

    @PrePersist
    protected void onCreate (){
        this.createdAt = LocalDateTime.now();
        if (this.status == null) this.status = Status.PENDING;
        if (this.discountAmount == null) this.discountAmount = BigDecimal.ZERO;
    }



}
