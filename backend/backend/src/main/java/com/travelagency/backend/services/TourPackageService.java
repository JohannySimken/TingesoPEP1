package com.travelagency.backend.services;

import com.travelagency.backend.entities.TourPackageEntity;
import com.travelagency.backend.repositories.TourPackageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TourPackageService {
    private final TourPackageRepository tourPackageRepository;

    public TourPackageEntity createPackage(TourPackageEntity tourPackage){
        tourPackage.setAvailableSlots(tourPackage.getAvailableSlots());
        tourPackage.setStatus(TourPackageEntity.Status.AVAILABLE);
        tourPackage.setDuration((int)(tourPackage.getEndDate().toEpochDay() - tourPackage.getStartDate().toEpochDay()));
        return tourPackageRepository.save(tourPackage);
    }

    public TourPackageEntity findById(Long id){
        return tourPackageRepository.findById(id).orElseThrow(()-> new RuntimeException("Paquete no encontrado"));
    }

    public List<TourPackageEntity> findAll(){
        return tourPackageRepository.findAll();
    }

    public List<TourPackageEntity> findAvailable(){
        return tourPackageRepository.findByStatus(TourPackageEntity.Status.AVAILABLE);
    }

    public List<TourPackageEntity> searchPackages(String destination, BigDecimal minPrice, BigDecimal maxPrice,
                                                  LocalDate startDate, LocalDate endDate, String tripType){
        return tourPackageRepository.searchPackages(destination, minPrice, maxPrice, startDate, endDate, tripType);
    }

    public TourPackageEntity update(Long id, TourPackageEntity tourPackageUpdated){
        TourPackageEntity pkg = findById(id);
        if(tourPackageUpdated.getName() != null) pkg.setName(tourPackageUpdated.getName());
        if(tourPackageUpdated.getDestination() != null) pkg.setDestination(tourPackageUpdated.getDestination());
        if(tourPackageUpdated.getDescription() != null) pkg.setDescription(tourPackageUpdated.getDescription());
        if(tourPackageUpdated.getPrice() != null) pkg.setPrice(tourPackageUpdated.getPrice());
        if(tourPackageUpdated.getIncludedServices() != null) pkg.setIncludedServices(tourPackageUpdated.getIncludedServices());
        if(tourPackageUpdated.getConditions() != null) pkg.setConditions(tourPackageUpdated.getConditions());
        if(tourPackageUpdated.getRestrictions() != null) pkg.setRestrictions(tourPackageUpdated.getRestrictions());
        if(tourPackageUpdated.getTripType() != null) pkg.setTripType(tourPackageUpdated.getTripType());
        if(tourPackageUpdated.getSeason() != null) pkg.setSeason(tourPackageUpdated.getSeason());
        if(tourPackageUpdated.getCategory() != null) pkg.setCategory(tourPackageUpdated.getCategory());
        return tourPackageRepository.save(pkg);
    }

    public TourPackageEntity changeStatus(Long id, TourPackageEntity.Status status){
        TourPackageEntity pkg = findById(id);
        pkg.setStatus(status);
        return tourPackageRepository.save(pkg);
    }

    public void reduceSlots(Long id, int slots){
        TourPackageEntity pkg = findById(id);
        if(pkg.getAvailableSlots() < slots){
            throw new RuntimeException("No hay asientos suficientes");
        }
        pkg.setAvailableSlots(pkg.getAvailableSlots() - slots);
        if(pkg.getAvailableSlots() == 0) pkg.setStatus(TourPackageEntity.Status.SOLD_OUT);
        tourPackageRepository.save(pkg);
    }

    public void releaseSlots(Long id, int slots){
        TourPackageEntity pkg = findById(id);
        pkg.setAvailableSlots(pkg.getAvailableSlots() + slots);
        if(pkg.getAvailableSlots() > 0) pkg.setStatus(TourPackageEntity.Status.AVAILABLE);
        tourPackageRepository.save(pkg);
    }

    private void validateAvailability(TourPackageEntity tourPackage){
        if(tourPackage.getPrice() == null || tourPackage.getPrice().compareTo(BigDecimal.ZERO) <= 0){
            throw new RuntimeException("El precio debe ser mayor a 0");
        }
        if(tourPackage.getTotalSlots() == null || tourPackage.getTotalSlots() <= 0){
            throw new RuntimeException("El total de asientos debe ser mayor a 0");
        }
        if(tourPackage.getEndDate().isBefore(tourPackage.getStartDate())){
            throw new RuntimeException("La fecha de inicio debe ser anterior a la fecha de fin");
        }
    }
}
