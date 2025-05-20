package com.plp.iotplatform.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.Instant;

@Entity
@Table(name = "sensors")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sensor {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id; // ID interne global du capteur

    // ID local au hub, utilisé dans le topic MQTT. Doit être unique PAR HUB.
    @Column(nullable = false)
    private String localId; // Ex: "TEMP_INTERNE", "PRESSION_CIRCUIT1"

    @Column(nullable = false)
    private String name; // Nom descriptif, ex: "Température Salon"

    @Column(nullable = false)
    private String type; // Ex: "TEMPERATURE", "PRESSURE", "HUMIDITY"

    private String model;
    private String unit; // Ex: "°C", "hPa"
    private String description;

    // Ex: PENDING_VALIDATION, ACTIVE, INACTIVE
    @Column(nullable = false)
    private String status = "PENDING_VALIDATION";

    private Instant createdAt = Instant.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hub_id", nullable = false)
    private Hub hub;
}