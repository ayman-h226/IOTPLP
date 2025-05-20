package com.plp.iotplatform.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "hubs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Hub {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // Ou GenerationType.IDENTITY si BDD gère l'auto-incrément
    private String id; // ID interne UUID

    @Column(unique = true, nullable = false)
    private String macAddress;

    @Column(nullable = false)
    private String name;

    private String location;
    private String description;

    // Ex: PENDING_CONFIG, ACTIVE, INACTIVE, OFFLINE
    @Column(nullable = false)
    private String status = "PENDING_CONFIG";

    private Instant createdAt = Instant.now();
    private Instant lastSeen;

    // Plus tard: String agentVersion;
    // Plus tard: champs pour crédentials MQTT (ou référence à un secret store)

    @OneToMany(mappedBy = "hub", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Sensor> sensors;
}