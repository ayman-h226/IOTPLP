package com.plp.iotplatform.service;

import com.plp.iotplatform.DTO.SensorCreationRequestDto;
import com.plp.iotplatform.model.entity.Sensor;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface SensorService {
    Sensor addSensorToHub(String hubId, SensorCreationRequestDto request);
    List<Sensor> getSensorsByHub(String hubId);
    Optional<Sensor> getSensorById(String sensorId);
    Sensor updateSensor(String sensorId, SensorCreationRequestDto request);
    void deleteSensor(String sensorId);
    void updateSensorLastDataReceived(String globalSensorId, Instant receptionTime);}

