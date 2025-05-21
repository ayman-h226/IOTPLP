package com.plp.iotplatform.controller;

import com.plp.iotplatform.dto.SensorCreationRequestDto;
import com.plp.iotplatform.dto.SensorDto;
import com.plp.iotplatform.dto.SensorUpdateRequestDto;
import com.plp.iotplatform.enums.SensorStatus;
import com.plp.iotplatform.service.SensorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin") // Base commune pour l'admin
@RequiredArgsConstructor
@Tag(name = "Admin - Sensor Management", description = "Endpoints for administrators to manage sensors")
// TODO: Add @PreAuthorize("hasRole('ADMIN')")
public class AdminSensorController {

    private final SensorService sensorService;

    @Operation(summary = "Add a new Sensor to a Hub (Manual Admin Action)")
    @ApiResponse(responseCode = "201", description = "Sensor created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request payload")
    @ApiResponse(responseCode = "404", description = "Hub not found")
    @ApiResponse(responseCode = "409", description = "Sensor with this localId already exists for this hub")
    @PostMapping("/hubs/{hubId}/sensors")
    public ResponseEntity<SensorDto> addSensorToHub(
            @Parameter(description = "ID of the Hub to add the sensor to") @PathVariable String hubId,
            @Valid @RequestBody SensorCreationRequestDto request) {
        SensorDto createdSensor = sensorService.addSensorToHub(hubId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSensor);
    }

    @Operation(summary = "List all Sensors for a specific Hub")
    @ApiResponse(responseCode = "200", description = "Sensors listed successfully")
    @ApiResponse(responseCode = "404", description = "Hub not found")
    @GetMapping("/hubs/{hubId}/sensors")
    public ResponseEntity<List<SensorDto>> getSensorsByHub(
            @Parameter(description = "ID of the Hub") @PathVariable String hubId) {
        List<SensorDto> sensors = sensorService.getSensorsByHub(hubId);
        return ResponseEntity.ok(sensors);
    }

    @Operation(summary = "Get Sensor details by its global ID")
    @ApiResponse(responseCode = "200", description = "Sensor found")
    @ApiResponse(responseCode = "404", description = "Sensor not found")
    @GetMapping("/sensors/{sensorId}")
    public ResponseEntity<SensorDto> getSensorById(
            @Parameter(description = "Global ID of the Sensor") @PathVariable String sensorId) {
        SensorDto sensor = sensorService.getSensorDtoById(sensorId); // Changer pour retourner DTO
        return ResponseEntity.ok(sensor);
    }

    @Operation(summary = "Update an existing Sensor")
    @ApiResponse(responseCode = "200", description = "Sensor updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request payload")
    @ApiResponse(responseCode = "404", description = "Sensor not found")
    @PutMapping("/sensors/{sensorId}")
    public ResponseEntity<SensorDto> updateSensor(
            @Parameter(description = "Global ID of the Sensor to update") @PathVariable String sensorId,
            @Valid @RequestBody SensorUpdateRequestDto request) {
        SensorDto updatedSensor = sensorService.updateSensor(sensorId, request);
        return ResponseEntity.ok(updatedSensor);
    }

    @Operation(summary = "Delete a Sensor by its global ID")
    @ApiResponse(responseCode = "204", description = "Sensor deleted successfully")
    @ApiResponse(responseCode = "404", description = "Sensor not found")
    @DeleteMapping("/sensors/{sensorId}")
    public ResponseEntity<Void> deleteSensor(
            @Parameter(description = "Global ID of the Sensor to delete") @PathVariable String sensorId) {
        sensorService.deleteSensor(sensorId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Validate a Sensor (change status from PENDING_VALIDATION to ACTIVE)")
    @ApiResponse(responseCode = "200", description = "Sensor validated successfully")
    @ApiResponse(responseCode = "404", description = "Sensor not found")
    @ApiResponse(responseCode = "409", description = "Sensor not in PENDING_VALIDATION state")
    @PostMapping("/sensors/{sensorId}/validate")
    public ResponseEntity<SensorDto> validateSensor(@Parameter(description = "Global ID of the Sensor to validate") @PathVariable String sensorId) {
        SensorDto validatedSensor = sensorService.validateSensor(sensorId);
        return ResponseEntity.ok(validatedSensor);
    }

    @Operation(summary = "Reject a Sensor (change status from PENDING_VALIDATION or delete)")
    @ApiResponse(responseCode = "200", description = "Sensor rejected successfully")
    @ApiResponse(responseCode = "404", description = "Sensor not found")
    @PostMapping("/sensors/{sensorId}/reject")
    public ResponseEntity<Void> rejectSensor(@Parameter(description = "Global ID of the Sensor to reject") @PathVariable String sensorId) {
        sensorService.rejectSensor(sensorId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "List all Sensors pending validation (across all hubs)")
    @GetMapping("/sensors/pending") // Note: le mapping est sur /api/admin
    public ResponseEntity<List<SensorDto>> getPendingSensors() {
        List<SensorDto> pendingSensors = sensorService.getSensorsByStatus(SensorStatus.PENDING_VALIDATION);
        return ResponseEntity.ok(pendingSensors);
    }
}