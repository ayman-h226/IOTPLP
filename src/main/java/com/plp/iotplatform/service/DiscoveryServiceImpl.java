package com.plp.iotplatform.service;

import com.plp.iotplatform.dto.RegistrationRequestDto;
import com.plp.iotplatform.enums.DeviceType;
import com.plp.iotplatform.enums.HubStatus;
import com.plp.iotplatform.enums.SensorStatus;
import com.plp.iotplatform.exception.ResourceNotFoundException;
import com.plp.iotplatform.entity.Hub;
import com.plp.iotplatform.entity.Sensor;
import com.plp.iotplatform.repository.HubRepository;
import com.plp.iotplatform.repository.SensorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiscoveryServiceImpl implements DiscoveryService {

    private final HubRepository hubRepository;
    private final SensorRepository sensorRepository;
    // Pas besoin de mappers ici si on crée directement les entités

    @Override
    @Transactional
    public void handleRegistrationRequest(RegistrationRequestDto request) {
        if (DeviceType.HUB.equals(request.getType())) {
            // Si un hub avec cette MAC existe déjà, on ne fait rien ou on logue une tentative de ré-enregistrement
            hubRepository.findByMacAddress(request.getHubMacAddress()).ifPresentOrElse(
                    existingHub -> log.info("Hub with MAC {} already exists or pending validation. ID: {}", request.getHubMacAddress(), existingHub.getId()),
                    () -> {
                        Hub newHub = new Hub();
                        newHub.setMacAddress(request.getHubMacAddress());
                        newHub.setName(request.getHubName() != null ? request.getHubName() : "Hub awaiting validation - " + request.getHubMacAddress());
                        newHub.setStatus(HubStatus.valueOf(HubStatus.PENDING_VALIDATION.toString())); // Utiliser Enum.toString() si le champ est String
                        // newHub.setStatus(HubStatus.PENDING_VALIDATION); // Si le champ est Enum
                        hubRepository.save(newHub);
                        log.info("New hub registration request received for MAC: {}. Status set to PENDING_VALIDATION.", request.getHubMacAddress());
                    }
            );
        } else if (DeviceType.SENSOR.equals(request.getType())) {
            Hub hub = hubRepository.findByMacAddress(request.getHubMacAddress())
                    .orElseThrow(() -> {
                        log.warn("Sensor registration request received for an unknown Hub MAC: {}", request.getHubMacAddress());
                        return new ResourceNotFoundException("Hub with MAC address " + request.getHubMacAddress() + " not found for sensor registration.");
                    });

            // Si un capteur avec ce localId pour ce hub existe déjà
            sensorRepository.findByHubIdAndLocalId(hub.getId(), request.getSensorLocalId()).ifPresentOrElse(
                    existingSensor -> log.info("Sensor with localId {} on hub {} already exists or pending. ID: {}", request.getSensorLocalId(), hub.getId(), existingSensor.getId()),
                    () -> {
                        Sensor newSensor = new Sensor();
                        newSensor.setHub(hub);
                        newSensor.setLocalId(request.getSensorLocalId());
                        newSensor.setName(request.getSensorLocalId()); // Nom par défaut
                        newSensor.setType(request.getSensorType() != null ? request.getSensorType() : "UNKNOWN");
                        newSensor.setModel(request.getSensorModel());
                        newSensor.setStatus(SensorStatus.valueOf(SensorStatus.PENDING_VALIDATION.toString()));
                        // newSensor.setStatus(SensorStatus.PENDING_VALIDATION); // Si champ Enum
                        sensorRepository.save(newSensor);
                        log.info("New sensor registration request received for localId: {} on hub MAC: {}. Status set to PENDING_VALIDATION.", request.getSensorLocalId(), request.getHubMacAddress());
                    }
            );
        } else {
            log.warn("Received registration request with unknown type: {}", request.getType());
            throw new IllegalArgumentException("Invalid registration request type: " + request.getType());
        }
    }
}