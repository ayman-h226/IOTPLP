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

    @Column(nullable = false)
    private String localId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hub_id", nullable = false)
    private Hub hub;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String type;

    private String model;
    private String unit;
    private String description;

    @Column(nullable = false)
    private String status = "PENDING_VALIDATION";

    private Instant createdAt = Instant.now();

    // NOUVEAU CHAMP
    private Instant lastDataReceivedAt;
}