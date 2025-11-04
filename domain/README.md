# 🎯 Module Domain - Cœur Métier

Module central de l'architecture hexagonale contenant **toute la logique métier** de l'application bancaire.

---

## 📋 Responsabilité

Ce module contient le **cœur métier** de l'application, complètement isolé des frameworks et technologies externes.

**Principe clé** : Le domain ne dépend d'aucun framework Spring ni d'aucun adaptateur.

---

## 🎯 Caractéristiques

- ✅ **Framework-agnostic** : Aucune dépendance ou annotation Spring
- ✅ **Logique métier pure** : Services, modèles, validateurs
- ✅ **Ports (interfaces)** : Définit ce dont le métier a besoin
- ✅ **Exceptions métier** : Gestion d'erreurs métier
- ✅ **Testable sans infrastructure** : Tests unitaires purs

---

## 📦 Structure

```
domain/
└── src/main/java/
    └── com.wgt.imt.architecture.business/
        ├── clients/                          # Domaine Clients
        │   ├── model/
        │   │   └── Client.java              # Modèle métier Client
        │   ├── validators/
        │   │   ├── ClientAlreadyExistValidatorStep.java
        │   │   └── ClientGenreValidatorStep.java
        │   ├── ClientsService.java          # Service métier
        │   ├── ClientsServiceValidator.java # Service avec validation
        │   └── ClientStorageProvider.java   # 🔌 PORT (interface)
        │
        ├── comptes/                          # Domaine Comptes
        │   ├── model/
        │   │   ├── Compte.java
        │   │   └── Mouvement.java
        │   ├── annotations/
        │   │   └── MouvementCompteObserver.java
        │   ├── events/
        │   │   └── MouvementCompteEvent.java
        │   ├── validators/
        │   │   ├── CompteAlreadyExistValidatorStep.java
        │   │   └── CompteTypeValidatorStep.java
        │   ├── ComptesService.java
        │   └── ComptesServiceValidator.java
        │
        └── common/                           # Éléments communs
            ├── exception/
            │   ├── ImtException.java
            │   ├── NotFoundException.java
            │   ├── BadRequestException.java
            │   └── ConflictException.java
            ├── model/
            │   ├── GenreEnum.java           # M/F/AUTRE
            │   ├── TypeCompteEnum.java      # COURANT/LIVRET_A/LDDS
            │   └── ValidatorResult.java
            ├── validators/
            │   ├── AbstractValidatorStep.java
            │   └── ConstraintValidatorStep.java
            └── utils/
                └── CollectionImtUtils.java
```

---

## 🔌 Les Ports (Interfaces)

Les **Ports** sont des interfaces définies par le métier. Les adapters les implémentent.

### ClientStorageProvider

Interface pour la persistance des clients.

```java
public interface ClientStorageProvider {
    boolean exist(UUID identifier);

    Collection<Client> getAll();

    Optional<Client> get(UUID identifier);

    Client save(Client client);

    void delete(UUID identifier);
}
```

**Implémenté par** : `adapters-out-bdd/ClientsBddService`

### Événements de mouvement

`MouvementCompteObserver` déclare le message, le type et les expressions SpEL utilisées
par l'adaptateur de publication. Cette annotation reste indépendante de Spring.

`MouvementCompteEvent` est le contrat métier partagé par les adaptateurs de publication
et d'écoute. Le modèle `Compte` conserve son historique de `Mouvement`.

---

## 📚 Modèles Métier

### Client

```java
@Builder(toBuilder = true)
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Client {
    @NotNull
    private UUID identifier;
    
    @NotBlank
    @Size(min = 2, max = 50)
    private String lastname;
    
    @NotBlank
    @Size(min = 2, max = 50)
    private String firstname;
    
    @NotNull
    private GenreEnum genre;
    
    private Set<Compte> comptes;
}
```

### Compte

```java
@Builder(toBuilder = true)
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Compte {
    @NotNull
    private UUID identifier;
    
    @NotNull
    private Double solde;
    
    @NotNull
    private TypeCompteEnum typeCompte;
}
```

---

## ⛓️ Pattern Chain of Responsibility

Les validateurs utilisent le pattern **Chain of Responsibility** pour valider les objets métier.

### Exemple d'utilisation

```java
public Client create(final Client newClient) throws ImtException {
    new ConstraintValidatorStep<Client>()
        .linkWith(new ClientAlreadyExistValidatorStep(this.service))
        .linkWith(new ClientGenreValidatorStep())
        .validate(newClient)
        .throwIfInvalid();

    return super.create(newClient);
}
```

### Validateurs disponibles

- **ConstraintValidatorStep** : Valide les annotations Jakarta Validation
- **ClientAlreadyExistValidatorStep** : Vérifie que le client n'existe pas déjà
- **ClientGenreValidatorStep** : Valide le genre du client
- **CompteTypeValidatorStep** : Valide le type de compte

---

## 🧪 Tests Unitaires

Le domain peut être testé **sans Spring**, **sans MongoDB**, **sans HTTP** !

```java
class ClientsServiceTest {

    @Test
    void shouldCreateClient() {
        // Arrange
        ClientStorageProvider mockStorage = mock(ClientStorageProvider.class);
        when(mockStorage.save(any())).thenReturn(new Client(...));

        ClientsService service = new ClientsService(mockStorage);
        Client newClient = Client.builder()
                .identifier(UUID.randomUUID())
                .lastname("Dupont")
                .firstname("Jean")
                .genre(GenreEnum.HOMME)
                .build();

        // Act
        Client created = service.create(newClient);

        // Assert
        assertNotNull(created);
        verify(mockStorage).save(any());
    }
}
```

**Avantages** :

- ✅ Pas besoin de `@SpringBootTest`
- ✅ Pas besoin de base de données
- ✅ Tests ultra-rapides
- ✅ Isolation complète

---

## 📦 Dépendances Maven

```xml
<dependencies>
    <!-- Validation Jakarta -->
    <dependency>
        <groupId>jakarta.validation</groupId>
        <artifactId>jakarta.validation-api</artifactId>
    </dependency>
    
    <!-- Lombok (génération code) -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

**⚠️ Aucune dépendance Spring !**

---

## ✅ Règles à Respecter

### ✅ AUTORISÉ

- ✅ POJO simples
- ✅ Interfaces (Ports)
- ✅ Jakarta Validation API (`@NotNull`, `@Size`, etc.)
- ✅ Lombok (`@Builder`, `@Getter`, etc.)
- ✅ Java standard library

### ❌ INTERDIT

- ❌ Annotations Spring (`@Service`, `@Component`, `@Repository`)
- ❌ Dépendances Spring dans le pom.xml
- ❌ Dépendances MongoDB, JPA, HTTP
- ❌ Connaître les détails techniques des adapters

---

## 🎯 Principes SOLID Appliqués

### Single Responsibility (S)

Chaque classe a une responsabilité unique :

- `ClientsService` : Opérations CRUD sur les clients
- `ClientsServiceValidator` : Validation métier
- `ClientStorageProvider` : Interface de persistance

### Dependency Inversion (D)

Le domain définit les abstractions (Ports), les adapters les implémentent.

```java
// Domain définit l'interface
public interface ClientStorageProvider { ... }

// Adapter l'implémente (adapters-out-bdd)
@Service
public class ClientsBddService implements ClientStorageProvider { ... }
```

---

## 📖 Voir Aussi

- [Architecture Hexagonale](../../architecture.wiki/Architecture.md)
- [Règles d'Architecture](../../architecture.wiki/Regles-Architecture.md)
- [Design Patterns](../../architecture.wiki/Design-Patterns.md)
- [Validation](../../architecture.wiki/Validation.md)

---

**Module Domain - Architecture Hexagonale**  
_Cœur métier framework-agnostic_
