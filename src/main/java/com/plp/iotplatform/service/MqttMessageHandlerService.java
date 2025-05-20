package com.plp.iotplatform.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct; // Important pour l'abonnement au démarrage
import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class MqttMessageHandlerService {

    @Value("${mqtt.subscription.data-topic-pattern}") // Ex: "send/+/+"
    private String dataSubscriptionPattern;

    // Pattern pour extraire ID_CAPTEUR et TYPE_DONNEE de "send/ID_CAPTEUR_GLOBAL/TYPE_DONNEE"
    private static final Pattern DATA_TOPIC_PATTERN = Pattern.compile("^send/([^/]+)/([^/]+)$");

    private final MqttClient mqttClient; // Injecté depuis MqttConfig
    private final SensorService sensorService; // Pour mettre à jour les métadonnées

    @PostConstruct // Cette méthode sera appelée après que le bean MqttClient soit créé et injecté
    public void subscribeToTopics() {
        if (!mqttClient.isConnected()) {
            log.warn("MQTT client not connected. Subscription to {} will be attempted upon reconnection.", dataSubscriptionPattern);
            // La reconnexion automatique devrait gérer cela.
            // Vous pourriez ajouter un listener sur l'état de connexion pour s'abonner après reconnexion.
            return;
        }
        try {
            mqttClient.subscribe(dataSubscriptionPattern, (topic, message) -> {
                log.debug("Backend received raw message on topic: {}", topic);
                try {
                    String payload = new String(message.getPayload());
                    // Le parsing de la valeur n'est plus la responsabilité principale du backend
                    // Double value = Double.parseDouble(payload);

                    Matcher matcher = DATA_TOPIC_PATTERN.matcher(topic);
                    if (matcher.matches()) {
                        String globalSensorId = matcher.group(1); // C'est l'ID global du capteur (PK)
                        String dataType = matcher.group(2);

                        log.info("Backend received data: SensorID='{}', Type='{}', Payload='{}'",
                                globalSensorId, dataType, payload);

                        // Mettre à jour le timestamp de la dernière donnée reçue pour ce capteur
                        sensorService.updateSensorLastDataReceived(globalSensorId, Instant.now());

                    } else {
                        log.warn("Backend received message on topic '{}' which does not match data pattern '{}'.",
                                topic, DATA_TOPIC_PATTERN.pattern());
                    }
                } catch (Exception e) { // Catch plus large pour les erreurs inattendues
                    log.error("Backend error processing message from topic {}: {}", topic, e.getMessage(), e);
                }
            });
            log.info("Backend successfully subscribed to MQTT data topic pattern: {}", dataSubscriptionPattern);
        } catch (MqttException e) {
            log.error("Backend failed to subscribe to MQTT data topic pattern '{}': {}",
                    dataSubscriptionPattern, e.getMessage(), e);
        }
    }
}