# 🏦 Architecture Bancaire - Architecture Hexagonale

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![MongoDB](https://img.shields.io/badge/MongoDB-Database-green.svg)](https://www.mongodb.com/)
[![Maven](https://img.shields.io/badge/Maven-Multi--Module-red.svg)](https://maven.apache.org/)

## 📋 Description

Application Spring Boot de gestion de clients et de comptes bancaires, développée dans le cadre d'un **cours
universitaire sur l'architecture logicielle** à l'IMT.

Ce projet démontre une **Architecture Hexagonale** (Ports & Adapters) complète avec :

- **6 modules Maven** indépendants
- **Domain framework-agnostic** (sans Spring)
- **Séparation stricte** des responsabilités
- Application des principes **SOLID**

### ✨ Fonctionnalités

#### Gestion des clients

- ✅ CRUD complet (Create, Read, Update, Delete)
- ✅ Validation métier robuste (Chain of Responsibility)
- ✅ Gestion du genre (M/F) avec validation

#### Gestion des comptes bancaires

- ✅ Création de comptes pour un client
- ✅ Consultation et modification des comptes
- ✅ Types de comptes : Compte Courant, Livret A, LDDS
- ✅ Application automatique des agios (événementiel asynchrone)

#### Architecture avancée

- 🎯 **Architecture Hexagonale** : Domain découplé des technologies
- 📦 **Multi-modules Maven** : Séparation stricte
- 🔌 **Ports & Adapters** : Interfaces pour le découplage
- 📡 **Événements asynchrones** : Pattern Observer avec Spring Events

---

## 🏗️ Architecture Hexagonale

### Structure Modulaire (6 modules)

```
architecture/
├── domain/                      🎯 Cœur métier (PAS de Spring)
├── adapters-in-rest/            🔌 API REST (Adaptateur primaire)
├── adapters-in-listeners/       📡 Event Listeners (Adaptateur primaire)
├── adapters-out-bdd/            💾 Persistance MongoDB (Adaptateur secondaire)
├── adapters-out-writers/        📤 Publication événements (Adaptateur secondaire)
└── application/                 🚀 Composition & Démarrage Spring Boot
```

### Diagramme de Dépendances

```
Adapters-IN ──────┐
                  │
                  ▼
               domain  ◄────── Adapters-OUT
                  ▲
                  │
              application
         (Assemble tout via @Configuration)
```

**Règle d'or** : Le `domain` ne dépend d'aucun framework Spring ni d'aucun adaptateur. Les adaptateurs dépendent des contrats métier.

---

## 📦 Description des Modules

### 🎯 `domain` - Cœur Métier

**Responsabilité** : Logique métier pure, framework-agnostic

**Contient** :

- Modèles métier (`Client`, `Compte`)
- Services métier (`ClientsService`, `ComptesService`)
- **Port sortant** : `ClientStorageProvider`
- Métadonnées d'observation métier : `MouvementCompteObserver`
- Validateurs métier (Chain of Responsibility)
- Exceptions métier

**Dépendances** : AUCUNE dépendance Spring !

```xml

<dependencies>
    <dependency>
        <groupId>jakarta.validation</groupId>
        <artifactId>jakarta.validation-api</artifactId>
    </dependency>
</dependencies>
```

### 🔌 `adapters-in-rest` - API REST

**Responsabilité** : Expose l'application via HTTP REST

**Contient** :

- Contrôleurs REST (`ClientsController`, `ComptesController`)
- DTOs Input/Output
- Gestion des exceptions HTTP

**Dépend de** : `domain` uniquement

### 📡 `adapters-in-listeners` - Event Listeners

**Responsabilité** : Écoute les événements métier (traitement asynchrone)

**Contient** :

- `MouvementCompteEventListener` : Ajoute chaque mouvement à l'historique
- `AgiosEventListener` : Applique les agios uniquement pour un mouvement utilisateur

### 💾 `adapters-out-bdd` - Persistance MongoDB

**Responsabilité** : Implémente la persistance des données

**Contient** :

- `ClientsBddService` : Implémente `ClientStorageProvider`
- Repositories Spring Data MongoDB
- Entities et Mappers

### 📤 `adapters-out-writers` - Publication Événements

**Responsabilité** : Publie les événements métier

**Contient** :

- `MouvementCompteEventPublisher` : Aspect qui publie après le succès des méthodes observées
- Le contrat `MouvementCompteEvent` est un événement métier défini dans `domain`

### 🚀 `application` - Composition

**Responsabilité** : Assemble tous les composants et démarre Spring Boot

**Contient** :

- `ArchitectureApplication.java` : Classe principale
- **`ControllerConfiguration.java`** : Crée les Beans des services métier

---

## 📁 Structure Détaillée du Projet

```
architecture/
│
├── domain/                                 # 🎯 Module Domain (Cœur métier)
│   └── src/main/java/
│       └── com.wgt.imt.architecture.business/
│           ├── clients/
│           │   ├── model/
│           │   │   └── Client.java
│           │   ├── validators/
│           │   │   ├── ClientAlreadyExistValidatorStep.java
│           │   │   └── ClientGenreValidatorStep.java
│           │   ├── ClientsService.java
│           │   ├── ClientsServiceValidator.java
│           │   └── ClientStorageProvider.java  ← PORT (interface)
│           │
│           ├── comptes/
│           │   ├── model/
│           │   │   └── Compte.java
│           │   ├── validators/
│           │   ├── ComptesService.java
│           │   ├── ComptesServiceValidator.java
│           │   ├── annotations/MouvementCompteObserver.java
│           │   ├── events/MouvementCompteEvent.java
│           │   └── model/Mouvement.java
│           │
│           └── common/
│               ├── exception/
│               ├── model/
│               ├── validators/
│               └── utils/
│
├── adapters-in-rest/                       # 🔌 Module REST (Adaptateur primaire)
│   └── src/main/java/
│       └── com.wgt.imt.architecture.rest/
│           ├── clients/
│           │   ├── ClientsController.java
│           │   └── model/
│           │       ├── input/
│           │       └── output/
│           ├── comptes/
│           │   ├── ComptesController.java
│           │   └── model/
│           └── common/
│               └── ControllerExceptionHandler.java
│
├── adapters-in-listeners/                  # 📡 Module Listeners (Adaptateur primaire)
│   └── src/main/java/
│       └── com.wgt.imt.architecture.listeners/
│           └── comptes/
│               ├── MouvementCompteEventListener.java
│               └── AgiosEventListener.java
│
├── adapters-out-bdd/                       # 💾 Module BDD (Adaptateur secondaire)
│   └── src/main/java/
│       └── com.wgt.imt.architecture.bdd/
│           └── clients/
│               ├── ClientsBddService.java  ← Implémente ClientStorageProvider
│               └── repositories/
│                   ├── ClientRepository.java
│                   ├── entities/
│                   │   ├── ClientEntity.java
│                   │   ├── CompteEntity.java
│                   │   └── MouvementEntity.java
│                   └── mappers/
│                       ├── ClientBddMapper.java
│                       └── CompteBddMapper.java
│
├── adapters-out-writers/                   # 📤 Module Writers (Adaptateur secondaire)
│   └── src/main/java/
│       └── com.wgt.imt.architecture.writers/
│           └── comptes/
│               └── MouvementCompteEventPublisher.java  ← Aspect Spring
│
└── application/                            # 🚀 Module Application (Composition)
    └── src/main/java/
        └── com.wgt.imt.architecture/
            ├── ArchitectureApplication.java
            └── config/
                └── ControllerConfiguration.java  ← Assemble tout
```

---

## 🔑 Concepts Clés

### Les Ports (Interfaces)

Les **Ports** sont des interfaces définies dans le `domain` qui décrivent ce dont le métier a besoin.

**Exemple de Port** :

```java
// domain/business/clients/ClientStorageProvider.java
public interface ClientStorageProvider {
    boolean exist(UUID identifier);

    Collection<Client> getAll();

    Optional<Client> get(UUID identifier);

    Client save(Client client);

    void delete(UUID identifier);
}
```

### Les Adapters (Implémentations)

Les **Adapters** implémentent les ports avec des technologies concrètes.

**Exemple d'Adapter** :

```java
// adapters-out-bdd/bdd/clients/ClientsBddService.java
@Service
public class ClientsBddService implements ClientStorageProvider {
    private ClientRepository repository;
    private ClientBddMapper mapper;

    @Override
    public Client save(Client client) {
        return mapper.from(
                repository.save(mapper.to(client))
        );
    }
    // ... autres méthodes
}
```

### La Configuration (Assemblage)

Le module **application** assemble tout via Spring :

```java
// application/config/ControllerConfiguration.java
@Configuration
public class ControllerConfiguration {

    @Bean
    public ClientsServiceValidator clientsServiceValidator(
            ClientStorageProvider clientStorageProvider) {  // ← Spring injecte automatiquement
        return new ClientsServiceValidator(clientStorageProvider);
    }
}
```

---

## 🚀 Démarrage Rapide

### Prérequis

- ☕ Java 21
- 🛠️ Maven 3.8+
- 🍃 MongoDB 7.0+ (ou Docker)

### Installation

1. **Cloner le projet**
   ```bash
   git clone <url-du-projet>
   cd architecture
   ```

2. **Démarrer MongoDB**

   **Option A : Avec le script fourni (Windows)**
   ```bash
   # Double-cliquer sur start-mongodb.bat
   # ou exécuter en ligne de commande :
   start-mongodb.bat
   ```

   **Option B : Manuellement avec le chemin de données local**
   ```bash
   mongod.exe --dbpath /c/Users/A388771/local/IMT/architecture/externals/mongo
   ```

   **Option C : Avec Docker (alternative)**
   ```bash
   docker run -d -p 27017:27017 --name mongodb mongo:latest
   ```

3. **Compiler et lancer l'application**

   **Option A : Avec le script fourni (Windows)**
   ```bash
   # Double-cliquer sur start-application.bat
   # ou exécuter en ligne de commande :
   start-application.bat
   ```

   **Option B : Manuellement**
   ```bash
   mvn clean install
   cd application
   mvn spring-boot:run
   ```

4. **Accéder à l'application**
    - API REST : `http://localhost:8080/api/imt/v1`
    - MongoDB : `localhost:27017`

### Tests Postman

Une collection Postman est disponible dans `externals/postman/`.

---

## 📖 Documentation Complète

➡️ **[Voir le Wiki](../architecture.wiki/)** pour la documentation complète :

- **Architecture Hexagonale** : Description détaillée des modules et flux
- **Règles d'Architecture** : Les règles strictes à respecter
- **Design Patterns** : Chain of Responsibility, Observer, etc.
- **API REST** : Documentation des endpoints
- **Technologies** : Stack technique par module

---

## 🔌 API REST - Endpoints Principaux

### Clients

| Méthode  | Endpoint                   | Description                |
|----------|----------------------------|----------------------------|
| `GET`    | `/api/imt/v1/clients`      | Récupérer tous les clients |
| `GET`    | `/api/imt/v1/clients/{id}` | Récupérer un client par ID |
| `POST`   | `/api/imt/v1/clients`      | Créer un nouveau client    |
| `PATCH`  | `/api/imt/v1/clients/{id}` | Mettre à jour un client    |
| `DELETE` | `/api/imt/v1/clients/{id}` | Supprimer un client        |

### Comptes

| Méthode  | Endpoint                                      | Description                            |
|----------|-----------------------------------------------|----------------------------------------|
| `GET`    | `/api/imt/v1/clients/{idClient}/comptes`      | Récupérer tous les comptes d'un client |
| `GET`    | `/api/imt/v1/clients/{idClient}/comptes/{id}` | Récupérer un compte spécifique         |
| `POST`   | `/api/imt/v1/clients/{idClient}/comptes`      | Créer un compte pour un client         |
| `PATCH`  | `/api/imt/v1/clients/{idClient}/comptes/{id}` | Mettre à jour un compte                |
| `DELETE` | `/api/imt/v1/clients/{idClient}/comptes/{id}` | Supprimer un compte                    |

---

## 🎯 Principes SOLID Appliqués

### Single Responsibility (S)

- Chaque module a une responsabilité unique
- `domain` : Logique métier uniquement
- `adapters-in-rest` : Exposition HTTP uniquement
- `adapters-out-bdd` : Persistance uniquement

### Open/Closed (O)

- Ajout de nouveaux adapters sans modifier le domain
- Ajout de validateurs via Chain of Responsibility

### Liskov Substitution (L)

- Tous les validateurs héritent de `AbstractValidatorStep`
- Tous les services de stockage implémentent `ClientStorageProvider`

### Interface Segregation (I)

- Port spécifique : `ClientStorageProvider`; les événements sont déclenchés par métadonnées métier
- Pas d'interface monolithique

### Dependency Inversion (D)

- Le domain définit les interfaces (Ports)
- Les adapters dépendent des abstractions
- Injection via `ControllerConfiguration`

---

## 🧪 Tests

### Tests Unitaires du Domain

Le domain peut être testé **sans Spring** :

```java
class ClientsServiceTest {
    @Test
    void shouldCreateClient() {
        // Mock du port
        ClientStorageProvider mockStorage = mock(ClientStorageProvider.class);

        // Service métier pur
        ClientsService service = new ClientsService(mockStorage);

        // Test
        Client client = service.create(new Client(...));

        verify(mockStorage).save(any());
    }
}
```

### Lancer les tests

```bash
mvn test
```

---

## 🎓 Intérêt Pédagogique

Ce projet est conçu pour démontrer :

### 1. Architecture Hexagonale

- **Découplage complet** du métier et des technologies
- Le domain ne connaît ni Spring, ni MongoDB, ni HTTP
- Changement de technologie sans refonte

### 2. Inversion de Dépendances

- Les abstractions (Ports) sont définies par le métier
- Les implémentations (Adapters) dépendent des abstractions
- Spring connecte tout automatiquement

### 3. Testabilité

- Tests unitaires du domain sans infrastructure
- Tests d'intégration par module
- Mock facile des dépendances

### 4. Évolutivité

- Ajout de nouveaux adapters sans impact sur le domain
- Remplacement de MongoDB par PostgreSQL : créer un nouveau module
- Ajout d'une API GraphQL : créer un nouveau module adapter-in

---

## 📚 Ressources et Références

### Architecture

- [Hexagonal Architecture - Alistair Cockburn](https://alistair.cockburn.us/hexagonal-architecture/)
- [Clean Architecture - Robert C. Martin](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Domain-Driven Design - Eric Evans](https://www.domainlanguage.com/ddd/)

### Design Patterns

- Chain of Responsibility
- Observer Pattern
- Repository Pattern

---

## 👨‍💻 Auteur

**Emmanuel WAGUET**  
Cours d'Architecture Logicielle - IMT

---

## 📄 Licence

Ce projet est développé dans un cadre pédagogique.

---

_Dernière mise à jour : Novembre 2025_
