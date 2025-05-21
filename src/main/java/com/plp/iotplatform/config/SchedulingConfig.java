package com.plp.iotplatform.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling // Cette annotation est suffisante si vous utilisez @Scheduled sur vos méthodes.
// Déplacée vers IotplatformApplication.java pour une portée globale.
public class SchedulingConfig {
    // Vous pouvez définir des beans de configuration pour le scheduler ici si nécessaire,
    // mais @EnableScheduling sur la classe principale suffit souvent.
}