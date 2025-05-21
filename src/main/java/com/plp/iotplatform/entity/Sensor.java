package com.plp.iotplatform.entity;

import com.plp.iotplatform.enums.SensorStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import java.time.Instant;

@Entity
@Table(name = "sensors", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"hub_id", "local_id"}) // localId doit être unique PAR hub
})
@Data
@NoArgsConstructor
public class Sensor {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "local_id", nullable = false)
    private String localId; // Identifiant unique du capteur au sein de son hub

    @ToString.Exclude // Pour éviter les problèmes de toString() en boucle
    @EqualsAndHashCode.Exclude // Idem
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hub_id", nullable = false)
    private Hub hub;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String type; // Ex: TEMPERATURE, HUMIDITY

    private String model;
    private String unit;
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SensorStatus status = SensorStatus.PENDING_VALIDATION; // Statut initial

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    private Instant lastDataReceivedAt;

    public Sensor(Hub hub, String localId, String name, String type, String model, String unit, String description) {
        this.hub = hub;
        this.localId = localId;
        this.name = name;
        this.type = type;
        this.model = model;
        this.unit = unit;
        this.description = description;
        this.status = SensorStatus.PENDING_VALIDATION;
        this.createdAt = Instant.now();
    }
}