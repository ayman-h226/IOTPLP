package com.plp.iotplatform.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plp.iotplatform.model.MqttMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor // Ajoute automatiquement InfluxDBService au constructeur
public class MqttService {

    @Value("${mqtt.topic.sensor-data}") // Doit être "sensors/+/data"
    private String sensorTopicPattern;

    private static final Pattern SENSOR_ID_PATTERN = Pattern.compile("sensors/([^/]+)/data");

    private final MqttClient mqttClient;

    private final List<MqttMessage> latestMessages = new ArrayList<>();
    private static final int MAX_MESSAGES = 10;

    @PostConstruct
    public void subscribe() {
        String subscriptionTopic = sensorTopicPattern.replace("+", "#"); // S'abonner à tous les sous-niveaux si besoin, ou garder le '+'

        try {
            mqttClient.subscribe(sensorTopicPattern, (topic, message) -> { // Le 'topic' reçu ici sera le topic réel (ex: sensors/TEST001/data)
                log.debug("Raw message received on actual topic: {}", topic);
                try {
                    String payload = new String(message.getPayload());
                    Double value = Double.parseDouble(payload);
                    log.info("Received and parsed message on topic {} with value: {}", topic, value);

                    Matcher matcher = SENSOR_ID_PATTERN.matcher(topic);
                    if (matcher.matches()) {
                        String sensorId = matcher.group(1); // Le groupe 1 contient ce qui est entre parenthèses dans le Pattern
                        log.info("Extracted sensorId: {}", sensorId);

                        log.info("Attempting to write to InfluxDB for sensor: {}", sensorId);
                        log.info("Successfully wrote to InfluxDB for sensor: {}", sensorId);

                    } else {
                        log.warn("Received message on topic '{}' which does not match the expected pattern '{}'. Cannot extract sensorId.", topic, SENSOR_ID_PATTERN.pattern());
                    }

                    MqttMessage mqttMessage = new MqttMessage(value, System.currentTimeMillis());
                    synchronized (latestMessages) {
                        latestMessages.add(0, mqttMessage);
                        if (latestMessages.size() > MAX_MESSAGES) {
                            latestMessages.remove(latestMessages.size() - 1);
                        }
                    }
                } catch (NumberFormatException e) {
                    log.error("Failed to parse message payload as number: '{}' on topic '{}'", new String(message.getPayload()), topic, e);
                } catch (Exception e) {
                    log.error("Error processing message from topic {}: {}", topic, e.getMessage(), e);
                }
            });
            log.info("Successfully subscribed to MQTT topic pattern: {}", sensorTopicPattern);
        } catch (MqttException e) {
            log.error("Failed to subscribe to MQTT topic pattern '{}': {}", sensorTopicPattern, e.getMessage(), e);
        }
    }

    public List<MqttMessage> getLatestMessages() {
        synchronized (latestMessages) {
            return new ArrayList<>(latestMessages);
        }
    }
}