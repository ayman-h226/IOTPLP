package com.plp.iotplatform.entity;

import com.plp.iotplatform.enums.HubStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "hubs")
@Data
@NoArgsConstructor
public class Hub {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true, nullable = false)
    private String macAddress;

    @Column(nullable = false)
    private String name;

    private String location;
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HubStatus status = HubStatus.PENDING_VALIDATION; // Statut initial

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    private Instant lastSeen;

    @ToString.Exclude // Pour éviter les problèmes de toString() en boucle avec la relation bidirectionnelle
    @EqualsAndHashCode.Exclude // Idem
    @OneToMany(mappedBy = "hub", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Sensor> sensors = new ArrayList<>();

    public Hub(String macAddress, String name, String location, String description) {
        this.macAddress = macAddress;
        this.name = name;
        this.location = location;
        this.description = description;
        this.status = HubStatus.PENDING_VALIDATION;
        this.createdAt = Instant.now();
    }
}