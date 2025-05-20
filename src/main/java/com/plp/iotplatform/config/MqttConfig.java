package com.plp.iotplatform.config;

import com.plp.iotplatform.service.MqttMessageHandlerService; // Nouveau service handler
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class MqttConfig {

    @Value("${mqtt.broker.url}")
    private String brokerUrl;

    @Value("${mqtt.client.id}")
    private String clientId;

    // Injection du service qui va réellement traiter le message
    private final MqttMessageHandlerService messageHandlerService;

    @Bean
    public MqttClient mqttClient() throws MqttException {
        MqttClient client = new MqttClient(brokerUrl, clientId, new MemoryPersistence());
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true); // Ou false si vous voulez des messages persistants
        options.setConnectionTimeout(10);
        // Plus tard: options.setUserName(mqttUsername); options.setPassword(mqttPassword.toCharArray());
        log.info("Attempting to connect to MQTT broker at: {}", brokerUrl);
        client.connect(options);
        log.info("Successfully connected to MQTT broker at: {}", brokerUrl);
        // L'abonnement se fera dans un service séparé via @PostConstruct ou dans un composant dédié
        return client;
    }

    // Nettoyage à l'arrêt de l'application
    @PreDestroy
    public void disconnectMqtt() {
        try {
            if (mqttClient() != null && mqttClient().isConnected()) {
                mqttClient().disconnect();
                log.info("Disconnected from MQTT broker.");
            }
        } catch (MqttException e) {
            log.error("Error disconnecting from MQTT broker", e);
        }
    }
}