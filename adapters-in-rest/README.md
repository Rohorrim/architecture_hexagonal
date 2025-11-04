# 🔌 Module Adapters-In-Rest - API REST

Adaptateur **primaire** (driving adapter) exposant l'application via une API REST.

---

## 📋 Responsabilité

Ce module expose les **endpoints HTTP REST** permettant aux clients externes (Postman, applications front-end, etc.)
d'interagir avec l'application.

**Principe** : Point d'entrée HTTP → Appelle le domain → Retourne les réponses

---

## 🎯 Caractéristiques

- ✅ **Contrôleurs REST** Spring MVC
- ✅ **DTOs Input/Output** : Isolation du modèle métier
- ✅ **Gestion des erreurs HTTP** : `@ControllerAdvice`
- ✅ **Validation des requêtes** : Jakarta Bean Validation
- ⚠️ **Dépend UNIQUEMENT du domain** (pas des adapters-out)

---

## 📦 Structure

```
adapters-in-rest/
└── src/main/java/
    └── com.wgt.imt.architecture.rest/
        ├── clients/
        │   ├── ClientsController.java       # 🎮 Contrôleur REST Clients
        │   └── model/
        │       ├── input/
        │       │   ├── ClientInput.java
        │       │   └── ClientUpdateInput.java
        │       └── output/
        │           └── ClientOutput.java
        │
        ├── comptes/
        │   ├── ComptesController.java       # 🎮 Contrôleur REST Comptes
        │   └── model/
        │       ├── input/
        │       │   ├── CompteInput.java
        │       │   └── CompteUpdateInput.java
        │       └── output/
        │           └── ComptesOutput.java
        │
        └── common/
            ├── ControllerExceptionHandler.java  # 🚨 Gestion globale des erreurs
            └── model/
                ├── input/
                │   ├── AbstractInput.java
                │   ├── AbstractUpdateInput.java
                │   └── UpdatableProperty.java
                └── output/
                    ├── AbstractOutput.java
                    └── ExceptionOutput.java
```

---

## 🎮 Contrôleurs REST

### ClientsController

**Base URL** : `/api/imt/v1/clients`

```java
@RestController
@AllArgsConstructor
@RequestMapping("api/imt/v1/clients")
public class ClientsController {
    private final ClientsServiceValidator service;
    
    @GetMapping
    public Collection<ClientOutput> getAll() { ... }
    
    @PostMapping
    public ClientOutput create(@RequestBody ClientInput client) { ... }
    
    @GetMapping("/{idClient}")
    public ClientOutput getOne(@PathVariable String identifier) { ... }
    
    @PatchMapping("/{idClient}")
    public void update(@PathVariable String identifier, 
                      @RequestBody ClientUpdateInput client) { ... }
    
    @DeleteMapping("/{idClient}")
    public void delete(@PathVariable String identifier) { ... }
}
```

| Méthode  | Endpoint                   | Description            | Code Retour    |
|----------|----------------------------|------------------------|----------------|
| `GET`    | `/api/imt/v1/clients`      | Liste tous les clients | 200 OK         |
| `POST`   | `/api/imt/v1/clients`      | Crée un client         | 201 CREATED    |
| `GET`    | `/api/imt/v1/clients/{id}` | Récupère un client     | 200 OK         |
| `PATCH`  | `/api/imt/v1/clients/{id}` | Met à jour un client   | 204 NO CONTENT |
| `DELETE` | `/api/imt/v1/clients/{id}` | Supprime un client     | 204 NO CONTENT |

### ComptesController

**Base URL** : `/api/imt/v1/clients/{idClient}/comptes`

| Méthode  | Endpoint                                      | Description          | Code Retour    |
|----------|-----------------------------------------------|----------------------|----------------|
| `GET`    | `/api/imt/v1/clients/{idClient}/comptes`      | Liste les comptes    | 200 OK         |
| `POST`   | `/api/imt/v1/clients/{idClient}/comptes`      | Crée un compte       | 201 CREATED    |
| `GET`    | `/api/imt/v1/clients/{idClient}/comptes/{id}` | Récupère un compte   | 200 OK         |
| `PATCH`  | `/api/imt/v1/clients/{idClient}/comptes/{id}` | Met à jour un compte | 204 NO CONTENT |
| `DELETE` | `/api/imt/v1/clients/{idClient}/comptes/{id}` | Supprime un compte   | 204 NO CONTENT |

---

## 📝 DTOs (Data Transfer Objects)

### Pattern Input/Output

Les DTOs isolent le modèle métier des requêtes/réponses HTTP.

#### ClientInput (Création)

```java
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClientInput extends AbstractInput {
    @NotBlank
    @Size(min = 2, max = 50)
    private String lastname;
    
    @NotBlank
    @Size(min = 2, max = 50)
    private String firstname;
    
    @NotNull
    private GenreEnum genre;
    
    // Conversion vers modèle métier
    public static Client convert(ClientInput input) {
        return Client.builder()
            .identifier(UUID.randomUUID())
            .lastname(input.getLastname())
            .firstname(input.getFirstname())
            .genre(input.getGenre())
            .build();
    }
}
```

#### ClientUpdateInput (Mise à jour partielle)

```java
public class ClientUpdateInput extends AbstractUpdateInput {
    private UpdatableProperty<String> lastname;
    private UpdatableProperty<String> firstname;
    private UpdatableProperty<GenreEnum> genre;
    
    // Applique les modifications au client existant
    public static Client from(ClientUpdateInput input, Client existing) {
        return existing.toBuilder()
            .lastname(input.getLastname().orElse(existing.getLastname()))
            .firstname(input.getFirstname().orElse(existing.getFirstname()))
            .genre(input.getGenre().orElse(existing.getGenre()))
            .build();
    }
}
```

**UpdatableProperty** permet de différencier :

- Champ absent de la requête → garder la valeur existante
- Champ présent avec valeur → mettre à jour
- Champ présent avec `null` → mettre à null

#### ClientOutput (Réponse)

```java
@Getter
@Setter
public class ClientOutput extends AbstractOutput {
    private String lastname;
    private String firstname;
    private GenreEnum genre;
    
    // Conversion depuis modèle métier
    public static ClientOutput from(Client client) {
        ClientOutput output = new ClientOutput();
        output.setIdentifier(client.getIdentifier());
        output.setLastname(client.getLastname());
        output.setFirstname(client.getFirstname());
        output.setGenre(client.getGenre());
        return output;
    }
}
```

---

## 🚨 Gestion des Erreurs

### ControllerExceptionHandler

Intercepte les exceptions et retourne des réponses HTTP appropriées.

```java
@ControllerAdvice
public class ControllerExceptionHandler {
    
    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ExceptionOutput handleNotFoundException(NotFoundException ex) {
        return new ExceptionOutput(ex.getMessage());
    }
    
    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionOutput handleBadRequestException(BadRequestException ex) {
        return new ExceptionOutput(ex.getMessage());
    }
    
    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ExceptionOutput handleConflictException(ConflictException ex) {
        return new ExceptionOutput(ex.getMessage());
    }
}
```

### Codes HTTP Retournés

| Exception Métier      | Code HTTP                 | Description           |
|-----------------------|---------------------------|-----------------------|
| `NotFoundException`   | 404 NOT FOUND             | Ressource non trouvée |
| `BadRequestException` | 400 BAD REQUEST           | Validation échouée    |
| `ConflictException`   | 409 CONFLICT              | Client déjà existant  |
| `Exception`           | 500 INTERNAL SERVER ERROR | Erreur inattendue     |

---

## 📦 Dépendances Maven

```xml
<dependencies>
    <!-- Domain (UNIQUEMENT) -->
    <dependency>
        <groupId>com.wgt.imt</groupId>
        <artifactId>architecture-domain</artifactId>
    </dependency>
    
    <!-- Spring Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- Spring Validation -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    
    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

**⚠️ Important** : Ce module ne dépend PAS de `adapters-out-bdd` ou `adapters-out-writers` !

---

## 🧪 Exemples de Requêtes

### Créer un client

```bash
curl -X POST http://localhost:8080/api/imt/v1/clients \
  -H "Content-Type: application/json" \
  -d '{
    "lastname": "Dupont",
    "firstname": "Jean",
    "genre": "HOMME"
  }'
```

**Réponse** :

```json
{
  "identifier": "123e4567-e89b-12d3-a456-426614174000",
  "lastname": "Dupont",
  "firstname": "Jean",
  "genre": "HOMME"
}
```

### Mettre à jour partiellement

```bash
curl -X PATCH http://localhost:8080/api/imt/v1/clients/{id} \
  -H "Content-Type: application/json" \
  -d '{
    "lastname": "Martin"
  }'
```

**Note** : Seul le `lastname` sera modifié.

### Créer un compte

```bash
curl -X POST http://localhost:8080/api/imt/v1/clients/{idClient}/comptes \
  -H "Content-Type: application/json" \
  -d '{
    "solde": 1000.50,
    "typeCompte": "COMPTE_COURANT"
  }'
```

---

## ✅ Règles à Respecter

### ✅ AUTORISÉ

- ✅ Annotations Spring MVC (`@RestController`, `@GetMapping`, etc.)
- ✅ Dépendre du domain
- ✅ DTOs spécifiques pour HTTP
- ✅ Validation des DTOs avec Jakarta Validation

### ❌ INTERDIT

- ❌ Dépendre de `adapters-out-bdd` ou `adapters-out-writers`
- ❌ Injecter `ClientsBddService` directement
- ❌ Logique métier dans les contrôleurs
- ❌ Exposer directement les modèles métier (`Client`, `Compte`)

---

## 🎯 Architecture Hexagonale

```
HTTP Request
     │
     ▼
┌─────────────────┐
│ ClientsController│  ← Ce module
└────────┬────────┘
         │ injecte ClientsServiceValidator
         ▼
    ┌─────────┐
    │ DOMAIN  │  ← Service métier
    └─────────┘
```

Le contrôleur :

1. Reçoit la requête HTTP
2. Convertit DTO → Model métier
3. Appelle le service métier (domain)
4. Convertit Model métier → DTO
5. Retourne la réponse HTTP

---

## 📖 Voir Aussi

- [API REST Documentation](../../architecture.wiki/API-REST.md)
- [Architecture Hexagonale](../../architecture.wiki/Architecture.md)
- [Domain Module](../domain/README.md)

---

**Module Adapters-In-Rest - Architecture Hexagonale**  
_Adaptateur primaire HTTP REST_

