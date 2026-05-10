package com.travelagency.backend.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "vouchers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoucherEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long reservationId;

    private LocalDateTime generatedAt;

    @Column(columnDefinition = "TEXT")
    private String content;

    @PrePersist
    protected void onCreate() {
        if (this.generatedAt == null) this.generatedAt = LocalDateTime.now();
    }
}
