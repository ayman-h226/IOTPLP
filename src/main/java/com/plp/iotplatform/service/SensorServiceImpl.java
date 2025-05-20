package com.plp.iotplatform.service;

import com.plp.iotplatform.DTO.SensorCreationRequestDto;
import com.plp.iotplatform.exception.ResourceNotFoundException;
import com.plp.iotplatform.exception.DuplicateResourceException;
import com.plp.iotplatform.model.entity.Hub;
import com.plp.iotplatform.model.entity.Sensor;
import com.plp.iotplatform.repository.HubRepository;
import com.plp.iotplatform.repository.SensorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SensorServiceImpl implements SensorService {
    private final SensorRepository sensorRepository;
    private final HubRepository hubRepository;
    // Plus tard: MqttAdminService pour les ACLs des topics capteurs

    @Override
    @Transactional
    public Sensor addSensorToHub(String hubId, SensorCreationRequestDto request) {
        Hub hub = hubRepository.findById(hubId)
                .orElseThrow(() -> new ResourceNotFoundException("Hub not found with id: " + hubId));

        // Vérifier l'unicité de localId PAR HUB
        sensorRepository.findByHubIdAndLocalId(hubId, request.getLocalId()).ifPresent(s -> {
            throw new DuplicateResourceException("Sensor with localId " + request.getLocalId() + " already exists for this hub.");
        });

        Sensor sensor = new Sensor();
        sensor.setHub(hub);
        sensor.setLocalId(request.getLocalId());
        sensor.setName(request.getName());
        sensor.setType(request.getType());
        sensor.setModel(request.getModel());
        sensor.setUnit(request.getUnit());
        sensor.setDescription(request.getDescription());
        // Statut par défaut "PENDING_VALIDATION"
        // Plus tard: le backend assigne un ID global unique, ou on utilise celui généré par la BDD
        return sensorRepository.save(sensor);
    }

    @Override
    public List<Sensor> getSensorsByHub(String hubId) {
        if (!hubRepository.existsById(hubId)) {
            throw new ResourceNotFoundException("Hub not found with id: " + hubId);
        }
        return sensorRepository.findByHubId(hubId);
    }

    @Override
    public Optional<Sensor> getSensorById(String sensorId) {
        return sensorRepository.findById(sensorId);
    }

    @Override
    @Transactional
    public Sensor updateSensor(String sensorId, SensorCreationRequestDto request) {
        Sensor sensor = sensorRepository.findById(sensorId)
                .orElseThrow(() -> new ResourceNotFoundException("Sensor not found with id: " + sensorId));

        // Vérifier si le nouveau localId n'est pas déjà pris par un AUTRE capteur SUR LE MÊME HUB
        sensorRepository.findByHubIdAndLocalId(sensor.getHub().getId(), request.getLocalId()).ifPresent(existingSensor -> {
            if (!existingSensor.getId().equals(sensorId)) {
                throw new DuplicateResourceException("Sensor with localId " + request.getLocalId() + " already exists for this hub.");
            }
        });

        sensor.setLocalId(request.getLocalId());
        sensor.setName(request.getName());
        sensor.setType(request.getType());
        sensor.setModel(request.getModel());
        sensor.setUnit(request.getUnit());
        sensor.setDescription(request.getDescription());
        return sensorRepository.save(sensor);
    }

    @Override
    @Transactional
    public void deleteSensor(String sensorId) {
        if (!sensorRepository.existsById(sensorId)) {
            throw new ResourceNotFoundException("Sensor not found with id: " + sensorId);
        }
        // Plus tard: notifier le hub, révoquer les ACLs MQTT
        sensorRepository.deleteById(sensorId);
    }



    @Override
    @Transactional // Important pour les opérations d'écriture
    public void updateSensorLastDataReceived(String globalSensorId, Instant receptionTime) {
        Optional<Sensor> sensorOpt = sensorRepository.findById(globalSensorId); // Trouve par PK
        if (sensorOpt.isPresent()) {
            Sensor sensor = sensorOpt.get();
            sensor.setLastDataReceivedAt(receptionTime);
            sensorRepository.save(sensor);
            // log.debug("Updated lastDataReceivedAt for sensorId: {}", globalSensorId); // Optionnel
        } else {
            log.warn("Attempted to update lastDataReceivedAt for non-existent sensorId: {}", globalSensorId);
        }
    }

}