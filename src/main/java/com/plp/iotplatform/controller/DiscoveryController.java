package com.plp.iotplatform.controller;

import com.plp.iotplatform.dto.RegistrationRequestDto;
import com.plp.iotplatform.service.DiscoveryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/discovery")
@RequiredArgsConstructor
@Tag(name = "Device Discovery", description = "Endpoints for device self-registration requests")
public class DiscoveryController {

    private final DiscoveryService discoveryService;

    @Operation(summary = "Submit a registration request for a new Hub or Sensor",
            description = "This endpoint is used by external agents or devices to request registration. " +
                    "Hubs provide MAC. Sensors provide Hub MAC and their local ID.")
    @ApiResponse(responseCode = "202", description = "Request accepted for processing")
    @ApiResponse(responseCode = "400", description = "Invalid request payload")
    @PostMapping("/registration-request")
    public ResponseEntity<Void> submitRegistrationRequest(@Valid @RequestBody RegistrationRequestDto request) {
        discoveryService.handleRegistrationRequest(request);
        return ResponseEntity.accepted().build();
    }
}