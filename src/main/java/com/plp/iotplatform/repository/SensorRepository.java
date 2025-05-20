package com.plp.iotplatform.repository;

import com.plp.iotplatform.model.entity.Sensor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SensorRepository extends JpaRepository<Sensor, String> {
    List<Sensor> findByHubId(String hubId);
    List<Sensor> findByHubIdAndStatus(String hubId, String status);
    Optional<Sensor> findByHubIdAndLocalId(String hubId, String localId);
    // Plus tard: d'autres méthodes de recherche
}
