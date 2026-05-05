package com.travelagency.backend.controllers;

import  com.travelagency.backend.services.PromotionService;
import com.travelagency.backend.entities.PromotionEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService promotionService;

    @PostMapping
    public ResponseEntity<PromotionEntity> createPromotion(@RequestBody PromotionEntity promotion){
        return ResponseEntity.status(HttpStatus.CREATED).body(promotionService.createPromotion(promotion));
    }

    @GetMapping
    public ResponseEntity<List<PromotionEntity>> findAll(){
        return ResponseEntity.ok(promotionService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PromotionEntity> findById(@PathVariable Long id){
        return ResponseEntity.ok(promotionService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PromotionEntity> updatePromotion(@PathVariable Long id, @RequestBody PromotionEntity promotionUpdated){
        return ResponseEntity.ok(promotionService.updatePromotion(id, promotionUpdated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePromotion(@PathVariable Long id){
        promotionService.deletePromotion(id);
        return ResponseEntity.noContent().build();
    }
}
