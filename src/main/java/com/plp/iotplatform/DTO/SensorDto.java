package com.plp.iotplatform.DTO;

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
    private String status;
    private String hubId; // Juste l'ID du hub parent
    private Instant createdAt;
}