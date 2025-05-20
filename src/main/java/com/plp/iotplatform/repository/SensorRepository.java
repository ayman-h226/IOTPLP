package com.plp.iotplatform.repository;

import com.plp.iotplatform.entity.Sensor;
import com.plp.iotplatform.enums.SensorStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface SensorRepository extends JpaRepository<Sensor, String> {
    List<Sensor> findByHubId(String hubId);
    List<Sensor> findByHubIdAndStatus(String hubId, SensorStatus status);
    Optional<Sensor> findByHubIdAndLocalId(String hubId, String localId);
    List<Sensor> findByStatus(SensorStatus status);

    @Query("SELECT s FROM Sensor s WHERE s.status = :status AND (s.lastDataReceivedAt IS NULL OR s.lastDataReceivedAt < :cutoffTime)")
    List<Sensor> findSensorsInactiveByStatusAndLastDataBefore(
            @Param("status") SensorStatus status,
            @Param("cutoffTime") Instant cutoffTime);
}
