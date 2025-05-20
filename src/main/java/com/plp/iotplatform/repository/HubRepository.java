package com.plp.iotplatform.repository;

import com.plp.iotplatform.entity.Hub;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;
import com.plp.iotplatform.enums.HubStatus;

public interface HubRepository extends JpaRepository<Hub, String> {
    Optional<Hub> findByMacAddress(String macAddress);
    List<Hub> findByStatus(HubStatus status);
}