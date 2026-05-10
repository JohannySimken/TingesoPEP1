package com.travelagency.backend.entities;


import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Table(name = "payments")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class PaymentEntity {

    public enum Method { CREDIT_CARD}
    public enum Status { APPROVED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String transactionCode;

    @Column(nullable = false)
    private Long reservationId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Method paymentMethod;

    private String cardNumber;
    private String cardExpiry;
    private String cvv;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    private LocalDateTime paidAt;

    @PrePersist
    protected void onCreate (){
        if(this.paidAt == null) this.paidAt = LocalDateTime.now();
        if(this.status == null) this.status = Status.APPROVED;
        if(this.paymentMethod == null) this.paymentMethod = Method.CREDIT_CARD;
    }
}
