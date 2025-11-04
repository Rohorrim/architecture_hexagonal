# 💾 Module Adapters-Out-BDD - Persistance MongoDB

Adaptateur **secondaire** (driven adapter) implémentant la persistance des données avec MongoDB.

---

## 📋 Responsabilité

Ce module **implémente les Ports de persistance** définis dans le domain en utilisant MongoDB et Spring Data.

**Principe** : Convertit les modèles métier en entités MongoDB et vice-versa.

---

## 🎯 Caractéristiques

- ✅ **Implémente `ClientStorageProvider`** (Port défini dans domain)
- ✅ **Spring Data MongoDB** pour l'accès aux données
- ✅ **Mappers** : Entity ↔ Model métier
- ✅ **Repository pattern** avec `MongoRepository`
- ✅ **Isolation complète** : Le domain ne connaît pas MongoDB

---

## 📦 Structure

```
adapters-out-bdd/
└── src/main/java/
    └── com.wgt.imt.architecture.bdd/
        ├── clients/
        │   ├── ClientsBddService.java          # ⚙️ Implémente ClientStorageProvider
        │   └── repositories/
        │       ├── ClientRepository.java       # 📚 Repository Spring Data
        │       ├── entities/
        │       │   ├── ClientEntity.java       # 📄 Entité MongoDB Client
        │       │   └── CompteEntity.java       # 📄 Entité MongoDB Compte (imbriquée)
        │       └── mappers/
        │           ├── ClientBddMapper.java    # 🔄 Mapper Client
        │           └── CompteBddMapper.java    # 🔄 Mapper Compte
        │
        └── common/
            └── model/
                └── mappers/
                    └── AbstractBddMapper.java  # 🔄 Mapper abstrait
```

---

## ⚙️ ClientsBddService

Implémente le Port `ClientStorageProvider` défini dans le domain.

```java

@Service
@AllArgsConstructor
public class ClientsBddService implements ClientStorageProvider {

    private ClientRepository repository;
    private ClientBddMapper mapper;

    @Override
    public boolean exist(UUID identifier) {
        return Optional.ofNullable(identifier)
                .map(UUID::toString)
                .map(this.repository::existsById)
                .orElse(false);
    }

    @Override
    public Collection<Client> getAll() {
        return this.repository.findAll()
                .stream()
                .map(this.mapper::from)
                .collect(Collectors.toSet());
    }

    @Override
    public Optional<Client> get(UUID identifier) {
        return Optional.ofNullable(identifier)
                .map(UUID::toString)
                .flatMap(this.repository::findById)
                .map(this.mapper::from);
    }

    @Override
    public Client save(Client client) {
        Objects.requireNonNull(client, "Impossible de sauvegarder un client nul");
        return this.mapper.from(
                this.repository.save(
                        this.mapper.to(client)
                )
        );
    }

    @Override
    public void delete(UUID identifier) {
        Optional.ofNullable(identifier)
                .map(UUID::toString)
                .ifPresent(this.repository::deleteById);
    }
}
```

---

## 📚 Repository Spring Data

```java
public interface ClientRepository extends MongoRepository<ClientEntity, String> {
    // Méthodes héritées :
    // - findAll()
    // - findById(String id)
    // - save(ClientEntity entity)
    // - deleteById(String id)
    // - existsById(String id)

    // Requêtes personnalisées possibles :
    Optional<ClientEntity> findByLastname(String lastname);

    List<ClientEntity> findByGenre(String genre);
}
```

**Spring Data** génère automatiquement l'implémentation !

---

## 📄 Entités MongoDB

### ClientEntity

```java
@Document(collection = "clients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientEntity {
    
    @Id
    private String id;  // UUID stocké en String
    
    private String lastname;
    private String firstname;
    private String genre;
    
    // Comptes imbriqués dans le document
    private Set<CompteEntity> comptes;
}
```

### CompteEntity

```java
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompteEntity {
    
    private String identifier;  // UUID
    private Double solde;
    private String typeCompte;  // Enum stocké en String
}
```

### Structure MongoDB

```json
{
  "_id": "123e4567-e89b-12d3-a456-426614174000",
  "lastname": "Dupont",
  "firstname": "Jean",
  "genre": "HOMME",
  "comptes": [
    {
      "identifier": "456e7890-e89b-12d3-a456-426614174111",
      "solde": 1000.50,
      "typeCompte": "COMPTE_COURANT"
    },
    {
      "identifier": "789e0123-e89b-12d3-a456-426614174222",
      "solde": 5000.00,
      "typeCompte": "LIVRET_A"
    }
  ]
}
```

**Choix de conception** :

- ✅ **Comptes imbriqués** : Un client a toujours ses comptes
- ✅ **Dénormalisation** : Performances de lecture optimisées
- ⚠️ Limite MongoDB : 16 Mo par document (largement suffisant ici)

---

## 🔄 Mappers

### ClientBddMapper

Convertit `Client` (domain) ↔ `ClientEntity` (BDD).

```java
@Component
@AllArgsConstructor
public class ClientBddMapper extends AbstractBddMapper<Client, ClientEntity> {
    
    private CompteBddMapper compteMapper;
    
    /**
     * Convertit ClientEntity → Client (domain)
     */
    @Override
    public Client from(ClientEntity entity) {
        if (entity == null) return null;
        
        return Client.builder()
                .identifier(UUID.fromString(entity.getId()))
                .lastname(entity.getLastname())
                .firstname(entity.getFirstname())
                .genre(GenreEnum.valueOf(entity.getGenre()))
                .comptes(
                    Optional.ofNullable(entity.getComptes())
                        .orElse(Collections.emptySet())
                        .stream()
                        .map(compteMapper::from)
                        .collect(Collectors.toSet())
                )
                .build();
    }
    
    /**
     * Convertit Client (domain) → ClientEntity
     */
    @Override
    public ClientEntity to(Client model) {
        if (model == null) return null;
        
        return ClientEntity.builder()
                .id(model.getIdentifier().toString())
                .lastname(model.getLastname())
                .firstname(model.getFirstname())
                .genre(model.getGenre().name())
                .comptes(
                    Optional.ofNullable(model.getComptes())
                        .orElse(Collections.emptySet())
                        .stream()
                        .map(compteMapper::to)
                        .collect(Collectors.toSet())
                )
                .build();
    }
}
```

### CompteBddMapper

```java
@Component
public class CompteBddMapper extends AbstractBddMapper<Compte, CompteEntity> {
    
    @Override
    public Compte from(CompteEntity entity) {
        return Compte.builder()
                .identifier(UUID.fromString(entity.getIdentifier()))
                .solde(entity.getSolde())
                .typeCompte(TypeCompteEnum.valueOf(entity.getTypeCompte()))
                .build();
    }
    
    @Override
    public CompteEntity to(Compte model) {
        return CompteEntity.builder()
                .identifier(model.getIdentifier().toString())
                .solde(model.getSolde())
                .typeCompte(model.getTypeCompte().name())
                .build();
    }
}
```

**Responsabilités des Mappers** :

- ✅ Conversion des types (`UUID` ↔ `String`, `Enum` ↔ `String`)
- ✅ Gestion des collections
- ✅ Gestion des valeurs nulles
- ✅ Isolation complète entre domain et BDD

---

## 📦 Dépendances Maven

```xml
<dependencies>
    <!-- Domain (Ports à implémenter) -->
    <dependency>
        <groupId>com.wgt.imt</groupId>
        <artifactId>architecture-domain</artifactId>
    </dependency>
    
    <!-- Spring Data MongoDB -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-mongodb</artifactId>
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

## ⚙️ Configuration MongoDB

### application.properties

```properties
# MongoDB URI
spring.data.mongodb.uri=mongodb://localhost:27017/architecture
spring.data.mongodb.database=architecture

# Optionnel : logs des requêtes MongoDB
logging.level.org.springframework.data.mongodb.core.MongoTemplate=DEBUG
```

### Avec authentification

```properties
spring.data.mongodb.uri=mongodb://username:password@localhost:27017/architecture?authSource=admin
```

### Avec MongoDB Atlas (cloud)

```properties
spring.data.mongodb.uri=mongodb+srv://username:password@cluster.mongodb.net/architecture?retryWrites=true&w=majority
```

---

## 🧪 Tests

### Test du Service BDD

```java

@DataMongoTest
class ClientsBddServiceTest {

    @Autowired
    private ClientRepository repository;

    @Autowired
    private MongoTemplate mongoTemplate;

    private ClientsBddService service;
    private ClientBddMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ClientBddMapper(new CompteBddMapper());
        service = new ClientsBddService(repository, mapper);
    }

    @Test
    void shouldSaveAndRetrieveClient() {
        // Arrange
        Client client = Client.builder()
                .identifier(UUID.randomUUID())
                .lastname("Dupont")
                .firstname("Jean")
                .genre(GenreEnum.HOMME)
                .build();

        // Act
        Client saved = service.save(client);
        Optional<Client> retrieved = service.get(saved.getIdentifier());

        // Assert
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getLastname()).isEqualTo("Dupont");
    }
}
```

---

## 🔧 Commandes MongoDB Utiles

### Se connecter à la base

```bash
mongosh
use architecture
```

### Voir les collections

```javascript
show collections
```

### Voir tous les clients

```javascript
db.clients.find().pretty()
```

### Requêtes

```javascript
// Trouver par nom
db.clients.find({ lastname: "Dupont" })

// Compter les clients
db.clients.countDocuments()

// Clients ayant au moins un compte
db.clients.find({ "comptes.0": { $exists: true } })

// Supprimer tous les clients
db.clients.deleteMany({})
```

---

## ✅ Règles à Respecter

### ✅ AUTORISÉ

- ✅ Annoter les entités avec `@Document`
- ✅ Utiliser `MongoRepository`
- ✅ Mapper Entity ↔ Model métier
- ✅ Gérer les types MongoDB (String pour UUID, etc.)

### ❌ INTERDIT

- ❌ Exposer les entités MongoDB au domain
- ❌ Utiliser les annotations MongoDB dans le domain
- ❌ Logique métier dans les mappers ou repositories

---

## 🎯 Architecture Hexagonale

```
Domain (Port)               Adapter-Out-BDD
     │
     │ ClientStorageProvider
     │ (interface)
     │
     ├──────────────────────► ClientsBddService
                              (implémentation)
                                    │
                                    ├─► ClientRepository
                                    │   (Spring Data)
                                    │
                                    └─► ClientBddMapper
                                        (conversion)
                                             │
                                             ▼
                                        MongoDB
```

**Le domain ne sait pas qu'on utilise MongoDB !**

---

## 📖 Voir Aussi

- [Architecture Hexagonale](../../architecture.wiki/Architecture.md)
- [Modèles de Données](../../architecture.wiki/Modeles-Donnees.md)
- [Domain Module](../domain/README.md)

---

**Module Adapters-Out-BDD - Architecture Hexagonale**  
_Adaptateur secondaire MongoDB_

