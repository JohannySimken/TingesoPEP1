package com.travelagency.backend.services;

import com.travelagency.backend.entities.PromotionEntity;
import com.travelagency.backend.repositories.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class PromotionService {

    private final PromotionRepository promotionRepository;

    public PromotionEntity createPromotion(PromotionEntity promotion){
        if(promotion.getStartDate().isAfter(promotion.getEndDate())){
            throw new RuntimeException("La fecha de inicio debe ser anterior a la fecha de fin");
        }
        return promotionRepository.save(promotion);
    }

    public List<PromotionEntity> findAll(){
        return promotionRepository.findAll();
    }

    public List<PromotionEntity> findActivePromotions(LocalDate date){
        return promotionRepository.findActivePromotions(date);
    }

    public PromotionEntity findById(Long id){
        return promotionRepository.findById(id).orElseThrow(() -> new RuntimeException("Promoción no encontrada"));
    }


    public PromotionEntity updatePromotion(Long id, PromotionEntity promotionUpdated){
        PromotionEntity promotion = findById(id);
        if(promotionUpdated.getName() != null) promotion.setName(promotionUpdated.getName());
        if(promotionUpdated.getStartDate() != null) promotion.setStartDate(promotionUpdated.getStartDate());
        if(promotionUpdated.getDiscountPercentage() != null) promotion.setDiscountPercentage(promotionUpdated.getDiscountPercentage());
        if(promotionUpdated.getEndDate() != null) promotion.setEndDate(promotionUpdated.getEndDate());
        if(promotionUpdated.getIsActive() != null) promotion.setIsActive(promotionUpdated.getIsActive());
        return promotionRepository.save(promotion);
    }

    public void deletePromotion(Long id){
        promotionRepository.deleteById(id);
    }
}
