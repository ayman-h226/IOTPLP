package com.plp.iotplatform.config;

import com.plp.iotplatform.service.MqttMessageHandlerService;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy; // Important pour éviter les cycles au démarrage

@Configuration
@Slf4j
public class MqttConfig {

    @Value("${mqtt.broker.url}")
    private String brokerUrl;

    @Value("${mqtt.client.id}")
    private String clientId;

    private final MqttMessageHandlerService mqttMessageHandlerService;

    // Injection @Lazy pour aider à rompre les cycles de dépendance au démarrage
    public MqttConfig(@Lazy MqttMessageHandlerService mqttMessageHandlerService) {
        this.mqttMessageHandlerService = mqttMessageHandlerService;
    }

    @Bean
    public MqttClient mqttClient() {
        MqttClient client = null;
        try {
            client = new MqttClient(brokerUrl, clientId + "-" + System.currentTimeMillis(), new MemoryPersistence()); // ClientId unique
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);
            // TODO: Plus tard: options.setUserName(mqttUsername); options.setPassword(mqttPassword.toCharArray());

            client.setCallback(mqttMessageHandlerService); // Utiliser notre handler pour les messages et états de connexion

            log.info("Attempting to connect to MQTT broker at: {}", brokerUrl);
            client.connect(options);
            log.info("Successfully connected to MQTT broker at: {}", brokerUrl);

            // L'abonnement initial sera déclenché par MqttMessageHandlerService via connectComplete
            // ou par un appel explicite si nécessaire (voir MqttMessageHandlerService)

        } catch (MqttException e) {
            log.error("Failed to create or connect MQTT client to {}: {}", brokerUrl, e.getMessage(), e);
            // Selon la criticité, on pourrait vouloir que l'application ne démarre pas
            // throw new RuntimeException("Failed to initialize MQTT client", e);
        }
        return client; // Peut être null si la connexion échoue et qu'on ne relance pas d'exception
    }
}