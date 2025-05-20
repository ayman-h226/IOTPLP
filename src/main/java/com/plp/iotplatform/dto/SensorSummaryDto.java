package com.plp.iotplatform.dto;

import lombok.Data;

@Data
public class SensorSummaryDto {
    private String id;
    private String localId;
    private String name;
    private String type;
    private String status;
}