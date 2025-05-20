package com.plp.iotplatform.DTO;

import lombok.Data;
import java.time.Instant;

@Data
public class HubDto {
    private String id;
    private String macAddress;
    private String name;
    private String location;
    private String description;
    private String status;
    private Instant createdAt;
    private Instant lastSeen;
    // private List<SensorSummaryDto> sensors; // Version simplifiée pour l'instant
}