package com.plp.iotplatform.controller;

import com.plp.iotplatform.DTO.HubCreationRequestDto;
import com.plp.iotplatform.model.entity.Hub;
import com.plp.iotplatform.service.HubService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
        import java.util.List;

@RestController
@RequestMapping("/api/admin/hubs") // Préfixe pour les routes d'admin des hubs
@RequiredArgsConstructor
// TODO: @Tag(name = "Admin - Hub Management")
public class AdminHubController {

    private final HubService hubService;

    @PostMapping
    public ResponseEntity<Hub> createHub(@Valid @RequestBody HubCreationRequestDto request) {
        Hub createdHub = hubService.createHub(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdHub);
    }

    @GetMapping
    public ResponseEntity<List<Hub>> getAllHubs() {
        return ResponseEntity.ok(hubService.getAllHubs());
    }

    @GetMapping("/{hubId}")
    public ResponseEntity<Hub> getHubById(@PathVariable String hubId) {
        return hubService.getHubById(hubId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{hubId}")
    public ResponseEntity<Hub> updateHub(@PathVariable String hubId, @Valid @RequestBody HubCreationRequestDto request) {
        Hub updatedHub = hubService.updateHub(hubId, request);
        return ResponseEntity.ok(updatedHub);
    }

    @DeleteMapping("/{hubId}")
    public ResponseEntity<Void> deleteHub(@PathVariable String hubId) {
        hubService.deleteHub(hubId);
        return ResponseEntity.noContent().build();
    }
}