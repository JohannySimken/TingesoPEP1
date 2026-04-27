package com.travelagency.backend.entities;


import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "tour_packages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourPackageEntity {

    public enum Status { AVAILABLE, SOLD_OUT, NOT_VALID, CANCELLED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String destination;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    private Integer duration;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer totalSlots;

    @Column(nullable = false)
    private Integer availableSlots;

    @Column(columnDefinition = "TEXT")
    private String includedServices;

    @Column(columnDefinition = "TEXT")
    private String conditions;

    @Column(columnDefinition = "TEXT")
    private String restrictions;

    private String tripType;
    private String season;
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @PrePersist
    protected void onCreate (){
        if (this.status == null) this.status = Status.AVAILABLE;
        if (this.duration == null && startDate != null && endDate != null) {
            this.duration = (int) (endDate.toEpochDay() - startDate.toEpochDay());
        }

    }
}
