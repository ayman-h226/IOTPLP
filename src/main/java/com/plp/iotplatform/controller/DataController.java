package com.plp.iotplatform.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/data")
@RequiredArgsConstructor
@Tag(name = "Sensor Data (Demo)", description = "Endpoints for retrieving sensor data - FOR DEMO/TESTING ONLY")
public class DataController {

    // Pour la démo, on peut injecter SensorService pour lire lastDataReceivedAt et simuler une valeur
    private final com.plp.iotplatform.service.SensorService sensorServiceLocal;

    @Operation(summary = "Get latest (simulated) sensor reading - FOR DEMO")
    @GetMapping("/latest/{globalSensorId}")
    // TODO: Add Security (check if user has access to this sensorId)
    public ResponseEntity<?> getLatestSensorData(@PathVariable String globalSensorId) {
        // Pour cette démo, on va juste retourner les infos du capteur et son lastDataReceivedAt
        // Puisque le backend ne stocke plus les valeurs.
        return sensorServiceLocal.getSensorById(globalSensorId)
                .map(sensor -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("sensorId", sensor.getId());
                    response.put("localId", sensor.getLocalId());
                    response.put("hubId", sensor.getHub().getId());
                    response.put("name", sensor.getName());
                    response.put("type", sensor.getType());
                    response.put("lastDataReceivedAt", sensor.getLastDataReceivedAt());
                    response.put("status", sensor.getStatus());
                    response.put("simulatedValue", Math.round(Math.random() * 500) / 10.0); // Valeur simulée
                    response.put("message", "NOTE: Actual sensor value is consumed by Data Team via MQTT. This is a simulated value for demo.");
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}