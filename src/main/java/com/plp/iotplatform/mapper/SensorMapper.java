package com.plp.iotplatform.mapper;

import com.plp.iotplatform.dto.SensorCreationRequestDto;
import com.plp.iotplatform.dto.SensorDto;
import com.plp.iotplatform.dto.SensorSummaryDto;
import com.plp.iotplatform.dto.SensorUpdateRequestDto;
import com.plp.iotplatform.entity.Sensor;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SensorMapper {

    SensorMapper INSTANCE = Mappers.getMapper(SensorMapper.class);

    @Mapping(source = "hub.id", target = "hubId")
    @Mapping(source = "hub.name", target = "hubName")
    SensorDto sensorToSensorDto(Sensor sensor);
    List<SensorDto> sensorsToSensorDtos(List<Sensor> sensors);

    Sensor sensorCreationRequestDtoToSensor(SensorCreationRequestDto dto);

    // Pour la vue résumée dans HubDto
    SensorSummaryDto sensorToSensorSummaryDto(Sensor sensor);
    List<SensorSummaryDto> sensorsToSensorSummaryDtos(List<Sensor> sensors);


    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateSensorFromDto(SensorUpdateRequestDto dto, @MappingTarget Sensor entity);
}