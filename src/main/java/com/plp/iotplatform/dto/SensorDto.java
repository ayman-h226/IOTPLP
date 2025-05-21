package com.plp.iotplatform.dto;

import lombok.Data;
import java.time.Instant;

@Data
public class SensorDto {
    private String id;
    private String localId;
    private String name;
    private String type;
    private String model;
    private String unit;
    private String description;
    private String status; // PENDING_VALIDATION, ACTIVE, INACTIVE, OFFLINE
    private String hubId;
    private String hubName; // Ajouter le nom du hub pour info
    private Instant createdAt;
    private Instant lastDataReceivedAt;
}