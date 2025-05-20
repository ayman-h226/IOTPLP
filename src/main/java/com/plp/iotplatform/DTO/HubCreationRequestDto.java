package com.plp.iotplatform.DTO;

import jakarta.validation.constraints.NotBlank; // Ajouter spring-boot-starter-validation
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class HubCreationRequestDto {
    @NotBlank(message = "MAC address is mandatory")
    @Pattern(regexp = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$", message = "Invalid MAC address format")
    private String macAddress;

    @NotBlank(message = "Hub name is mandatory")
    private String name;

    private String location;
    private String description;
}