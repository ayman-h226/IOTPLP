package com.plp.iotplatform.service;

import com.plp.iotplatform.DTO.HubCreationRequestDto;
import com.plp.iotplatform.DTO.HubDto; // Ou directement l'entité Hub pour le retour
import com.plp.iotplatform.model.entity.Hub;
import java.util.List;
import java.util.Optional;

public interface HubService {
    Hub createHub(HubCreationRequestDto request);
    List<Hub> getAllHubs();
    Optional<Hub> getHubById(String hubId);
    Optional<Hub> getHubByMacAddress(String macAddress);
    Hub updateHub(String hubId, HubCreationRequestDto request); // Pour la mise à jour
    void deleteHub(String hubId);
    // Plus tard: méthodes pour statut, crédentials, etc.
}
