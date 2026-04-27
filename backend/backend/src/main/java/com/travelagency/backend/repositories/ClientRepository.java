package com.travelagency.backend.repositories;

import com.travelagency.backend.entities.ClientEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<ClientEntity, Long> {
    Optional<ClientEntity> findByEmail(String email);
    boolean existsByEmail(String email);
}
