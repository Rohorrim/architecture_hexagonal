# 🚀 Module : application

**Composition & Démarrage** - Assemblage de tous les composants

---

## 🎯 Responsabilité

Ce module est le **point d'entrée** de l'application Spring Boot. Il a pour rôle d'**assembler tous les composants** (
domain + adapters) via l'injection de dépendances Spring et de **démarrer l'application**.

C'est ici que les **Ports** (interfaces du domain) sont **connectés** aux **Adapters** (implémentations concrètes).

---

## 🏗️ Architecture

```
┌──────────────────────────────────────────────────────────────┐
│                    application                               │
│                                                              │
│  ┌────────────────────────────────────────────────────────┐ │
│  │ ArchitectureApplication.java                           │ │
│  │  @SpringBootApplication                                │ │
│  │  @EnableAsync                                          │ │
│  │  main()                                                │ │
│  └────────────────────────────────────────────────────────┘ │
│                                                              │
│  ┌────────────────────────────────────────────────────────┐ │
│  │ ControllerConfiguration.java                           │ │
│  │  @Configuration                                        │ │
│  │                                                        │ │
│  │  @Bean clientsServiceValidator()                      │ │
│  │  @Bean comptesServiceValidator()                      │ │
│  │                                                        │ │
│  │  ⚡ Injection de dépendances :                         │ │
│  │     - ClientStorageProvider                           │ │
│  └────────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────────┘
                          │
                          │ agrège et assemble
                          ▼
┌──────────────────────────────────────────────────────────────┐
│                    Tous les modules                          │
│                                                              │
│  domain + adapters-in-rest + adapters-in-listeners           │
│  + adapters-out-bdd + adapters-out-writers                   │
└──────────────────────────────────────────────────────────────┘
```

---

## 📁 Structure du module

```
application/
├── pom.xml
├── src/
│   └── main/
│       ├── java/
│       │   └── com.wgt.imt.architecture/
│       │       ├── ArchitectureApplication.java
│       │       └── config/
│       │           └── ControllerConfiguration.java
│       └── resources/
│           ├── application.properties
│           └── application-dev.properties
└── target/
```

---

## 🔧 Composants principaux

### ArchitectureApplication.java

Point d'entrée de l'application Spring Boot.

```java
@SpringBootApplication
@EnableAsync
public class ArchitectureApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(ArchitectureApplication.class, args);
    }
}
```

**Annotations** :

- `@SpringBootApplication` : Active l'auto-configuration Spring Boot
- `@EnableAsync` : Active le support des événements asynchrones

---

### ControllerConfiguration.java

Classe de configuration qui **crée les Beans** des services métier et **connecte les Ports aux Adapters**.

```java
@Configuration
public class ControllerConfiguration {
    
    /**
     * Crée le bean du service de gestion des clients avec validation.
     * 
     * Spring injecte automatiquement l'implémentation de ClientStorageProvider
     * (ClientsBddService dans adapters-out-bdd).
     */
    @Bean
    public ClientsServiceValidator clientsServiceValidator(
            ClientStorageProvider clientStorageProvider) {
        return new ClientsServiceValidator(clientStorageProvider);
    }
    
    /**
     * Crée le bean du service de gestion des comptes avec validation.
     * 
     * Spring injecte automatiquement :
     * - ClientStorageProvider (ClientsBddService)
     */
    @Bean
    public ComptesServiceValidator comptesServiceValidator(
            ClientStorageProvider clientStorageProvider) {
        return new ComptesServiceValidator(clientStorageProvider);
    }
}
```

**Rôle** : C'est ici que la **magie de l'architecture hexagonale** opère ! Les services métier du `domain` (qui sont des
POJOs sans Spring) sont instanciés et reçoivent leurs dépendances (Ports) qui sont implémentées par les Adapters.

---

## ⚙️ Configuration

### application.properties

Configuration principale de l'application.

```properties
# Application
spring.application.name=architecture-bancaire

# MongoDB
spring.data.mongodb.host=localhost
spring.data.mongodb.port=27017
spring.data.mongodb.database=architecture

# Événements asynchrones
spring.task.execution.pool.core-size=2
spring.task.execution.pool.max-size=5
spring.task.execution.pool.queue-capacity=500
spring.task.execution.thread-name-prefix=async-

# Logging
logging.level.com.wgt.imt.architecture=DEBUG
logging.pattern.console=%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n
```

### application-dev.properties

Configuration pour l'environnement de développement (optionnel).

---

## 📦 Dépendances Maven

Ce module **agrège TOUS les autres modules** du projet.

```xml
<dependencies>
    <!-- Domain : le cœur métier -->
    <dependency>
        <groupId>com.wgt.imt</groupId>
        <artifactId>architecture-domain</artifactId>
        <version>${project.version}</version>
    </dependency>
    
    <!-- Adapters IN : points d'entrée -->
    <dependency>
        <groupId>com.wgt.imt</groupId>
        <artifactId>architecture-adapters-in-rest</artifactId>
        <version>${project.version}</version>
    </dependency>
    <dependency>
        <groupId>com.wgt.imt</groupId>
        <artifactId>architecture-adapters-in-listeners</artifactId>
        <version>${project.version}</version>
    </dependency>
    
    <!-- Adapters OUT : points de sortie -->
    <dependency>
        <groupId>com.wgt.imt</groupId>
        <artifactId>architecture-adapters-out-bdd</artifactId>
        <version>${project.version}</version>
    </dependency>
    <dependency>
        <groupId>com.wgt.imt</groupId>
        <artifactId>architecture-adapters-out-writers</artifactId>
        <version>${project.version}</version>
    </dependency>
    
    <!-- Spring Boot Starter -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter</artifactId>
    </dependency>
    
    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

---

## 🔌 Injection de dépendances

### Comment ça fonctionne ?

L'injection de dépendances Spring connecte automatiquement les Ports (interfaces) aux Adapters (implémentations) :

1. **Domain** : Définit les Ports (interfaces)
   ```java
   public interface ClientStorageProvider { ... }
   ```

2. **Adapter OUT** : Implémente le Port
   ```java
   @Service
   public class ClientsBddService implements ClientStorageProvider { ... }
   ```

3. **Application** : Crée le Bean du service métier
   ```java
   @Bean
   public ClientsServiceValidator clientsServiceValidator(
           ClientStorageProvider clientStorageProvider) {  // ← Spring injecte ClientsBddService
       return new ClientsServiceValidator(clientStorageProvider);
   }
   ```

4. **Adapter IN** : Utilise le service métier
   ```java
   @RestController
   public class ClientsController {
       private final ClientsServiceValidator clientsService;  // ← Spring injecte le Bean
   }
   ```

**Résultat** : Le domain ne connaît ni Spring, ni MongoDB, ni les contrôleurs REST. Il reste pur et testable !

---

## 🚀 Démarrage de l'application

### Prérequis

- **Java 17+**
- **Maven 3.8+**
- **MongoDB 4.4+** (en local ou Docker)

### Démarrer MongoDB

```bash
# Via Docker
docker run -d -p 27017:27017 --name mongodb mongo:latest

# Ou MongoDB local
mongod --dbpath /path/to/data
```

### Lancer l'application

```bash
# Depuis la racine du projet
mvn clean install
mvn spring-boot:run -pl application

# Ou avec Maven Wrapper
./mvnw spring-boot:run -pl application
```

### Vérifier le démarrage

```bash
# Healthcheck
curl http://localhost:8080/actuator/health

# Test de l'API
curl http://localhost:8080/clients
```

L'application démarre sur **http://localhost:8080**

---

## ⚠️ Règles architecturales

### ✅ Ce module DOIT

- ✅ Agréger TOUS les modules du projet
- ✅ Contenir la classe `@SpringBootApplication`
- ✅ Créer les Beans des services métier via `@Configuration`
- ✅ Laisser Spring injecter les implémentations des Ports
- ✅ Contenir les fichiers de configuration (`application.properties`)

### ❌ Ce module NE DOIT PAS

- ❌ Contenir de la logique métier (dans le `domain`)
- ❌ Contenir des contrôleurs REST (dans `adapters-in-rest`)
- ❌ Contenir des repositories (dans `adapters-out-bdd`)
- ❌ Contenir des validateurs métier (dans le `domain`)

---

## 🔗 Intégration avec les autres modules

### Dépend de

- **domain** : Services métier à instancier
- **adapters-in-rest** : Contrôleurs REST à exposer
- **adapters-in-listeners** : Listeners d'événements à enregistrer
- **adapters-out-bdd** : Implémentation de la persistance
- **adapters-out-writers** : Implémentation de la publication d'événements

### Utilisé par

- Aucun module (c'est le point d'entrée final)

### Flux d'assemblage

```
1. Spring démarre ArchitectureApplication
2. Scan des composants (@Service, @RestController, @Repository)
3. ControllerConfiguration crée les Beans des services métier
4. Spring injecte les dépendances (Ports → Adapters)
5. Application prête à recevoir des requêtes HTTP
```

---

## 🧪 Tests

### Tests d'intégration

Ce module est idéal pour les **tests d'intégration** qui testent l'application complète :

```java
@SpringBootTest
@AutoConfigureMockMvc
class ClientsIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void createClient_shouldReturn201() throws Exception {
        mockMvc.perform(post("/clients")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "nom": "Dupont",
                        "prenom": "Jean",
                        "genre": "HOMME"
                    }
                    """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nom").value("Dupont"));
    }
}
```

---

## 📊 Packaging

### Construction du JAR exécutable

```bash
mvn clean package -pl application
```

Génère : `application/target/architecture-application-1.0.0.jar`

### Exécution du JAR

```bash
java -jar application/target/architecture-application-1.0.0.jar
```

### Avec profil

```bash
java -jar application.jar --spring.profiles.active=dev
```

---

## 📚 Documentation

Pour plus d'informations, consulter le wiki :

- **[[Installation]]** : Guide d'installation complet
- **[[Architecture]]** : Vue d'ensemble de l'architecture hexagonale
- **[[API-REST]]** : Documentation des endpoints
- **[[Technologies]]** : Stack technique utilisée

---

## 💡 Bonnes pratiques

### Configuration des Beans

✅ **BON** : Créer explicitement les Beans des services métier

```java
@Bean
public ClientsServiceValidator clientsServiceValidator(
        ClientStorageProvider storage) {
    return new ClientsServiceValidator(storage);
}
```

❌ **MAUVAIS** : Annoter les services métier avec `@Service`

```java
// ❌ NE PAS FAIRE : le domain doit rester framework-agnostic
@Service  // NON !
public class ClientsService { ... }
```

### Séparation des configurations

Pour les projets plus complexes, séparer les configurations par domaine :

```
config/
├── ControllerConfiguration.java    ← Beans des services
├── DatabaseConfiguration.java      ← Config MongoDB
├── EventConfiguration.java         ← Config événements
└── SecurityConfiguration.java      ← Config sécurité
```

---

## 🎓 Pédagogie : Comprendre le rôle du module application

### Question : Pourquoi ne pas annoter directement les services métier avec @Service ?

**Réponse** : Pour garder le **domain framework-agnostic** !

- ✅ Le domain ne doit dépendre d'AUCUN framework (pas de Spring)
- ✅ Les services métier sont de simples POJOs avec des constructeurs
- ✅ Le module `application` fait le "pont" entre le domain pur et Spring
- ✅ Cela permet de tester le domain sans Spring et de changer de framework facilement

### Question : Que se passe-t-il si on supprime ControllerConfiguration ?

**Réponse** : Spring ne trouvera pas les implémentations des services métier !

- ❌ Les contrôleurs REST ne pourront pas injecter `ClientsServiceValidator`
- ❌ Erreur au démarrage : "No qualifying bean of type ClientsServiceValidator"
- ✅ `ControllerConfiguration` est INDISPENSABLE pour créer les Beans

---

**Module créé dans le cadre du cours d'architecture logicielle - IMT**
