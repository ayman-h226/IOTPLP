package com.plp.iotplatform.dto;

import lombok.Data;

@Data
public class SensorCreationRequestDto {
    private String localId;
    private String name;
    private String type;
    private String model;
    private String unit;
    private String description;

}
