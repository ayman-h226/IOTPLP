package com.plp.iotplatform.repository;

import com.plp.iotplatform.model.entity.Hub;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface HubRepository extends JpaRepository<Hub, String> {
    Optional<Hub> findByMacAddress(String macAddress);
    List<Hub> findByStatus(String status);
    // Plus tard: d'autres méthodes de recherche
}