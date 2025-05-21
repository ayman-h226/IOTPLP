package com.plp.iotplatform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class HubUpdateRequestDto {
    // Similaire à Creation, mais tous les champs sont optionnels pour une mise à jour partielle
    // ou vous pouvez réutiliser HubCreationRequestDto si une mise à jour complète est attendue.
    // Pour une vraie mise à jour partielle (PATCH), la logique est plus complexe.
    // Ici, on suppose un PUT pour une mise à jour complète des champs modifiables.

    @Pattern(regexp = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$", message = "Invalid MAC address format")
    private String macAddress; // L'admin pourrait vouloir corriger une MAC mal saisie

    @NotBlank(message = "Hub name cannot be blank if provided")
    private String name;

    private String location;
    private String description;
    // Le statut est géré par des actions spécifiques (validate, deactivate)
}