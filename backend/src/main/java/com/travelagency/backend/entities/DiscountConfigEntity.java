package com.travelagency.backend.entities;


import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "discount_config")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiscountConfigEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer groupDiscountThreshold;
    private BigDecimal groupDiscountPercentage;
    private Integer individualDiscountThreshold;
    private BigDecimal individualDiscountPercentage;
    private BigDecimal minimumOrderAmount;
    private BigDecimal maximumOrderAmount;
    private Boolean acumulateDiscount;

}
