package com.plp.iotplatform.service;

import com.plp.iotplatform.enums.SensorStatus;
import com.plp.iotplatform.entity.Sensor;
import com.plp.iotplatform.repository.SensorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Service responsable de surveiller et mettre à jour le statut des capteurs
 * selon leur activité récente.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class StatusUpdateService {

    private final SensorRepository sensorRepository;

    @Value("${sensor.offline.threshold.hours:24}")
    private long offlineThresholdHours;

    /**
     * Vérifie périodiquement si des capteurs actifs n'ont pas transmis de données
     * depuis un certain temps et les marque comme OFFLINE.
     */
    @Scheduled(fixedRateString = "${status.update.scheduler.fixedRate:3600000}",
               initialDelayString = "${status.update.scheduler.initialDelay:60000}")
    @Transactional
    public void checkForOfflineSensors() {
        log.info("Running scheduled task to check for offline sensors...");

        try {
            Instant offlineCutoff = Instant.now().minus(offlineThresholdHours, ChronoUnit.HOURS);

            // Utiliser la nouvelle méthode optimisée du repository
            List<Sensor> inactiveSensors = sensorRepository.findSensorsInactiveByStatusAndLastDataBefore(
                    SensorStatus.ACTIVE, offlineCutoff);

            if (inactiveSensors.isEmpty()) {
                log.info("No sensors to mark as OFFLINE in this run.");
                return;
            }

            for (Sensor sensor : inactiveSensors) {
                sensor.setStatus(SensorStatus.OFFLINE);
                log.warn("Sensor {} (Hub {}) marked as OFFLINE due to inactivity since {}.",
                        sensor.getLocalId(),
                        sensor.getHub() != null ? sensor.getHub().getName() : "unknown",
                        sensor.getLastDataReceivedAt() != null ? sensor.getLastDataReceivedAt() : "never received data");
            }

            // Enregistrer tous les capteurs en une seule opération
            sensorRepository.saveAll(inactiveSensors);
            log.info("Marked {} sensors as OFFLINE.", inactiveSensors.size());

        } catch (Exception e) {
            log.error("Error while checking for offline sensors", e);
            // Ne pas relancer l'exception pour éviter que le scheduler ne s'arrête
        }
    }

    /**
     * Méthode manuelle pour vérifier les capteurs offline à la demande
     * @return le nombre de capteurs marqués OFFLINE
     */
    @Transactional
    public int checkOfflineSensorsManually() {
        log.info("Manual check for offline sensors triggered");
        Instant offlineCutoff = Instant.now().minus(offlineThresholdHours, ChronoUnit.HOURS);

        List<Sensor> inactiveSensors = sensorRepository.findSensorsInactiveByStatusAndLastDataBefore(
                SensorStatus.ACTIVE, offlineCutoff);

        if (inactiveSensors.isEmpty()) {
            return 0;
        }

        inactiveSensors.forEach(sensor -> sensor.setStatus(SensorStatus.OFFLINE));
        sensorRepository.saveAll(inactiveSensors);

        return inactiveSensors.size();
    }
}