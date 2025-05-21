package com.plp.iotplatform.controller;

import com.plp.iotplatform.dto.HubCreationRequestDto;
import com.plp.iotplatform.dto.HubDto;
import com.plp.iotplatform.dto.HubUpdateRequestDto;
import com.plp.iotplatform.dto.IdResponseDto;
import com.plp.iotplatform.enums.HubStatus;
import com.plp.iotplatform.service.HubService;
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
@RequestMapping("/api/admin/hubs")
@RequiredArgsConstructor
@Tag(name = "Admin - Hub Management", description = "Endpoints for administrators to manage IoT Hubs")
// TODO: Add @PreAuthorize("hasRole('ADMIN')") ou une sécurité équivalente
public class AdminHubController {

    private final HubService hubService;

    @Operation(summary = "Create a new Hub (Manual Admin Action)")
    @ApiResponse(responseCode = "201", description = "Hub created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request payload")
    @ApiResponse(responseCode = "409", description = "Hub with this MAC address already exists")
    @PostMapping
    public ResponseEntity<HubDto> createHub(@Valid @RequestBody HubCreationRequestDto request) {
        HubDto createdHub = hubService.createHub(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdHub);
    }

    @Operation(summary = "List all Hubs")
    @GetMapping
    public ResponseEntity<List<HubDto>> getAllHubs() {
        List<HubDto> hubs = hubService.getAllHubs();
        return ResponseEntity.ok(hubs);
    }

    @Operation(summary = "Get Hub details by ID")
    @ApiResponse(responseCode = "200", description = "Hub found")
    @ApiResponse(responseCode = "404", description = "Hub not found")
    @GetMapping("/{hubId}")
    public ResponseEntity<HubDto> getHubById(@Parameter(description = "ID of the Hub to retrieve") @PathVariable String hubId) {
        HubDto hub = hubService.getHubDtoById(hubId); // Changer pour retourner DTO
        return ResponseEntity.ok(hub);
    }

    @Operation(summary = "Update an existing Hub")
    @ApiResponse(responseCode = "200", description = "Hub updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request payload")
    @ApiResponse(responseCode = "404", description = "Hub not found")
    @PutMapping("/{hubId}")
    public ResponseEntity<HubDto> updateHub(
            @Parameter(description = "ID of the Hub to update") @PathVariable String hubId,
            @Valid @RequestBody HubUpdateRequestDto request) {
        HubDto updatedHub = hubService.updateHub(hubId, request);
        return ResponseEntity.ok(updatedHub);
    }

    @Operation(summary = "Delete a Hub")
    @ApiResponse(responseCode = "204", description = "Hub deleted successfully")
    @ApiResponse(responseCode = "404", description = "Hub not found")
    @DeleteMapping("/{hubId}")
    public ResponseEntity<Void> deleteHub(@Parameter(description = "ID of the Hub to delete") @PathVariable String hubId) {
        hubService.deleteHub(hubId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Validate a Hub (change status from PENDING_VALIDATION to ACTIVE)")
    @ApiResponse(responseCode = "200", description = "Hub validated successfully")
    @ApiResponse(responseCode = "404", description = "Hub not found")
    @ApiResponse(responseCode = "409", description = "Hub not in PENDING_VALIDATION state")
    @PostMapping("/{hubId}/validate")
    public ResponseEntity<HubDto> validateHub(@Parameter(description = "ID of the Hub to validate") @PathVariable String hubId) {
        HubDto validatedHub = hubService.validateHub(hubId);
        return ResponseEntity.ok(validatedHub);
    }

    @Operation(summary = "Reject a Hub (change status from PENDING_VALIDATION or delete)")
    @ApiResponse(responseCode = "200", description = "Hub rejected successfully")
    @ApiResponse(responseCode = "404", description = "Hub not found")
    @PostMapping("/{hubId}/reject")
    public ResponseEntity<Void> rejectHub(@Parameter(description = "ID of the Hub to reject") @PathVariable String hubId) {
        hubService.rejectHub(hubId); // Le service décide de supprimer ou de changer le statut
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get all Hubs with status PENDING_VALIDATION")
    @ApiResponse(responseCode = "200", description = "List of pending Hubs")
    @GetMapping("/pending")
    public ResponseEntity<List<HubDto>> getPendingHubs() {
        List<HubDto> pendingHubs = hubService.getHubsByStatus(HubStatus.PENDING_VALIDATION);
        return ResponseEntity.ok(pendingHubs);
    }
}