package com.plp.iotplatform.enums;

public enum HubStatus {
    PENDING_VALIDATION,
    ACTIVE,
    INACTIVE, // Mis hors service par un admin
    OFFLINE   // Détecté comme ne communiquant plus
}