package com.plp.iotplatform.config;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MqttClientCleanup {

    private final MqttClient mqttClient;

    public MqttClientCleanup(MqttClient mqttClient) {
        this.mqttClient = mqttClient;
    }

    @PreDestroy
    public void cleanup() {
        try {
            if (mqttClient.isConnected()) {
                mqttClient.disconnect();
                log.info("Disconnected from MQTT broker");
            }
            mqttClient.close();
        } catch (MqttException e) {
            log.error("Error during MQTT client cleanup: {}", e.getMessage());
        }
    }
}