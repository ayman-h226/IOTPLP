package com.plp.iotplatform.service;

import com.plp.iotplatform.dto.SensorCreationRequestDto;
import com.plp.iotplatform.dto.SensorDto;
import com.plp.iotplatform.dto.SensorUpdateRequestDto;
import com.plp.iotplatform.enums.SensorStatus;
import com.plp.iotplatform.exception.DuplicateResourceException;
import com.plp.iotplatform.exception.ResourceNotFoundException;
import com.plp.iotplatform.entity.Hub;
import com.plp.iotplatform.entity.Sensor;
import com.plp.iotplatform.mapper.SensorMapper;
import com.plp.iotplatform.repository.HubRepository;
import com.plp.iotplatform.repository.SensorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SensorServiceImpl implements SensorService {

    private final SensorRepository sensorRepository;
    private final HubRepository hubRepository;
    private final SensorMapper sensorMapper;

    @Override
    public SensorDto addSensorToHub(String hubId, SensorCreationRequestDto request) {
        Hub hub = hubRepository.findById(hubId)
                .orElseThrow(() -> new ResourceNotFoundException("Hub not found with ID: " + hubId));

        sensorRepository.findByHubIdAndLocalId(hubId, request.getLocalId()).ifPresent(s -> {
            throw new DuplicateResourceException("Sensor with localId '" + request.getLocalId() + "' already exists for hub ID: " + hubId);
        });

        Sensor sensor = sensorMapper.sensorCreationRequestDtoToSensor(request);
        sensor.setHub(hub);
        sensor.setStatus(SensorStatus.ACTIVE); // Création manuelle par admin = directement actif
        Sensor savedSensor = sensorRepository.save(sensor);
        log.info("Sensor '{}' added manually to hub '{}' (Sensor ID: {})", savedSensor.getName(), hub.getName(), savedSensor.getId());
        return sensorMapper.sensorToSensorDto(savedSensor);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SensorDto> getSensorsByHub(String hubId) {
        if (!hubRepository.existsById(hubId)) {
            throw new ResourceNotFoundException("Hub not found with ID: " + hubId);
        }
        return sensorRepository.findByHubId(hubId).stream()
                .map(sensorMapper::sensorToSensorDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SensorDto getSensorDtoById(String sensorId) {
        return sensorRepository.findById(sensorId)
                .map(sensorMapper::sensorToSensorDto)
                .orElseThrow(() -> new ResourceNotFoundException("Sensor not found with ID: " + sensorId));
    }

    @Override
    public SensorDto updateSensor(String sensorId, SensorUpdateRequestDto request) {
        Sensor sensor = sensorRepository.findById(sensorId)
                .orElseThrow(() -> new ResourceNotFoundException("Sensor not found with ID: " + sensorId));

        // Vérifier si le nouveau localId (si fourni et différent) n'est pas déjà pris par un autre capteur sur le même hub
        if (request.getLocalId() != null && !request.getLocalId().equals(sensor.getLocalId())) {
            sensorRepository.findByHubIdAndLocalId(sensor.getHub().getId(), request.getLocalId()).ifPresent(existingSensor -> {
                if (!existingSensor.getId().equals(sensorId)) {
                    throw new DuplicateResourceException("Sensor with localId '" + request.getLocalId() + "' already exists for this hub.");
                }
            });
        }

        sensorMapper.updateSensorFromDto(request, sensor);
        Sensor updatedSensor = sensorRepository.save(sensor);
        log.info("Sensor updated: {} (ID: {})", updatedSensor.getName(), updatedSensor.getId());
        return sensorMapper.sensorToSensorDto(updatedSensor);
    }

    @Override
    public void deleteSensor(String sensorId) {
        if (!sensorRepository.existsById(sensorId)) {
            throw new ResourceNotFoundException("Sensor not found with ID: " + sensorId);
        }
        sensorRepository.deleteById(sensorId);
        log.info("Sensor deleted with ID: {}", sensorId);
    }

    @Override
    public void updateSensorLastDataReceivedByMacAndLocalId(String hubMacAddress, String sensorLocalId, Instant receptionTime) {
        hubRepository.findByMacAddress(hubMacAddress).ifPresentOrElse(
                hub -> sensorRepository.findByHubIdAndLocalId(hub.getId(), sensorLocalId).ifPresentOrElse(
                        sensor -> {
                            if (sensor.getStatus() == SensorStatus.OFFLINE) { // Si était offline, le repasser en ACTIVE
                                sensor.setStatus(SensorStatus.ACTIVE);
                                log.info("Sensor {} (Hub {}) status changed from OFFLINE to ACTIVE due to new data.", sensor.getLocalId(), hub.getName());
                            }
                            sensor.setLastDataReceivedAt(receptionTime);
                            sensorRepository.save(sensor);
                            log.debug("Updated lastDataReceivedAt for sensor localId: {} on hub MAC: {}", sensorLocalId, hubMacAddress);
                        },
                        () -> log.warn("Received data for unknown sensor localId: {} on hub MAC: {}", sensorLocalId, hubMacAddress)
                ),
                () -> log.warn("Received data for sensor on unknown hub MAC: {}", hubMacAddress)
        );
    }

    @Override
    public SensorDto validateSensor(String sensorId) {
        Sensor sensor = sensorRepository.findById(sensorId)
                .orElseThrow(() -> new ResourceNotFoundException("Sensor not found with ID: " + sensorId));
        if (sensor.getStatus() != SensorStatus.PENDING_VALIDATION) {
            throw new IllegalStateException("Sensor " + sensorId + " is not in PENDING_VALIDATION state. Current status: " + sensor.getStatus());
        }
        sensor.setStatus(SensorStatus.ACTIVE);
        Sensor validatedSensor = sensorRepository.save(sensor);
        log.info("Sensor validated: {} (ID: {}) on Hub {}", validatedSensor.getName(), validatedSensor.getId(), validatedSensor.getHub().getName());
        return sensorMapper.sensorToSensorDto(validatedSensor);
    }

    @Override
    public void rejectSensor(String sensorId) {
        Sensor sensor = sensorRepository.findById(sensorId)
                .orElseThrow(() -> new ResourceNotFoundException("Sensor not found with ID: " + sensorId));
        if (sensor.getStatus() == SensorStatus.PENDING_VALIDATION) {
            sensorRepository.delete(sensor);
            log.info("Pending Sensor registration rejected and deleted: {} (ID: {}) on Hub {}",sensor.getName(), sensorId, sensor.getHub().getName());
        } else {
            log.warn("Attempted to reject sensor {} which is not in PENDING_VALIDATION state. Current status: {}", sensorId, sensor.getStatus());
            throw new IllegalStateException("Sensor " + sensorId + " cannot be rejected as it's not in PENDING_VALIDATION state.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Sensor> getSensorById(String sensorId) {
        return sensorRepository.findById(sensorId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SensorDto> getSensorsByStatus(SensorStatus status) {
        return sensorRepository.findByStatus(status) // Assurez-vous que SensorRepository a findByStatus(SensorStatus status)
                .stream()
                .map(sensorMapper::sensorToSensorDto)
                .collect(Collectors.toList());
    }
}