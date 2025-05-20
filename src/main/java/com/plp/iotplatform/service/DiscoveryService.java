package com.plp.iotplatform.service;

import com.plp.iotplatform.dto.RegistrationRequestDto;

public interface DiscoveryService {
    void handleRegistrationRequest(RegistrationRequestDto request);
}