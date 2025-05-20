package com.plp.iotplatform.service;

import com.plp.iotplatform.DTO.HubCreationRequestDto;
import com.plp.iotplatform.model.entity.Hub;

import java.util.List;
import java.util.Optional;
import com.plp.iotplatform.DTO.HubCreationRequestDto;
import com.plp.iotplatform.exception.ResourceNotFoundException; // Exception custom
import com.plp.iotplatform.exception.DuplicateResourceException; // Exception custom
import com.plp.iotplatform.model.entity.Hub;
import com.plp.iotplatform.repository.HubRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Pour les opérations d'écriture


@Service
@RequiredArgsConstructor
public class HubServiceImpl implements HubService {
    private final HubRepository hubRepository;
    // Plus tard: MqttAdminService pour générer des crédentials/ACLs

    @Override
    @Transactional
    public Hub createHub(HubCreationRequestDto request) {
        if (hubRepository.findByMacAddress(request.getMacAddress()).isPresent()) {
            throw new DuplicateResourceException("Hub with MAC address " + request.getMacAddress() + " already exists.");
        }
        Hub hub = new Hub();
        hub.setMacAddress(request.getMacAddress());
        hub.setName(request.getName());
        hub.setLocation(request.getLocation());
        hub.setDescription(request.getDescription());
        // Statut par défaut "PENDING_CONFIG" déjà mis dans l'entité
        // Plus tard: Générer les crédentials MQTT ici
        return hubRepository.save(hub);
    }

    @Override
    public List<Hub> getAllHubs() {
        return hubRepository.findAll();
    }

    @Override
    public Optional<Hub> getHubById(String hubId) {
        return hubRepository.findById(hubId);
    }

    @Override
    public Optional<Hub> getHubByMacAddress(String macAddress) {
        return hubRepository.findByMacAddress(macAddress);
    }

    @Override
    @Transactional
    public Hub updateHub(String hubId, HubCreationRequestDto request) {
        Hub hub = hubRepository.findById(hubId)
                .orElseThrow(() -> new ResourceNotFoundException("Hub not found with id: " + hubId));
        // Vérifier si le nouveau MAC n'est pas déjà pris par un AUTRE hub
        hubRepository.findByMacAddress(request.getMacAddress()).ifPresent(existingHub -> {
            if (!existingHub.getId().equals(hubId)) {
                throw new DuplicateResourceException("MAC address " + request.getMacAddress() + " is already used by another hub.");
            }
        });
        hub.setMacAddress(request.getMacAddress());
        hub.setName(request.getName());
        hub.setLocation(request.getLocation());
        hub.setDescription(request.getDescription());
        return hubRepository.save(hub);
    }

    @Override
    @Transactional
    public void deleteHub(String hubId) {
        if (!hubRepository.existsById(hubId)) {
            throw new ResourceNotFoundException("Hub not found with id: " + hubId);
        }
        // Plus tard: Révoquer les crédentials MQTT du hub
        hubRepository.deleteById(hubId);
    }
}
