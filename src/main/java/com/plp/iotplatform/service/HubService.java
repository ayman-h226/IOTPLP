package com.plp.iotplatform.service;

import com.plp.iotplatform.dto.HubCreationRequestDto;
import com.plp.iotplatform.dto.HubDto;
import com.plp.iotplatform.dto.HubUpdateRequestDto;
import com.plp.iotplatform.enums.HubStatus;

import java.util.List;

public interface HubService {
    HubDto createHub(HubCreationRequestDto request);
    List<HubDto> getAllHubs();
    HubDto getHubDtoById(String hubId); // Renvoie DTO
    HubDto updateHub(String hubId, HubUpdateRequestDto request);
    void deleteHub(String hubId);
    HubDto validateHub(String hubId);
    void rejectHub(String hubId); // Décide de supprimer ou marquer comme rejeté
    List<HubDto> getHubsByStatus(HubStatus status);
}