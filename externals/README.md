# Répertoire External - Ressources du Projet

Ce répertoire contient les ressources externes du projet Architecture Bancaire.

## 📂 Contenu

### `mongo/`

Répertoire de données MongoDB utilisé par le projet.

**Utilisation** :

```bash
mongod.exe --dbpath /c/Users/A388771/local/IMT/architecture_hexagonal/externals/mongo/
```

Ou vient le script powershell `start-mongo.ps1` pour démarrer le serveur MongoDB.
PS : Si l'exécution des scripts est bloquée par la politique de sécurité, exécutez dans PowerShell :

**Caractéristiques** :

- ✅ Base de données locale isolée par projet
- ✅ Données persistantes (conservées entre les redémarrages)
- ✅ Sauvegarde facile (copie du répertoire)

**Fichiers générés** :

- `*.wt` : Fichiers WiredTiger (moteur de stockage MongoDB)
- `_mdb_catalog.wt` : Catalogue des collections
- `collection-*.wt` : Fichiers de collection
- `index-*.wt` : Fichiers d'index
- `journal/` : Journal des transactions
- `diagnostic.data/` : Données de diagnostic

**Nettoyage** :
Pour réinitialiser complètement la base de données :

```bash
# Arrêter MongoDB d'abord !
# Puis supprimer le contenu du répertoire
rm -rf C:\Users\A388771\local\IMT\architecture\externals\mongo\*
```

### `postman/`

Collection Postman pour tester l'API REST.

**Utilisation** :

1. Ouvrir Postman
2. Import → File → Sélectionner `architecture.postman_collection.json`
3. Exécuter les requêtes

### `TP.pdf` / `TP.docx`

Énoncé du travail pratique (sujet du cours).

---

## 📊 Structure de la Base MongoDB

### Base de données : `architecture`

#### Collection : `clients`

**Structure du document** :

```json
{
  "_id": "uuid-string",
  "lastname": "Dupont",
  "firstname": "Jean",
  "genre": "HOMME",
  "comptes": [
    {
      "identifier": "uuid-string",
      "solde": 1000.50,
      "typeCompte": "COMPTE_COURANT"
    }
  ]
}
```

**Index** :

- `_id` : Index unique sur l'identifiant

**Taille moyenne** :

- ~200 octets par client (sans comptes)
- +~100 octets par compte

---

## 🔧 Commandes Utiles MongoDB

### Se connecter à la base

```bash
mongosh
```

### Utiliser la base du projet

```javascript
use
architecture
```

### Voir les collections

```javascript
show
collections
```

### Voir tous les clients

```javascript
db.clients.find().pretty()
```

### Compter les clients

```javascript
db.clients.countDocuments()
```

### Trouver un client par nom

```javascript
db.clients.find({lastname: "Dupont"})
```

### Supprimer tous les clients

```javascript
db.clients.deleteMany({})
```

### Statistiques de la collection

```javascript
db.clients.stats()
```

---

## 📁 Gitignore

**Important** : Le répertoire `mongo/` est exclu du contrôle de version (`.gitignore`) car :

- Les fichiers MongoDB sont volumineux
- Les données peuvent contenir des informations sensibles
- Chaque développeur a sa propre base locale

Si vous souhaitez partager des données de test, utilisez plutôt :

- Scripts d'initialisation SQL/JavaScript
- Fichiers JSON d'import
- Fixtures de test

---

_Architecture Bancaire - Projet Pédagogique IMT_

