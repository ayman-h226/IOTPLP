package com.plp.iotplatform.service;

import com.plp.iotplatform.dto.HubCreationRequestDto;
import com.plp.iotplatform.dto.HubDto;
import com.plp.iotplatform.dto.HubUpdateRequestDto;
import com.plp.iotplatform.enums.HubStatus;
import com.plp.iotplatform.exception.DuplicateResourceException;
import com.plp.iotplatform.exception.ResourceNotFoundException;
import com.plp.iotplatform.mapper.HubMapper;
import com.plp.iotplatform.entity.Hub;
import com.plp.iotplatform.repository.HubRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional // La plupart des méthodes modifient l'état
public class HubServiceImpl implements HubService {

    private final HubRepository hubRepository;
    private final HubMapper hubMapper;

    @Override
    public HubDto createHub(HubCreationRequestDto request) {
        hubRepository.findByMacAddress(request.getMacAddress()).ifPresent(h -> {
            throw new DuplicateResourceException("Hub with MAC address " + request.getMacAddress() + " already exists.");
        });
        Hub hub = hubMapper.hubCreationRequestDtoToHub(request);
        hub.setStatus(HubStatus.ACTIVE); // Création manuelle par admin = directement actif
        // TODO: Générer des crédentials MQTT pour ce hub et les stocker/afficher
        Hub savedHub = hubRepository.save(hub);
        log.info("Hub created manually: {} (ID: {})", savedHub.getName(), savedHub.getId());
        return hubMapper.hubToHubDto(savedHub);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HubDto> getAllHubs() {
        return hubRepository.findAll().stream()
                .map(hubMapper::hubToHubDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public HubDto getHubDtoById(String hubId) {
        return hubRepository.findById(hubId)
                .map(hubMapper::hubToHubDto)
                .orElseThrow(() -> new ResourceNotFoundException("Hub not found with ID: " + hubId));
    }

    @Override
    public HubDto updateHub(String hubId, HubUpdateRequestDto request) {
        Hub hub = hubRepository.findById(hubId)
                .orElseThrow(() -> new ResourceNotFoundException("Hub not found with ID: " + hubId));

        // Vérifier si la nouvelle MAC (si fournie et différente) n'est pas déjà utilisée par un autre hub
        if (request.getMacAddress() != null && !request.getMacAddress().equalsIgnoreCase(hub.getMacAddress())) {
            hubRepository.findByMacAddress(request.getMacAddress()).ifPresent(existingHub -> {
                if (!existingHub.getId().equals(hubId)) {
                    throw new DuplicateResourceException("MAC address " + request.getMacAddress() + " is already used by another hub.");
                }
            });
        }
        hubMapper.updateHubFromDto(request, hub); // MapStruct gère les champs à mettre à jour
        Hub updatedHub = hubRepository.save(hub);
        log.info("Hub updated: {} (ID: {})", updatedHub.getName(), updatedHub.getId());
        return hubMapper.hubToHubDto(updatedHub);
    }

    @Override
    public void deleteHub(String hubId) {
        if (!hubRepository.existsById(hubId)) {
            throw new ResourceNotFoundException("Hub not found with ID: " + hubId);
        }
        // TODO: Logique de révocation des crédentials MQTT avant suppression
        hubRepository.deleteById(hubId);
        log.info("Hub deleted with ID: {}", hubId);
    }

    @Override
    public HubDto validateHub(String hubId) {
        Hub hub = hubRepository.findById(hubId)
                .orElseThrow(() -> new ResourceNotFoundException("Hub not found with ID: " + hubId));
        if (hub.getStatus() != HubStatus.PENDING_VALIDATION) {
            throw new IllegalStateException("Hub " + hubId + " is not in PENDING_VALIDATION state. Current status: " + hub.getStatus());
        }
        hub.setStatus(HubStatus.ACTIVE);
        // TODO: Générer des crédentials MQTT si ce n'est pas fait lors de la demande d'enregistrement
        Hub validatedHub = hubRepository.save(hub);
        log.info("Hub validated: {} (ID: {})", validatedHub.getName(), validatedHub.getId());
        return hubMapper.hubToHubDto(validatedHub);
    }

    @Override
    public void rejectHub(String hubId) {
        Hub hub = hubRepository.findById(hubId)
                .orElseThrow(() -> new ResourceNotFoundException("Hub not found with ID: " + hubId));
        // Option: Marquer comme REJECTED ou simplement supprimer
        // Pour une demande de découverte, supprimer est souvent plus simple.
        if (hub.getStatus() == HubStatus.PENDING_VALIDATION) {
            hubRepository.delete(hub);
            log.info("Pending Hub registration rejected and deleted: {} (ID: {})", hub.getName(), hubId);
        } else {
            log.warn("Attempted to reject hub {} which is not in PENDING_VALIDATION state. Current status: {}", hubId, hub.getStatus());
            // On pourrait aussi le passer en INACTIVE
            throw new IllegalStateException("Hub " + hubId + " cannot be rejected as it's not in PENDING_VALIDATION state.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<HubDto> getHubsByStatus(HubStatus status) {
        return hubRepository.findByStatus(status) // Assurez-vous que votre HubRepository a findByStatus(HubStatus status)
                .stream()
                .map(hubMapper::hubToHubDto)
                .collect(Collectors.toList());
    }
}