package com.plp.iotplatform.dto;

import lombok.Data;
import java.time.Instant;
import java.util.List;

@Data
public class HubDto {
    private String id;
    private String macAddress;
    private String name;
    private String location;
    private String description;
    private String status; // PENDING_VALIDATION, ACTIVE, INACTIVE, OFFLINE
    private Instant createdAt;
    private Instant lastSeen; // Mis à jour par heartbeat ou données capteur
    private List<SensorSummaryDto> sensors; // Résumé des capteurs pour éviter la circularité complète
}