package com.plp.iotplatform.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SensorUpdateRequestDto {
    // Similaire à Creation, champs modifiables
    @NotBlank(message = "Sensor local ID cannot be blank if provided")
    private String localId;

    @NotBlank(message = "Sensor name cannot be blank if provided")
    private String name;

    @NotBlank(message = "Sensor type cannot be blank if provided")
    private String type;

    private String model;
    private String unit;
    private String description;
}