# Backend de la Plateforme IoT

## 1. Résumé Exécutif et Objectifs du Projet

*   **Contexte :** Brève description du besoin (centralisation des données IoT, gestion des équipements).
*   **Objectifs du Backend :**
    *   Fournir un broker MQTT central et fiable.
    *   Gérer un inventaire précis des hubs et capteurs.
    *   Offrir une API d'administration pour cet inventaire.
    *   Permettre l'enregistrement et la validation de nouveaux équipements.
    *   Servir de source de métadonnées sur l'état des équipements (ex: dernière communication).
*   **Positionnement :** Rôle du backend par rapport aux autres équipes (hubs, data, frontend admin).

## 2. Architecture Globale de la Solution

*   Diagramme d'architecture simplifié montrant les principaux composants :
    *   Hubs IoT
    *   Broker MQTT (Mosquitto)
    *   Backend Applicatif (ce projet)
    *   Base de Données d'Inventaire (PostgreSQL)
    *   Frontend d'Administration (consommateur de l'API backend)
    *   Système de l'Équipe Data (consommateur du MQTT)
*   Flux de données principaux (un schéma de haut niveau).

## 3. Fonctionnalités Implémentées du Backend

*   Description détaillée de chaque fonctionnalité majeure **effectivement codée et testable** :
    *   **3.1. Gestion de l'Inventaire (API REST Admin)**
        *   CRUD Hubs (création manuelle par admin = statut `ACTIVE` direct)
            *   Endpoints:
                * `POST /api/admin/hubs` - Création d'un nouveau hub
                * `GET /api/admin/hubs` - Liste de tous les hubs
                * `GET /api/admin/hubs/{hubId}` - Détails d'un hub spécifique
                * `PUT /api/admin/hubs/{hubId}` - Mise à jour d'un hub existant
                * `DELETE /api/admin/hubs/{hubId}` - Suppression d'un hub
                * `GET /api/admin/hubs/pending` - Liste des hubs en attente de validation
            *   Champs gérés (MAC, nom, localisation, etc.)
        *   CRUD Capteurs (création manuelle par admin = statut `ACTIVE` direct, lié à un hub)
            *   Endpoints:
                * `POST /api/admin/hubs/{hubId}/sensors` - Ajout d'un capteur à un hub
                * `GET /api/admin/hubs/{hubId}/sensors` - Liste des capteurs d'un hub spécifique
                * `GET /api/admin/sensors/{sensorId}` - Détails d'un capteur spécifique
                * `PUT /api/admin/sensors/{sensorId}` - Mise à jour d'un capteur
                * `DELETE /api/admin/sensors/{sensorId}` - Suppression d'un capteur
                * `GET /api/admin/sensors/pending` - Liste des capteurs en attente de validation
            *   Champs gérés (`localId`, nom, type, etc.)
    *   **3.2. Demandes d'Enregistrement et Validation (Auto-Discovery Simplifié)**
        *   API d'enregistrement des appareils:
            *   `POST /api/discovery/registration-request` (sans authentification complexe)
            *   Payload attendu (type "hub" ou "sensor", infos associées).
            *   Logique de création d'entités avec statut `PENDING_VALIDATION`.
        *   APIs de Validation Admin:
            *   `POST /api/admin/hubs/{hubId}/validate` - Valide un hub (passe à `ACTIVE`)
            *   `POST /api/admin/sensors/{sensorId}/validate` - Valide un capteur (passe à `ACTIVE`)
            *   `POST /api/admin/hubs/{hubId}/reject` - Rejette un hub (supprime si `PENDING_VALIDATION`)
            *   `POST /api/admin/sensors/{sensorId}/reject` - Rejette un capteur (supprime si `PENDING_VALIDATION`)
    *   **3.3. Déploiement et Fourniture du Broker MQTT**
        *   Configuration via `docker-compose.yml` (Mosquitto).
        *   Mention de la configuration actuelle (ex: accès anonyme pour la phase initiale).
    *   **3.4. Consommation des Messages MQTT par le Backend**
        *   Abonnement au pattern `hubs/<MAC_HUB>/send/<NOM_CAPTEUR_PHYSIQUE>/<TYPE_DONNEE>`.
        *   Logique d'extraction des informations du topic.
        *   Mise à jour du champ `lastDataReceivedAt` dans l'entité `Sensor` (PostgreSQL).
        *   **Précision :** Le backend ne stocke pas les valeurs des mesures.
    *   **3.5. Détection de Statut "Offline" (Simplifié)**
        *   Tâche planifiée (`@Scheduled`).
        *   Logique de vérification basée sur `lastDataReceivedAt` et un seuil.
        *   Mise à jour du statut du capteur en `OFFLINE`.

## 4. Flux de Fonctionnement Détaillés

*   Cette section est cruciale pour expliquer comment les choses s'articulent. Reprenez les flux que nous avons détaillés :
    *   **4.1. Flux de Données Capteur (Fonctionnement Normal)**
        *   Hub -> Publication MQTT (topic `hubs/.../send/...`) -> Broker MQTT -> Consommation par le Backend (mise à jour `lastDataReceivedAt`) ET Consommation par l'Équipe Data.
    *   **4.2. Flux d'Ajout Manuel d'un Hub par l'Admin**
        *   Admin (Frontend) -> API Backend (`POST /admin/hubs`) -> Création en BDD (statut `ACTIVE`).
        *   Configuration manuelle du hub physique par l'équipe des hubs.
    *   **4.3. Flux d'Ajout Manuel d'un Capteur par l'Admin**
        *   Admin (Frontend) -> API Backend (`POST /admin/hubs/{hubId}/sensors`) -> Création en BDD (statut `ACTIVE`).
        *   Configuration manuelle sur l'agent du hub par l'équipe des hubs.
    *   **4.4. Flux d'Enregistrement et Validation d'un Nouveau Hub (Auto-Discovery)**
        *   Agent Externe/Hub -> API Backend (`POST /discovery/registration-request`) -> Création en BDD (statut `PENDING_VALIDATION`).
        *   Admin (Frontend) -> Voit la demande -> API Backend (`POST /admin/hubs/{id}/validate`) -> Mise à jour statut en `ACTIVE`.
        *   Communication des crédentials/configuration au hub (actuellement manuelle).
    *   **4.5. Flux d'Enregistrement et Validation d'un Nouveau Capteur (Auto-Discovery)**
        *   Similaire au hub, mais initié pour un capteur sur un hub déjà existant.

## 5. Technologies et Structure du Projet Backend

*   **Langage et Framework :** Java, Spring Boot (version).
*   **Base de Données :** PostgreSQL (version).
*   **Broker MQTT :** Mosquitto (version).
*   **Gestion des Dépendances :** Maven.
*   **Structure des Packages Principaux :** `config`, `controller`, `dto`, `entity`, `enums`, `exception`, `mapper`, `repository`, `service`.
*   **Dépendances Clés :** `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-validation`, `org.eclipse.paho.client.mqttv3`, `springdoc-openapi-starter-webmvc-ui`, `org.mapstruct`, `lombok`.
*   **Conteneurisation :** Docker, Docker Compose.

## 6. Configuration et Lancement

*   **Prérequis :** Docker, Docker Compose, JDK, Maven.
*   **Configuration :**
    *   `application.properties` (principales propriétés : datasource, MQTT broker URL, pattern d'abonnement).
    *   `docker-compose.yml` (configuration des services PostgreSQL, Mosquitto, et l'application backend).
*   **Instructions de Lancement :**
    1.  Cloner le repository.
    2.  Configurer `application.properties` si nécessaire (pour des environnements hors Docker Compose, mais normalement les variables d'env Docker suffisent).
    3.  Lancer avec `docker compose up --build -d`.
    4.  Accès à Swagger UI : `http://localhost:8080/swagger-ui/index.html`.
    5.  Accès aux logs : `docker compose logs -f app`.

## 7. Implications et Responsabilités des Différentes Équipes

*   **7.1. Équipe Backend et Front Admin (Ce Projet) :**
    *   Développement et maintenance du code backend (API, services, logique MQTT, BDD d'inventaire).
    *   Déploiement et gestion du broker MQTT et de la BDD d'inventaire (via Docker Compose).
    *   Définition et documentation des API REST pour le frontend admin.
    *   Définition du format des topics MQTT que le backend écoute pour les métadonnées. 
    *   Développe l'interface utilisateur pour les administrateurs. 
    *   Consomme les API REST fournies par ce backend pour afficher l'inventaire, gérer les demandes de validation, et effectuer les opérations CRUD.
    *   **Ne gère pas :** Le code sur les hubs, le stockage/traitement des données de séries temporelles, le développement du frontend admin.
*   **7.2. Équipe de Gestion des Hubs/Agents IoT :**
    *   Développement de l'agent logiciel sur les hubs.
    *   Configuration initiale des hubs (réseau, adresse du broker MQTT).
    *   Configuration des crédentials MQTT sur les hubs (une fois fournies par le backend/admin).
    *   Programmation de l'agent pour :
        *   Appeler l'API `/api/discovery/registration-request` si un hub/capteur est nouveau et non configuré.
        *   Publier les données des capteurs sur le broker MQTT en respectant le format de topic convenu : `hubs/<MAC_HUB>/send/<NOM_CAPTEUR_PHYSIQUE>/<TYPE_DONNEE>`.
        *   Utiliser le `NOM_CAPTEUR_PHYSIQUE` qui a été enregistré/validé dans le backend.
*   **7.3. Équipe Data :**
    *   Principal consommateur des données brutes du broker MQTT.
    *   S'abonne aux topics pertinents (ex: `hubs/+/send/+/+` ou plus spécifiques).
    *   Responsable du stockage, du parsing, du traitement et de l'analyse des données de séries temporelles.
    *   Définit le format du payload si plus complexe que des valeurs brutes.


## 8. Limites Actuelles et Pistes d'Amélioration

*   **8.1. Sécurité :**
    *   **Broker MQTT :** Actuellement configuré pour un accès potentiellement anonyme (à vérifier dans `mosquitto.conf`). Nécessite la mise en place d'une authentification forte pour les hubs et des ACLs.
    *   **APIs Backend :** Pas de sécurité Spring Security implémentée (authentification des utilisateurs admin, rôles, autorisation).
    *   **API de Discovery :** `POST /api/discovery/registration-request` est publique. Pourrait nécessiter une clé API simple pour éviter les abus.
*   **8.2. Communication des Crédentials/Configuration aux Hubs :**
    *   La génération des crédentials MQTT par le backend est prévue, mais leur transmission et configuration sur les hubs physiques sont actuellement manuelles après validation.
    *   Aucun mécanisme pour que le backend "pousse" des configurations (ex: l'ID global d'un capteur validé) vers un hub.
*   **8.3. Gestion des Erreurs et Robustesse :**
    *   La gestion des erreurs peut être affinée (codes d'erreur plus spécifiques, messages plus détaillés).
    *   La reconnexion MQTT et la gestion des files d'attente de messages pourraient être plus robustes (actuellement repose sur les capacités de base de Paho). Spring Integration MQTT pourrait offrir plus.
*   **8.4. Scalabilité :**
    *   Un seul broker Mosquitto et une seule instance backend pourraient devenir des goulots d'étranglement avec un grand nombre d'équipements.
*   **8.5. Monitoring du Backend Lui-même :**
    *   Les métriques Actuator sont une base, mais un monitoring plus poussé pourrait être nécessaire.
*   **8.6. Tests :**
    *   Absence de tests unitaires et d'intégration automatisés.
*   **8.7. Identification des Capteurs dans les Topics MQTT :**
    *   Le format de topic actuel `hubs/<MAC_HUB>/send/<NOM_CAPTEUR_PHYSIQUE>/<TYPE_DONNEE>` est bon car il inclut la MAC du hub, ce qui permet au backend de trouver le bon hub et ensuite le capteur par son `NOM_CAPTEUR_PHYSIQUE` (qui est unique *par hub*). C'est une amélioration par rapport à `send/NOM_CAPTEUR_PHYSIQUE_GLOBAL_UNIQUE/...` qui nécessiterait que le `NOM_CAPTEUR_PHYSIQUE` soit unique à travers *tous* les hubs.

## 9. Comparaison avec un Cas Réel Idéal et Suggestions

*   **Cas Réel Idéal :**
    *   **Sécurité de bout en bout :** TLS pour toutes les communications (API, MQTT), authentification mutuelle par certificats pour MQTT, gestion centralisée des identités (ex: Keycloak).
    *   **Provisioning Zero-Touch (ou quasi) des Hubs :** Mécanismes automatisés pour qu'un nouveau hub récupère sa configuration et ses crédentials de manière sécurisée après son branchement.
    *   **Gestion de Flotte d'Appareils (Device Management) :** Fonctionnalités pour les mises à jour OTA (Over-The-Air) des agents sur les hubs, diagnostics à distance, révocation de crédentials.
    *   **Broker MQTT Hautement Disponible et Scalable :** Cluster EMQX ou Kafka pour de très gros volumes.
    *   **Pipelines de Données Robustes :** Utilisation de files de messages (Kafka, RabbitMQ) entre le broker MQTT et les consommateurs pour la résilience.
    *   **Monitoring et Alerting Complets.**
*   **Suggestions pour ce Projet :**
    *   **Priorité 1 (Après la base actuelle) :** Sécuriser le broker MQTT avec authentification et ACLs gérées par le backend.
    *   **Priorité 2 :** Mettre en place Spring Security pour les APIs admin.
    *   **Considérer Spring Integration MQTT :** Pour une gestion plus robuste et déclarative des flux MQTT dans le backend.
    *   **Définir un mécanisme de communication du backend vers les hubs :** Même simple au début (ex: un topic MQTT de config que le hub écoute).
    *   **Standardiser les `TYPE_DONNEE`** et les formats de payload JSON avec l'équipe Data.


## 10. Scénarios de Test Fonctionnels Clés

Cette section décrit plusieurs scénarios de test pour valider les fonctionnalités principales du backend. Il est recommandé d'utiliser un outil comme Postman ou `curl` pour les requêtes API, et MQTT Explorer (ou `mosquitto_pub`) pour les messages MQTT.

**Prérequis pour les tests :**
*   L'ensemble des services (PostgreSQL, Mosquitto, Application Backend) est démarré via `docker compose up --build -d`.
*   L'application backend écoute sur `http://localhost:8080`.
*   Le broker MQTT écoute sur `localhost:1883`.

---

### Scénario 1 : Ajout Manuel d'un Hub et d'un Capteur, et Vérification

**Objectif :** Valider la capacité d'un administrateur à créer manuellement un hub et un capteur, et à récupérer leurs informations. Ces équipements sont immédiatement `ACTIVE`.

1.  **Créer un Nouveau Hub (Manuel Admin) :**
    *   **Pourquoi :** Simule l'action d'un administrateur enregistrant un hub physiquement installé.
    *   **Commande `curl` :**
        ```bash
        curl -X POST -H "Content-Type: application/json" -d '{
          "macAddress": "AA:BB:CC:11:22:33",
          "name": "Hub_Bureau_Admin",
          "location": "Bureau Principal - Étage 1",
          "description": "Hub ajouté manuellement pour le bureau admin"
        }' http://localhost:8080/api/admin/hubs
        ```
    *   **Réponse Attendue :** `201 Created` avec les détails du hub. **Notez l' `id` du hub retourné** (ex: `HUB_ADMIN_ID`).

2.  **Vérifier la Création du Hub :**
    *   **Pourquoi :** Confirmer que le hub est bien enregistré et listable.
    *   **Commande `curl` (remplacez `HUB_ADMIN_ID` par l'ID réel) :**
        ```bash
        curl -X GET http://localhost:8080/api/admin/hubs/HUB_ADMIN_ID
        ```
    *   **Réponse Attendue :** `200 OK` avec les détails du "Hub_Bureau_Admin", statut `ACTIVE`.

3.  **Ajouter un Capteur au Hub Créé (Manuel Admin) :**
    *   **Pourquoi :** Simule l'ajout d'un capteur à un hub existant par un administrateur.
    *   **Commande `curl` (remplacez `HUB_ADMIN_ID` par l'ID réel) :**
        ```bash
        curl -X POST -H "Content-Type: application/json" -d '{
          "localId": "TEMP_BUREAU_01",
          "name": "Température Bureau Admin",
          "type": "TEMPERATURE",
          "unit": "C",
          "description": "Capteur de température principal du bureau"
        }' http://localhost:8080/api/admin/hubs/HUB_ADMIN_ID/sensors
        ```
    *   **Réponse Attendue :** `201 Created` avec les détails du capteur. **Notez l'`id` global du capteur retourné** (ex: `SENSOR_ADMIN_ID`).

4.  **Vérifier la Création du Capteur :**
    *   **Pourquoi :** Confirmer que le capteur est bien enregistré et lié au bon hub.
    *   **Commande `curl` (remplacez `SENSOR_ADMIN_ID` par l'ID réel) :**
        ```bash
        curl -X GET http://localhost:8080/api/admin/sensors/SENSOR_ADMIN_ID
        ```
    *   **Réponse Attendue :** `200 OK` avec les détails du "Température Bureau Admin", statut `ACTIVE`, et le `hubId` correspondant à `HUB_ADMIN_ID`.

---

### Scénario 2 : Flux de Données MQTT et Mise à Jour des Métadonnées

**Objectif :** Valider que le backend reçoit les messages MQTT, les parse correctement, et met à jour le champ `lastDataReceivedAt` du capteur correspondant.

*   **Prérequis :** Le Hub "Hub_Bureau_Admin" (MAC `AA:BB:CC:11:22:33`) et son capteur "TEMP_BUREAU_01" (ID global `SENSOR_ADMIN_ID`) ont été créés (voir Scénario 1).

1.  **Consulter l'état initial du capteur :**
    *   **Pourquoi :** Observer la valeur de `lastDataReceivedAt` avant la publication MQTT.
    *   **Commande `curl` (remplacez `SENSOR_ADMIN_ID`) :**
        ```bash
        curl -X GET http://localhost:8080/api/admin/sensors/SENSOR_ADMIN_ID
        ```
    *   **Réponse Attendue :** `lastDataReceivedAt` sera `null` ou une ancienne valeur.

2.  **Publier un Message MQTT :**
    *   **Pourquoi :** Simuler l'envoi d'une donnée par l'agent du hub.
    *   **Outil :** MQTT Explorer (ou `mosquitto_pub`).
    *   **Connexion :** `localhost:1883`.
    *   **Publication :**
        *   **Topic :** `hubs/AA:BB:CC:11:22:33/send/TEMP_BUREAU_01/temperature`
        *   **Payload :** `24.7`
    *   **Vérifier les logs du backend (`docker compose logs -f app`) :** Vous devriez voir des logs indiquant la réception du message et l'extraction des informations du topic.

3.  **Consulter l'état du capteur après publication MQTT :**
    *   **Pourquoi :** Vérifier que `lastDataReceivedAt` a été mis à jour.
    *   **Commande `curl` (remplacez `SENSOR_ADMIN_ID`) :**
        ```bash
        curl -X GET http://localhost:8080/api/admin/sensors/SENSOR_ADMIN_ID
        ```
    *   **Réponse Attendue :** `200 OK`. Le champ `lastDataReceivedAt` doit maintenant afficher un timestamp récent, correspondant à peu près au moment de la publication MQTT.

4.  **(Optionnel) Tester l'API de Démo `/latest` :**
    *   **Pourquoi :** Vérifier cet endpoint spécifique (qui retourne des métadonnées et une valeur simulée).
    *   **Commande `curl` (remplacez `SENSOR_ADMIN_ID`) :**
        ```bash
        curl -X GET http://localhost:8080/api/data/latest/SENSOR_ADMIN_ID
        ```
    *   **Réponse Attendue :** `200 OK` avec les métadonnées du capteur (y compris le `lastDataReceivedAt` mis à jour) et une `"simulatedValue"`.

---

### Scénario 3 : Demande d'Enregistrement (Discovery) d'un Hub et Validation Admin

**Objectif :** Valider le flux d'auto-discovery pour un nouveau hub, de sa demande initiale à sa validation par un administrateur.

1.  **Soumettre une Demande d'Enregistrement pour un Nouveau Hub :**
    *   **Pourquoi :** Simuler un nouveau hub non configuré qui se signale au backend.
    *   **Commande `curl` :**
        ```bash
        curl -X POST -H "Content-Type: application/json" -d '{
          "type": "HUB",
          "hubMacAddress": "DE:AD:BE:EF:CA:FE",
          "hubName": "Hub_Discovery_Test"
        }' http://localhost:8080/api/discovery/registration-request
        ```
    *   **Réponse Attendue :** `202 Accepted`.
    *   **Vérifier les logs du backend :** Un message indiquant la réception d'une nouvelle demande de hub.

2.  **Lister les Hubs en Attente de Validation (Admin) :**
    *   **Pourquoi :** Simuler l'action de l'admin vérifiant les nouvelles demandes.
    *   **Commande `curl` :**
        ```bash
        curl -X GET http://localhost:8080/api/admin/hubs/pending
        ```
    *   **Réponse Attendue :** `200 OK` avec une liste contenant "Hub_Discovery_Test" (ou le nom assigné) avec le statut `PENDING_VALIDATION`. **Notez son `id`** (ex: `HUB_PENDING_ID`).

3.  **Valider le Hub (Admin) :**
    *   **Pourquoi :** Simuler l'approbation de la demande par l'admin.
    *   **Commande `curl` (remplacez `HUB_PENDING_ID`) :**
        ```bash
        curl -X POST http://localhost:8080/api/admin/hubs/HUB_PENDING_ID/validate
        ```
    *   **Réponse Attendue :** `200 OK` avec les détails du hub, maintenant avec le statut `ACTIVE`.

4.  **Vérifier le Statut du Hub après Validation :**
    *   **Pourquoi :** Confirmer le changement de statut.
    *   **Commande `curl` (remplacez `HUB_PENDING_ID`) :**
        ```bash
        curl -X GET http://localhost:8080/api/admin/hubs/HUB_PENDING_ID
        ```
    *   **Réponse Attendue :** `200 OK` avec le hub et son statut `ACTIVE`.

---

### Scénario 4 : Détection de Capteur "Offline" (Simplifié)

**Objectif :** Valider que la tâche planifiée marque correctement un capteur comme `OFFLINE` s'il n'envoie pas de données pendant une période définie.

*   **Prérequis :**
    *   Un capteur `ACTIVE` existe (ex: `SENSOR_ADMIN_ID` du Scénario 1).
    *   Pour tester rapidement :
        1.  Dans `StatusUpdateService.java`, changez `OFFLINE_THRESHOLD_HOURS` pour une valeur très courte, par exemple en minutes : `private static final long OFFLINE_THRESHOLD_MINUTES = 1;` et adaptez la logique : `Instant offlineCutoff = Instant.now().minus(OFFLINE_THRESHOLD_MINUTES, ChronoUnit.MINUTES);`
        2.  Dans `application.properties`, réglez `status.update.scheduler.fixedRate=60000` (1 minute).
        3.  Reconstruisez et redémarrez le conteneur `app` : `docker compose up --build -d app`.

1.  **Vérifier le statut initial du capteur :**
    *   **Commande `curl` (remplacez `SENSOR_ADMIN_ID`) :**
        ```bash
        curl -X GET http://localhost:8080/api/admin/sensors/SENSOR_ADMIN_ID
        ```
    *   **Réponse Attendue :** Statut `ACTIVE`. Le `lastDataReceivedAt` peut être celui du Scénario 2.

2.  **Attendre :** Ne publiez **aucun message MQTT** pour ce capteur. Attendez que la tâche planifiée (`StatusUpdateService.checkForOfflineSensors`) s'exécute au moins une ou deux fois (plus de 1-2 minutes avec la configuration de test).
    *   **Vérifier les logs du backend :** Vous devriez voir `Running scheduled task to check for offline sensors...`.

3.  **Vérifier le statut du capteur (devrait être OFFLINE) :**
    *   **Pourquoi :** Confirmer que la tâche a changé le statut.
    *   **Commande `curl` (remplacez `SENSOR_ADMIN_ID`) :**
        ```bash
        curl -X GET http://localhost:8080/api/admin/sensors/SENSOR_ADMIN_ID
        ```
    *   **Réponse Attendue :** `200 OK` avec le capteur et son statut maintenant à `OFFLINE`.

4.  **Renvoyer une Donnée MQTT pour ce Capteur :**
    *   **Pourquoi :** Vérifier si le capteur redevient `ACTIVE`.
    *   **Outil :** MQTT Explorer.
    *   **Topic :** `hubs/AA:BB:CC:11:22:33/send/TEMP_BUREAU_01/temperature`
    *   **Payload :** `21.0`
    *   **Vérifier les logs du backend :** Vous devriez voir la réception du message et potentiellement un log indiquant que le statut est repassé à `ACTIVE`.

5.  **Vérifier à nouveau le statut du capteur (devrait être ACTIVE) :**
    *   **Commande `curl` (remplacez `SENSOR_ADMIN_ID`) :**
        ```bash
        curl -X GET http://localhost:8080/api/admin/sensors/SENSOR_ADMIN_ID
        ```
    *   **Réponse Attendue :** `200 OK` avec le capteur et son statut revenu à `ACTIVE`, et `lastDataReceivedAt` mis à jour.

**Important :** N'oubliez pas de remettre les valeurs normales pour `OFFLINE_THRESHOLD_HOURS` et `status.update.scheduler.fixedRate` après ce test.

---

## 11. Conclusion

Le backend actuel fournit les fondations essentielles pour la gestion de l'inventaire et le routage initial des données IoT. Les fonctionnalités implémentées permettent une administration manuelle et un début d'auto-discovery. Les prochaines étapes se concentreront sur la sécurisation et l'amélioration des mécanismes d'onboarding des équipements.

---
