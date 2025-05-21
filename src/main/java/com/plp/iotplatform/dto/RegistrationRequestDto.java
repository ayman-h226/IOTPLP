package com.plp.iotplatform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import com.plp.iotplatform.enums.DeviceType;

@Data
public class RegistrationRequestDto {
    @NotNull
    private DeviceType type; // "hub" ou "sensor"

    // Pour hub et sensor (le hub auquel le sensor est attaché)
    @Pattern(regexp = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$", message = "Invalid MAC address format")
    private String hubMacAddress;

    // Pour hub seulement
    private String hubName; // Optionnel, si le hub peut se nommer

    // Pour sensor seulement
    private String sensorLocalId;
    private String sensorType;
    private String sensorModel;
    // Autres infos que l'agent peut fournir
}