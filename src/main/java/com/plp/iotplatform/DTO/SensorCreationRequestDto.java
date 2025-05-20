package com.plp.iotplatform.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SensorCreationRequestDto {
    @NotBlank(message = "Sensor local ID is mandatory")
    private String localId; // Ex: "TEMP_INTERNE"

    @NotBlank(message = "Sensor name is mandatory")
    private String name;    // Ex: "Température Salon"

    @NotBlank(message = "Sensor type is mandatory")
    private String type;    // Ex: "TEMPERATURE"

    private String model;
    private String unit;
    private String description;
    // Le hubId sera fourni via le path de l'API (ex: /api/admin/hubs/{hubId}/sensors)
}