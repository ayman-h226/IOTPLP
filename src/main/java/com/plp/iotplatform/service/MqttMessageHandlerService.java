package com.plp.iotplatform.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*; // Importer les classes nécessaires
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component; // Utiliser @Component
import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component // Rendre ce bean gérable par Spring pour l'injection
@RequiredArgsConstructor
public class MqttMessageHandlerService implements MqttCallbackExtended {

    @Value("${mqtt.subscription.data-topic-pattern}") // Ex: "hubs/+/send/+/+"
    private String dataSubscriptionPattern;

    // Pattern pour extraire MAC_HUB, SENSOR_LOCAL_ID, TYPE_DONNEE de "hubs/MAC/send/LOCAL_ID/TYPE"
    private static final Pattern DATA_TOPIC_PATTERN = Pattern.compile("^hubs/([^/]+)/send/([^/]+)/([^/]+)$");

    // Pas d'injection de MqttClient ici, il sera passé par MqttConfig ou utilisé via un appel de méthode
    private final SensorService sensorService; // Pour mettre à jour les métadonnées
    private final MqttClient mqttClient;


    // Appelée par MqttConfig après la connexion initiale
    public void performInitialSubscription(MqttClient client) {
        subscribeToTopics(client);
    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        log.info("MQTT connectComplete: Reconnect={}, ServerURI={}. Re-subscribing...", reconnect, serverURI);
        if (mqttClient != null && mqttClient.isConnected()) { // Utiliser le client injecté
            subscribeToTopics(mqttClient); // S'assurer qu'on utilise le client MQTT actif
        } else {
            log.warn("MqttClient is not connected in connectComplete, cannot subscribe.");
        }
    }


    public void subscribeToTopics(MqttClient client) { // Méthode pour que MqttConfig puisse initier l'abonnement
        if (client == null || !client.isConnected()) {
            log.warn("MQTT client is null or not connected. Cannot subscribe to topics.");
            return;
        }
        try {
            log.info("Attempting to subscribe to MQTT data topic pattern: {}", dataSubscriptionPattern);
            // QoS 0 pour les messages que le backend reçoit (fiabilité moindre, mais moins d'overhead)
            // QoS 1 ou 2 si on ne veut pas perdre de mises à jour de lastDataReceivedAt
            client.subscribe(dataSubscriptionPattern, 1);
            log.info("Backend successfully subscribed to MQTT data topic pattern: {}", dataSubscriptionPattern);
        } catch (MqttException e) {
            log.error("Backend failed to subscribe to MQTT data topic pattern '{}': {}",
                    dataSubscriptionPattern, e.getMessage(), e);
        }
    }


    @Override
    public void connectionLost(Throwable cause) {
        log.warn("MQTT connection lost! Cause: {}", cause != null ? cause.getMessage() : "Unknown");        // La reconnexion automatique de Paho devrait tenter de se reconnecter.
        // La méthode connectComplete sera appelée après une reconnexion réussie.
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
        log.debug("Backend received raw message on topic: {} with payload: {}", topic, new String(mqttMessage.getPayload()));
        try {
            String payload = new String(mqttMessage.getPayload());
            Matcher matcher = DATA_TOPIC_PATTERN.matcher(topic);

            if (matcher.matches()) {
                String hubMacAddress = matcher.group(1);
                String sensorLocalId = matcher.group(2);
                String dataType = matcher.group(3);

                log.info("Backend received data: HubMAC='{}', SensorLocalID='{}', Type='{}', Payload='{}'",
                        hubMacAddress, sensorLocalId, dataType, payload);

                // Mettre à jour le timestamp de la dernière donnée reçue pour ce capteur
                sensorService.updateSensorLastDataReceivedByMacAndLocalId(hubMacAddress, sensorLocalId, Instant.now());

            } else {
                log.warn("Backend received message on topic '{}' which does not match data pattern '{}'. Pattern was: {}",
                        topic, dataSubscriptionPattern, DATA_TOPIC_PATTERN.pattern());
            }
        } catch (Exception e) {
            log.error("Backend error processing message from topic {}: {}", topic, e.getMessage(), e);
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // Non pertinent pour la consommation de messages
    }
}