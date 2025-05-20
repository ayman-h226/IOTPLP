package com.plp.iotplatform.controller;

import com.plp.iotplatform.DTO.SensorCreationRequestDto;
import com.plp.iotplatform.model.entity.Sensor;
import com.plp.iotplatform.service.SensorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
        import java.util.List;

@RestController
@RequestMapping("/api/admin/hubs/{hubId}/sensors") // Les capteurs sont gérés sous un hub
@RequiredArgsConstructor
// TODO: @Tag(name = "Admin - Sensor Management")
public class AdminSensorController {

    private final SensorService sensorService;

    @PostMapping
    public ResponseEntity<Sensor> addSensorToHub(
            @PathVariable String hubId,
            @Valid @RequestBody SensorCreationRequestDto request) {
        Sensor createdSensor = sensorService.addSensorToHub(hubId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSensor);
    }

    @GetMapping
    public ResponseEntity<List<Sensor>> getSensorsByHub(@PathVariable String hubId) {
        List<Sensor> sensors = sensorService.getSensorsByHub(hubId);
        return ResponseEntity.ok(sensors);
    }

    // Routes pour GET /sensorId, PUT /sensorId, DELETE /sensorId si on veut manipuler un capteur
    // directement par son ID global, hors contexte du hub.
    // Pour l'instant, on garde la gestion via le hub.
}