package com.plp.iotplatform.dto;

import lombok.Data;

@Data
public class HubCreationRequestDto {
    private String macAddress;
    private String name;
    private String description;
    private String location;


}
