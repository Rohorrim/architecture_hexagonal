# 🤖 Module Adapters-In-MCP — Model Context Protocol

Adaptateur **primaire** (driving adapter) exposant l'application via le protocole **MCP (Model Context Protocol)**.
Ce module permet à un **agent conversationnel (LLM)** de découvrir et d'utiliser les capacités de l'application
bancaire.

---

## 📋 Responsabilité

Ce module implémente le protocole **MCP via JSON-RPC 2.0 sur HTTP**, exposant les trois primitives fondamentales :

| Primitive     | Rôle                                  | Analogie                      |
|---------------|---------------------------------------|-------------------------------|
| **Tools**     | Actions que le LLM peut déclencher    | Appel de méthode typé         |
| **Resources** | Données de référence en lecture seule | Fichier de configuration      |
| **Prompts**   | Templates de workflow guidant le LLM  | Procédure guidée multi-étapes |

> 💡 **Note** : Le vrai protocole MCP utilise **JSON-RPC 2.0** sur **stdio** ou **HTTP + SSE**.
> Cette implémentation respecte le format **JSON-RPC 2.0** et utilise un **endpoint unique** (`POST /mcp`)
> avec routage par le champ `method`, conformément à la spec officielle.
> Le transport est HTTP classique (pas de stdio ni de SSE) pour rester facilement testable (Postman, curl).

---

## 🌐 Comparaison avec la spec MCP officielle

```
Spec MCP officielle         Ce module (JSON-RPC 2.0 sur HTTP)
───────────────────────     ─────────────────────────────────────────────────────────────
Transport : stdio / SSE  →  Transport : HTTP classique
Format    : JSON-RPC 2.0 →  Format    : JSON-RPC 2.0                             ✅ CONFORME
tools/list               →  POST /imt/v1/mcp  { "method": "tools/list"     }  ✅
tools/call               →  POST /imt/v1/mcp  { "method": "tools/call"     }  ✅
resources/list           →  POST /imt/v1/mcp  { "method": "resources/list" }  ✅
resources/read           →  POST /imt/v1/mcp  { "method": "resources/read" }  ✅
prompts/list             →  POST /imt/v1/mcp  { "method": "prompts/list"   }  ✅
prompts/get              →  POST /imt/v1/mcp  { "method": "prompts/get"    }  ✅
initialize (handshake)   →  Non implémenté (HTTP stateless — pas de session)
```

---

## 🔬 Anatomie comparée — Vrai MCP vs ce projet

Deux notions indépendantes sont à bien distinguer : le **transport** (le tuyau) et le **protocole** (le langage).
Ce projet respecte **entièrement le protocole** (JSON-RPC 2.0), mais simplifie volontairement le **transport** :
HTTP classique à la place de stdio ou HTTP+SSE, pour rester facilement testable.

### Notion 1 — Le transport

```
┌────────────────────────────────────────────────────────────────────┐
│  VRAI MCP — stdio (cas local, le plus courant aujourd'hui)          │
│                                                                     │
│  ┌─────────────────┐    stdin / stdout    ┌───────────────────┐    │
│  │  Client LLM     │ ◄──────────────────► │  Serveur MCP      │    │
│  │  (Claude,       │                      │  (.jar, .py, .js) │    │
│  │   VSCode...)    │  fork() — le LLM     └───────────────────┘    │
│  └─────────────────┘  lance le serveur                             │
│                        comme processus fils                         │
│                                                                     │
│  → Aucun réseau. Même machine. Pas d'authentification nécessaire.  │
└────────────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────────────┐
│  VRAI MCP — HTTP + SSE (cas distant, entreprise)                    │
│                                                                     │
│  Client LLM                         Serveur MCP                    │
│  ┌──────────┐                        ┌───────────────────────┐     │
│  │  Agent   │── POST /tools/call ──► │  Spring Boot          │     │
│  │          │   Bearer {jwt}         │                       │     │
│  │          │◄─ data: morceau 1 ──── │  Réponse streamée     │     │
│  │          │◄─ data: morceau 2 ──── │  token par token      │     │
│  │          │◄─ data: [DONE] ──────  │  (SSE)                │     │
│  └──────────┘                        └───────────────────────┘     │
│                                                                     │
│  → Réseau. OAuth2 obligatoire. Réponse progressive (streaming).    │
└────────────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────────────┐
│  CE PROJET — HTTP + JSON-RPC 2.0 (pédagogique)                      │
│                                                                     │
│  Postman / curl / Agent        Serveur MCP                         │
│  ┌──────────┐                   ┌───────────────────────┐          │
│  │  Client  │── POST /mcp       │  Spring Boot          │          │
│  │          │   Bearer {jwt} ──►│                       │          │
│  │          │◄─ { "result":{} } │  Réponse en un bloc   │          │
│  └──────────┘                   └───────────────────────┘          │
│                                                                     │
│  → Réseau. OAuth2 disponible. Pas de streaming.                    │
│  → Format JSON-RPC 2.0 conforme, endpoint unique POST /mcp.        │
└────────────────────────────────────────────────────────────────────┘
```

---

### Notion 2 — Le protocole (format des messages)

```
┌────────────────────────────────────────────────────────────────────┐
│  VRAI MCP — JSON-RPC 2.0                                            │
│                                                                     │
│  Requête :                       Réponse succès :                  │
│  {                               {                                 │
│    "jsonrpc": "2.0",               "jsonrpc": "2.0",               │
│    "id": 1,                        "id": 1,                        │
│    "method": "tools/call",         "result": {                     │
│    "params": {                       "content": [                  │
│      "name": "GetOneClient",           {"type":"text","text":"..."}│
│      "arguments": {                  ],                            │
│        "identifier": "550e-..."      "isError": false              │
│      }                             }                               │
│    }                             }                                 │
│  }                                                                  │
│                                                                     │
│  → Un seul endpoint POST /. Le champ "method" fait le routing.    │
│  → Le champ "id" corrèle chaque réponse à sa requête.             │
│  → Fonctionne identiquement sur stdio, HTTP ou SSE.               │
└────────────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────────────┐
│  CE PROJET — JSON-RPC 2.0 (conforme à la spec MCP)                  │
│                                                                     │
│  Requête :                       Réponse succès :                  │
│  POST /imt/v1/mcp        {                                 │
│  {                                 "jsonrpc": "2.0",               │
│    "jsonrpc": "2.0",               "id": 1,                        │
│    "id": 1,                        "result": {                     │
│    "method": "tools/call",           ...                           │
│    "params": {                     }                               │
│      "name": "GetOneClient",     }                                 │
│      "arguments": {                                                 │
│        "identifier": "550e-..."  Réponse erreur :                  │
│      }                           {                                 │
│    }                               "jsonrpc": "2.0",               │
│  }                                 "id": 1,                        │
│                                    "error": {                      │
│                                      "code": -32602,               │
│                                      "message": "..."              │
│                                    }                               │
│                                  }                                 │
│                                                                     │
│  → Un seul endpoint POST /mcp. Le champ "method" fait le routing. │
│  → Le champ "id" corrèle chaque réponse à sa requête.             │
│  → HTTP retourne toujours 200 OK, même en cas d'erreur applicative.│
└────────────────────────────────────────────────────────────────────┘
```

---

### Notion 3 — Le handshake `initialize`

```
┌────────────────────────────────────────────────────────────────────┐
│  VRAI MCP — Négociation des capacités avant tout appel              │
│                                                                     │
│  Client                                      Serveur               │
│    │── initialize ──────────────────────────►│                    │
│    │   { protocolVersion: "2024-11-05",       │                    │
│    │     clientInfo: { name: "Claude" } }     │                    │
│    │                                          │                    │
│    │◄── initialize result ────────────────────│                    │
│    │   { capabilities: {                      │                    │
│    │       tools: {}, resources: {},          │                    │
│    │       prompts: {}                        │                    │
│    │     },                                   │                    │
│    │     serverInfo: { name: "BankServer" } } │                    │
│    │                                          │                    │
│    │── initialized (notification) ───────────►│                    │
│    │                                          │                    │
│    │   ← seulement maintenant : tools/list, etc.                   │
│                                                                     │
│  → Le client sait exactement ce que le serveur supporte.          │
│  → Indispensable sur une connexion persistante (stdio / SSE).     │
└────────────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────────────┐
│  CE PROJET — Pas de handshake (HTTP stateless)                      │
│                                                                     │
│  → Chaque requête HTTP est indépendante — pas de session.         │
│  → Le client appelle tools/list directement sans négociation.     │
│  → Les capacités sont connues via la documentation / README.      │
└────────────────────────────────────────────────────────────────────┘
```

---

### Vue synthétique

```
                 Ce qui est IDENTIQUE
        ┌────────────────────────────────────┐
        │  ✅ Les 3 primitives               │
        │     (Tools, Resources, Prompts)    │
        │  ✅ Les noms d'actions             │
        │     (tools/call, resources/read...)│
        │  ✅ Le JSON Schema (inputSchema)   │
        │  ✅ L'architecture des handlers    │
        │  ✅ OAuth2                         │
        └────────────────────────────────────┘

 Ce projet (JSON-RPC 2.0)      Vrai MCP (officiel)
 ──────────────────────        ────────────────────────────
 Transport : HTTP              Transport : stdio ou HTTP+SSE
 1 endpoint POST /mcp  ✅       1 endpoint, routing par method  ✅
 Réponse en 1 bloc              Réponse streamée (SSE)
 Format : JSON-RPC 2.0 ✅       Format : JSON-RPC 2.0           ✅
 Pas de handshake               initialize obligatoire
 Testable : Postman, curl       Testable : MCP Inspector, SDK
```

---

## 📦 Structure du module

```
adapters-in-mcp/
└── src/main/java/
    └── com.wgt.imt.architecture.mcp/
        │
        ├── JsonRpcController.java               # 🎮 Contrôleur unique — endpoint POST /mcp (JSON-RPC 2.0)
        │
        ├── tools/                               # 🔧 Primitive : Tools
        │   ├── ToolHandler.java                 #    Interface générique <I> extends OutputAble<ToolOutput>
        │   ├── clients/
        │   │   ├── GetAllClientToolHandler.java  #    Outil : GetAllClients
        │   │   ├── GetOneClientToolHandler.java  #    Outil : GetOneClient
        │   │   └── input/
        │   │       └── GetOneToolArguments.java  #    Arguments UUID
        │   └── comptes/
        │       ├── GetAllComptesToolHandler.java #    Outil : GetAllComptes
        │       ├── GetOneCompteToolHandler.java  #    Outil : GetOneCompte
        │       └── input/
        │           ├── GetAllComptesToolArguments.java
        │           └── GetOneCompteToolArguments.java
        │
        ├── resources/                           # 📄 Primitive : Resources
        │   ├── ResourceHandler.java             #    Interface extends OutputAble<ResourceOutput>
        │   └── reference/
        │       ├── TypesCompteResourceHandler.java  # reference://types-compte
        │       └── GenresResourceHandler.java       # reference://genres
        │
        ├── prompts/                             # 💬 Primitive : Prompts
        │   ├── PromptHandler.java               #    Interface extends OutputAble<PromptOutput>
        │   └── clients/
        │       └── AnalyseClientPromptHandler.java  # Workflow : analyse-client
        │
        └── common/
            ├── OutputAble.java                  # 🔌 Interface générique — capacité de sérialisation
            ├── ControllerExceptionHandler.java  # 🚨 Gestion globale des erreurs
            ├── model/
            │   ├── exception/
            │   │   ├── InvalidRequestException.java # Requête JSON-RPC invalide (-32600/-32601/-32602)
            │   │   └── McpNotFoundException.java    # Element introuvable (-32601)
            │   ├── input/
            │   │   ├── JsonRpcRequest.java       # Requête unifiée JSON-RPC 2.0 (jsonrpc, id, method, params)
            │   │   └── tools/
            │   │       └── EmptyToolArguments.java  # Arguments vides (outils sans paramètre)
            │   ├── output/
            │   │   ├── JsonRpcResponse.java      # Réponse unifiée JSON-RPC 2.0 (record Java)
            │   │   ├── JsonRpcError.java          # Objet erreur JSON-RPC 2.0 (record Java)
            │   │   ├── tools/
            │   │   │   └── ToolOutput.java       # Descripteur d'outil (tools/list)
            │   │   ├── resources/
            │   │   │   ├── ResourceOutput.java   # Descripteur de resource
            │   │   │   └── ResourceContent.java  # Contenu lu (resources/read)
            │   │   └── prompts/
            │   │       ├── PromptOutput.java     # Descripteur de prompt
            │   │       ├── PromptArgument.java   # Définition d'un argument
            │   │       ├── PromptGetOutput.java  # Prompt rendu
            │   │       └── PromptMessage.java    # Message dans un prompt
            │   └── MethodTypeEnum.java           # Énumération des méthodes JSON-RPC supportées
            ├── validators/                       # ✅ Chaîne de validation (Chain of Responsibility)
            │   ├── JsonVersionValidatorStep.java #    Étape 1 — vérifie jsonrpc == "2.0"
            │   └── MethodValidatorStep.java      #    Étape 2 — vérifie method et params requis
            └── utils/
                ├── JsonSchemaGenerator.java      # Génération inputSchema par réflexion
                └── JsonUtils.java               # Sérialisation/désérialisation Jackson
```

---

## 🔌 Interface `OutputAble<T>` — nouveau contrat transversal

Toutes les interfaces de handlers héritent de `OutputAble<T>`.
Cette interface générique permet au contrôleur de traiter uniformément les trois primitives
lors des méthodes de type liste via **une seule méthode générique**.

```java
public interface OutputAble<T> {
    T toOutput(); // Produit le descripteur de sortie (ToolOutput, ResourceOutput, PromptOutput)
}
```

**Avant** : trois méthodes distinctes dans le contrôleur pour sérialiser chaque type de liste.  
**Après** : une seule méthode générique grâce au polymorphisme :

```java
private static <I extends OutputAble<?>> JsonRpcResponse handleResponseList(
        final JsonRpcRequest request, final Collection<I> output) {
    return JsonRpcResponse.success(request.getId(), Optional.ofNullable(output).stream()
            .flatMap(Collection::stream)
            .map(OutputAble::toOutput)
            .collect(Collectors.toSet()));
}
```

---

## 🔧 Primitive 1 — Tools

Les outils sont des **actions exécutables par le LLM**. Chaque outil implémente l'interface `ToolHandler<I>`.

### Interface ToolHandler

```java
public interface ToolHandler<I> extends OutputAble<ToolOutput> {

    // Méthode par défaut — template method : Map → objet typé → délègue à apply(I)
    default Object apply(Map<String, Object> arguments) throws ImtException;

    // Méthode par défaut — construit le descripteur ToolOutput pour tools/list
    default ToolOutput toOutput();

    String getName();              // Nom exposé dans tools/list (PascalCase)

    String getToolDescription();   // Description lue par le LLM

    Class<I> getInputType();       // Classe des arguments → génère l'inputSchema

    Object apply(I arguments) throws ImtException; // Logique métier à implémenter
}
```

> ⚠️ **Changements par rapport à la version précédente** :
> - `getToolName()` → **`getName()`**
> - `handle(Map<String, Object>)` → **`apply(Map<String, Object>)`**
> - `handle(I arguments, Map<String, String> context)` → **`apply(I arguments)`** (le contexte n'est plus passé)
> - `toOutput()` est désormais une **méthode par défaut** (plus besoin de l'implémenter)

### Outils disponibles

| Nom             | Handler                    | Arguments                                | Service appelé                                      |
|-----------------|----------------------------|------------------------------------------|-----------------------------------------------------|
| `GetAllClients` | `GetAllClientToolHandler`  | Aucun (`EmptyToolArguments`)             | `ClientsService.getAll()`                           |
| `GetOneClient`  | `GetOneClientToolHandler`  | `identifier` (UUID)                      | `ClientsService.getOne(UUID)`                       |
| `GetAllComptes` | `GetAllComptesToolHandler` | `clientIdentifier` (UUID)                | `ComptesService.getAllFilteredByClient(UUID)`       |
| `GetOneCompte`  | `GetOneCompteToolHandler`  | `clientIdentifier` + `identifier` (UUID) | `ComptesService.getOneFilteredByClient(UUID, UUID)` |

### Endpoints

#### POST `/imt/v1/mcp` — `tools/list`

Liste tous les outils avec leur `inputSchema` généré automatiquement par introspection Java.

**Requête :**

```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "method": "tools/list"
}
```

**Exemple de réponse :**

```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "result": [
    {
      "name": "GetOneClient",
      "description": "Permet de récupérer les informations d'un client en particulier",
      "inputSchema": {
        "type": "object",
        "properties": {
          "identifier": {
            "description": "Client identifier",
            "example": "550e8400-e29b-41d4-a716-446655440000",
            "type": "string",
            "format": "uuid"
          }
        },
        "required": ["identifier"]
      }
    }
  ]
}
```

#### POST `/imt/v1/mcp` — `tools/call`

Exécute un outil identifié par son `name` (dans `params`).

**Requête :**

```json
{
  "jsonrpc": "2.0",
  "id": 2,
  "method": "tools/call",
  "params": {
    "name": "GetOneClient",
    "arguments": {
      "identifier": "550e8400-e29b-41d4-a716-446655440000"
    }
  }
}
```

**Réponse (succès) :**

```json
{
  "jsonrpc": "2.0",
  "id": 2,
  "result": {
    "identifier": "550e8400-...",
    "lastname": "Dupont",
    "firstname": "Jean",
    "genre": "HOMME"
  }
}
```

**Réponse (erreur) :**

```json
{
  "jsonrpc": "2.0",
  "id": 2,
  "error": {
    "code": -32603,
    "message": "Internal error: Le client 550e8400-... n'existe pas"
  }
}
```

---

## 📄 Primitive 2 — Resources

Les resources sont des **données de référence en lecture seule** que le LLM consulte avant d'agir.
Chaque resource implémente l'interface `ResourceHandler`.

### Interface ResourceHandler

```java
public interface ResourceHandler extends OutputAble<ResourceOutput> {

    // Méthode par défaut — construit le descripteur ResourceOutput pour resources/list
    default ResourceOutput toOutput();

    String getResourceUri();         // URI unique : scheme://path

    String getResourceName();        // Nom lisible

    String getResourceDescription(); // Description pour le LLM

    String getMimeType();            // ex : "application/json"

    ResourceContent apply();         // Contenu retourné par resources/read
}
```

> ⚠️ **Changements par rapport à la version précédente** :
> - `read()` → **`apply()`**
> - `toOutput()` est désormais une **méthode par défaut**

### Resources disponibles

| URI                        | Handler                      | Contenu                            | Cas d'usage              |
|----------------------------|------------------------------|------------------------------------|--------------------------|
| `reference://types-compte` | `TypesCompteResourceHandler` | Liste des `TypeCompteEnum` valides | Avant de créer un compte |
| `reference://genres`       | `GenresResourceHandler`      | Liste des `GenreEnum` valides      | Avant de créer un client |

### Endpoints

#### POST `/imt/v1/mcp` — `resources/list`

**Requête :**

```json
{
  "jsonrpc": "2.0",
  "id": 3,
  "method": "resources/list"
}
```

**Réponse :**

```json
{
  "jsonrpc": "2.0",
  "id": 3,
  "result": [
    {
      "uri": "reference://types-compte",
      "name": "Types de compte bancaire disponibles",
      "description": "Liste des types de compte valides à utiliser lors de la création ou modification d'un compte bancaire",
      "mimeType": "application/json"
    },
    {
      "uri": "reference://genres",
      "name": "Genres disponibles pour un client",
      "description": "Liste des genres valides à utiliser lors de la création ou modification d'un client",
      "mimeType": "application/json"
    }
  ]
}
```

#### POST `/imt/v1/mcp` — `resources/read`

**Requête :**

```json
{
  "jsonrpc": "2.0",
  "id": 4,
  "method": "resources/read",
  "params": {
    "uri": "reference://types-compte"
  }
}
```

**Réponse :**

```json
{
  "jsonrpc": "2.0",
  "id": 4,
  "result": {
    "uri": "reference://types-compte",
    "mimeType": "application/json",
    "text": "[
      { \"value\": \"COMPTE_COURANT\", \"description\": \"Compte courant classique\" },
      { \"value\": \"LIVRET_A\",       \"description\": \"Livret d'épargne A\" },
      { \"value\": \"LDDS\",           \"description\": \"Livret de développement durable et solidaire\" }
    ]"
  }
}
```

---

## 💬 Primitive 3 — Prompts

Les prompts sont des **templates de workflow** qui guident le LLM à travers plusieurs Tool calls enchaînés.
Chaque prompt implémente l'interface `PromptHandler`.

### Interface PromptHandler

```java
public interface PromptHandler extends OutputAble<PromptOutput> {

    // Méthode par défaut — construit le descripteur PromptOutput pour prompts/list
    default PromptOutput toOutput();

    String getName();

    String getPromptDescription();

    List<PromptArgument> getArguments();

    PromptGetOutput apply(Map<String, String> arguments); // Rendu du template
}
```

> ⚠️ **Changements par rapport à la version précédente** :
> - `getPromptName()` → **`getName()`**
> - `get(Map<String, String>)` → **`apply(Map<String, String>)`**
> - `toOutput()` est désormais une **méthode par défaut**

### Prompts disponibles

| Nom              | Handler                      | Arguments                 | Workflow orchestré                                   |
|------------------|------------------------------|---------------------------|------------------------------------------------------|
| `analyse-client` | `AnalyseClientPromptHandler` | `clientIdentifier` (UUID) | `GetOneClient` → `GetAllComptes` → analyse → rapport |

### Endpoints

#### POST `/imt/v1/mcp` — `prompts/list`

**Requête :**

```json
{
  "jsonrpc": "2.0",
  "id": 5,
  "method": "prompts/list"
}
```

**Réponse :**

```json
{
  "jsonrpc": "2.0",
  "id": 5,
  "result": [
    {
      "name": "analyse-client",
      "description": "Génère un workflow guidé pour analyser la situation financière complète d'un client",
      "arguments": [
        {
          "name": "clientIdentifier",
          "description": "Identifiant UUID du client dont on veut analyser la situation financière",
          "required": true
        }
      ]
    }
  ]
}
```

#### POST `/imt/v1/mcp` — `prompts/get`

**Requête :**

```json
{
  "jsonrpc": "2.0",
  "id": 6,
  "method": "prompts/get",
  "params": {
    "name": "analyse-client",
    "arguments": {
      "clientIdentifier": "550e8400-e29b-41d4-a716-446655440000"
    }
  }
}
```

**Réponse :**

```json
{
  "jsonrpc": "2.0",
  "id": 6,
  "result": {
    "name": "analyse-client",
    "messages": [
      {
        "role": "user",
        "content": "Tu es un conseiller bancaire expert. Ta mission est d'analyser la situation financière du client identifié par l'UUID : 550e8400-...\n\nÉtape 1 — Appelle GetOneClient...\nÉtape 2 — Appelle GetAllComptes...\nÉtape 3 — Analyse les soldes...\nÉtape 4 — Produis un rapport structuré..."
      }
    ]
  }
}
```

---

## 📨 Modèles JSON-RPC 2.0

### `JsonRpcRequest` (input)

Modèle unifié de toute requête JSON-RPC 2.0. Remplace les anciens modèles séparés
(`ToolActionInput`, `ResourceReadInput`, `PromptGetInput`).

```java
public class JsonRpcRequest implements Serializable {
    private String jsonrpc;              // Toujours "2.0"
    private Object id;                   // Entier, chaîne, ou null (notification)
    private String method;               // ex : "tools/call"
    private Map<String, Object> params;  // Paramètres de la méthode
}
```

### `JsonRpcResponse` (output — record Java)

Modèle unifié de toute réponse JSON-RPC 2.0. Remplace les anciens modèles séparés
(`ToolActionOutput`, `ToolErrorOutput`).

```java
public record JsonRpcResponse(String jsonrpc, Object id, Object result, JsonRpcError error) {

    // Méthode de fabrique : réponse de succès
    public static JsonRpcResponse success(Object id, Object result);

    // Méthode de fabrique : réponse d'erreur
    public static JsonRpcResponse error(Object id, JsonRpcError error);
}
```

> Règle : `result` et `error` sont mutuellement exclusifs. `@JsonInclude(NON_NULL)` garantit
> qu'un seul des deux champs apparaît dans le JSON sérialisé.

### `JsonRpcError` (output — record Java)

```java
public record JsonRpcError(int code, String message, Object data) {

    // Constantes standard JSON-RPC 2.0
    public static final int PARSE_ERROR    = -32700;
    public static final int INVALID_REQUEST = -32600;
    public static final int METHOD_NOT_FOUND = -32601;
    public static final int INVALID_PARAMS  = -32602;
    public static final int INTERNAL_ERROR  = -32603;

    // Méthodes de fabrique
    public static JsonRpcError parseError(String detail);
    public static JsonRpcError methodNotFound(String methodName);
    public static JsonRpcError invalidRequest(String detail);
    public static JsonRpcError invalidParams(String detail);
    public static JsonRpcError internalError(String detail);
}
```

---

## ✅ Chaîne de validation — Chain of Responsibility

Avant le routing, le contrôleur exécute deux étapes de validation en séquence via le patron
**Chain of Responsibility** (classes héritant de `AbstractValidatorStep<JsonRpcRequest>` du domain).

```
JsonVersionValidatorStep  →  MethodValidatorStep
        │                           │
        │ vérifie                   │ vérifie
        │ jsonrpc == "2.0"          │ method non vide + connue de MethodTypeEnum
        │ → -32600 si KO            │ + params requis présents
                                    │ → -32601 si method inconnue
                                    │ → -32602 si param manquant
```

### `JsonVersionValidatorStep`

Vérifie que `jsonrpc == "2.0"`. Lève `InvalidRequestException` (code `-32600`) sinon.

### `MethodValidatorStep`

Vérifie que `method` est non vide et correspond à une valeur de `MethodTypeEnum`.
Puis vérifie les paramètres obligatoires par méthode :

| Méthode           | Paramètre requis  | Erreur si absent            |
|-------------------|-------------------|-----------------------------|
| `tools/call`      | `params.name`     | `-32602` Invalid params     |
| `resources/read`  | `params.uri`      | `-32602` Invalid params     |
| `prompts/get`     | `params.name`     | `-32602` Invalid params     |
| `tools/list`, `resources/list`, `prompts/list` | Aucun | — |

### Intégration dans le contrôleur

```java
@PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
public JsonRpcResponse dispatch(@RequestBody final JsonRpcRequest request) {
    final ValidatorResult validationResult = new JsonVersionValidatorStep()
            .linkWith(new MethodValidatorStep())
            .validate(request);

    if (!validationResult.isValid()) {
        final InvalidRequestException ex = (InvalidRequestException) validationResult.getExceptionToThrow();
        return JsonRpcResponse.error(ex.getId(), JsonRpcError.invalidRequest(ex.getMessage()));
    }
    // ... routing par MethodTypeEnum
}
```

---

## 🔢 `MethodTypeEnum` — Centralisation des méthodes JSON-RPC

Énumération centralisant toutes les valeurs valides du champ `method`.
Utilisée à la fois pour la **validation** et le **routing**.

| Constante         | Path JSON-RPC       | Primitive MCP                        |
|-------------------|---------------------|--------------------------------------|
| `TOOLS_LIST`      | `"tools/list"`      | Découverte des outils                |
| `TOOLS_CALL`      | `"tools/call"`      | Exécution d'un outil                 |
| `RESOURCES_LIST`  | `"resources/list"`  | Découverte des ressources            |
| `RESOURCES_CALL`  | `"resources/read"`  | Lecture d'une ressource (read)       |
| `PROMPTS_LIST`    | `"prompts/list"`    | Découverte des prompts               |
| `PROMPTS_GET`     | `"prompts/get"`     | Rendu d'un prompt                    |

```java
// Recherche par path JSON-RPC
Optional<MethodTypeEnum> method = MethodTypeEnum.fromPath("tools/call"); // → TOOLS_CALL
```

---

## 🏗️ Architecture — Extensibilité par injection de collection

Le `JsonRpcController` ne connaît aucun outil, resource ou prompt en dur.
Spring injecte automatiquement **toutes les implémentations** de chaque interface présentes dans le contexte :

```java
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/imt/v1/mcp")
public class JsonRpcController {
    private final Collection<ToolHandler<?>> toolHandlers;      // Tous les @Service ToolHandler
    private final Collection<ResourceHandler> resourceHandlers; // Tous les @Service ResourceHandler
    private final Collection<PromptHandler> promptHandlers;     // Tous les @Service PromptHandler
}
```

**Ajouter un nouvel outil** = créer une classe `@Service` qui implémente `ToolHandler<I>`.
Aucune modification du contrôleur n'est nécessaire (**principe Open/Closed**).

```
LLM / Agent
    │
    ▼
JsonRpcController
    │ [validation chain]
    │ JsonVersionValidatorStep → MethodValidatorStep
    │
    ├──────────────── tools/list    ──► handleResponseList(toolHandlers)     ──► [ToolOutput × N]
    │
    ├──────────────── tools/call    ──► handler.apply(arguments)             ──► Service métier
    │
    ├──────────────── resources/list──► handleResponseList(resourceHandlers) ──► [ResourceOutput × N]
    │
    ├──────────────── resources/read──► handler.apply()
    │
    ├──────────────── prompts/list  ──► handleResponseList(promptHandlers)   ──► [PromptOutput × N]
    │
    └──────────────── prompts/get   ──► handler.apply(args)
```

---

## 🚨 Gestion des erreurs

Le protocole JSON-RPC 2.0 définit ses propres codes d'erreur dans le corps de la réponse.
HTTP retourne **toujours 200 OK** — le code HTTP ne signale que les erreurs de transport.

### Codes d'erreur JSON-RPC

| Code JSON-RPC | Constante `JsonRpcError` | Déclencheur                                            |
|---------------|--------------------------|--------------------------------------------------------|
| `-32700`      | `PARSE_ERROR`            | Corps JSON invalide (`HttpMessageNotReadableException`) |
| `-32600`      | `INVALID_REQUEST`        | Champ `jsonrpc` absent ou != `"2.0"` / `method` absent |
| `-32601`      | `METHOD_NOT_FOUND`       | `method` inconnue ou outil/prompt introuvable          |
| `-32602`      | `INVALID_PARAMS`         | Paramètres requis manquants (`name`, `uri`...)         |
| `-32603`      | `INTERNAL_ERROR`         | Exception technique imprévue ou `ImtException` métier  |

### Exceptions MCP

| Exception                 | Code JSON-RPC | Levée par                                                      |
|---------------------------|---------------|----------------------------------------------------------------|
| `InvalidRequestException` | `-32600/-32601/-32602` | `JsonVersionValidatorStep`, `MethodValidatorStep`     |
| `McpNotFoundException`    | `-32601`      | Handlers (element introuvable)                                 |
| `ImtException`            | `-32603`      | Services métier du domain                                      |

### `ControllerExceptionHandler` — filet de sécurité

Le contrôleur intercepte lui-même la quasi-totalité des exceptions dans son `try/catch`.
Ce handler est un **filet de sécurité** pour les exceptions survenant *avant* l'entrée dans le contrôleur :

| Handler                           | Exception interceptée              | Code JSON-RPC |
|-----------------------------------|------------------------------------|---------------|
| `handleParseError`                | `HttpMessageNotReadableException`  | `-32700`      |
| `handleInvalidRequestException`   | `InvalidRequestException`          | `-32600`      |
| `handleImtException`              | `ImtException`                     | `-32603`      |
| `handleNotFoundException`         | `McpNotFoundException`             | `-32601`      |
| `handleGenericException`          | `Exception`                        | `-32603`      |

---

## 🛠️ Utilitaires clés

### JsonSchemaGenerator

Génère dynamiquement le champ `inputSchema` d'un outil par **introspection Java** (`java.lang.reflect`) :

- `@NotNull` → champ dans `"required": [...]`
- `@Schema(description = "...")` → champ `"description"`
- `@Schema(example = "...")` → champ `"example"`
- Type `UUID` → `"type": "string", "format": "uuid"`
- Type `Integer` → `"type": "integer", "format": "int32"`

### JsonUtils

Convertit les arguments d'appel (`Map<String, Object>`) vers l'objet Java fortement typé `<I>` via
`ObjectMapper.convertValue()`.

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
    </dependency>

    <!-- Swagger Annotations (pour @Schema sur les arguments) -->
    <dependency>
        <groupId>io.swagger.core.v3</groupId>
        <artifactId>swagger-annotations</artifactId>
    </dependency>
</dependencies>
```

**⚠️ Important** : Ce module ne dépend PAS de `adapters-out-bdd` ou `adapters-out-writers` !

---

## ✅ Règles à respecter

### ✅ AUTORISÉ

- ✅ Annotations Spring MVC (`@RestController`, `@GetMapping`, etc.)
- ✅ Dépendre uniquement du domain
- ✅ Créer de nouveaux `ToolHandler`, `ResourceHandler`, `PromptHandler` en `@Service`
- ✅ Utiliser `@Schema` sur les champs des classes d'arguments pour enrichir l'`inputSchema`
- ✅ Utiliser `@NotNull` pour marquer les arguments obligatoires

### ❌ INTERDIT

- ❌ Dépendre de `adapters-out-bdd` ou `adapters-out-writers`
- ❌ Logique métier dans les handlers (déléguer au domain)
- ❌ Exposer directement les modèles métier (`Client`, `Compte`)
- ❌ Modifier `JsonRpcController` pour ajouter un outil en dur

---

## 🧪 Scénario de test complet (Postman / curl)

Voici le scénario type qu'un LLM exécuterait pour analyser un client.
Toutes les requêtes sont des `POST /imt/v1/mcp` avec le format JSON-RPC 2.0 :

```bash
# 1. Découverte des capacités
POST /imt/v1/mcp  { "jsonrpc":"2.0","id":1,"method":"tools/list" }
POST /imt/v1/mcp  { "jsonrpc":"2.0","id":2,"method":"resources/list" }
POST /imt/v1/mcp  { "jsonrpc":"2.0","id":3,"method":"prompts/list" }

# 2. Consultation des données de référence
POST /imt/v1/mcp
{ "jsonrpc":"2.0","id":4,"method":"resources/read","params":{"uri":"reference://types-compte"} }

# 3. Récupération de tous les clients
POST /imt/v1/mcp
{ "jsonrpc":"2.0","id":5,"method":"tools/call","params":{"name":"GetAllClients","arguments":{}} }

# 4. Récupération d'un client spécifique
POST /imt/v1/mcp
{ "jsonrpc":"2.0","id":6,"method":"tools/call","params":{"name":"GetOneClient","arguments":{"identifier":"550e8400-..."}} }

# 5. Récupération de ses comptes
POST /imt/v1/mcp
{ "jsonrpc":"2.0","id":7,"method":"tools/call","params":{"name":"GetAllComptes","arguments":{"clientIdentifier":"550e8400-..."}} }

# 6. Obtention du workflow d'analyse guidé
POST /imt/v1/mcp
{ "jsonrpc":"2.0","id":8,"method":"prompts/get","params":{"name":"analyse-client","arguments":{"clientIdentifier":"550e8400-..."}} }
```

---

## 🎯 Architecture Hexagonale

```
HTTP Request (LLM / Agent)
        │
        ▼
┌───────────────────────┐
│   JsonRpcController   │  ← Ce module (adaptateur primaire)
│                       │
│  ┌─────────────────┐  │
│  │ Validation chain│  │  (Chain of Responsibility)
│  │ JsonVersion →   │  │
│  │ MethodValidator │  │
│  └─────────────────┘  │
└──────────┬────────────┘
           │ injecte ClientsService, ComptesService
           ▼
      ┌─────────┐
      │ DOMAIN  │  ← Logique métier pure (ignorante de MCP)
      └─────────┘
```

Le handler :

1. Reçoit les arguments JSON (via `ToolHandler.apply(Map)`)
2. Convertit `Map` → objet typé (via `JsonUtils.readFromMap()`)
3. Appelle le service métier du domain
4. Retourne le résultat brut (sérialisé automatiquement en JSON par Spring dans `JsonRpcResponse.success()`)

---

## 📖 Voir Aussi

- [Architecture Hexagonale](../../architecture_hexagonal.wiki/Architecture.md)
- [Module Domain](../domain/README.md)
- [Module Adapters-In-REST](../adapters-in-rest/README.md)
- [Spécification MCP — Tools](https://spec.modelcontextprotocol.io/specification/server/tools/)
- [Spécification MCP — Resources](https://spec.modelcontextprotocol.io/specification/server/resources/)
- [Spécification MCP — Prompts](https://spec.modelcontextprotocol.io/specification/server/prompts/)

---

**Module Adapters-In-MCP — Architecture Hexagonale**  
_Adaptateur primaire Model Context Protocol (JSON-RPC 2.0 sur HTTP)_
