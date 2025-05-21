package com.plp.iotplatform.mapper;

import com.plp.iotplatform.dto.HubCreationRequestDto;
import com.plp.iotplatform.dto.HubDto;
import com.plp.iotplatform.dto.HubUpdateRequestDto;
import com.plp.iotplatform.entity.Hub;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring", uses = {SensorMapper.class}) // uses SensorMapper pour la liste de SensorSummaryDto
public interface HubMapper {

    HubMapper INSTANCE = Mappers.getMapper(HubMapper.class);

    HubDto hubToHubDto(Hub hub);
    List<HubDto> hubsToHubDtos(List<Hub> hubs);
    Hub hubCreationRequestDtoToHub(HubCreationRequestDto dto);

    // Pour la mise à jour, ignore les champs nuls du DTO
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateHubFromDto(HubUpdateRequestDto dto, @MappingTarget Hub entity);
}