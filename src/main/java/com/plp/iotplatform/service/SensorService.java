package com.plp.iotplatform.service;

import com.plp.iotplatform.dto.SensorCreationRequestDto;
import com.plp.iotplatform.dto.SensorDto;
import com.plp.iotplatform.dto.SensorUpdateRequestDto;
import com.plp.iotplatform.entity.Sensor;
import com.plp.iotplatform.enums.SensorStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface SensorService {
    SensorDto addSensorToHub(String hubId, SensorCreationRequestDto request);
    List<SensorDto> getSensorsByHub(String hubId);
    SensorDto getSensorDtoById(String sensorId); // Renvoie DTO
    SensorDto updateSensor(String sensorId, SensorUpdateRequestDto request);
    void deleteSensor(String sensorId);
    void updateSensorLastDataReceivedByMacAndLocalId(String hubMacAddress, String sensorLocalId, Instant receptionTime);
    SensorDto validateSensor(String sensorId);
    void rejectSensor(String sensorId);
    Optional<Sensor> getSensorById(String globalSensorId);
    List<SensorDto> getSensorsByStatus(SensorStatus status);
}