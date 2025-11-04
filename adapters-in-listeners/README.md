# Adaptateur entrant — listeners de mouvements

Ce module traite les événements métier sans dépendre de l'adaptateur qui les publie. Il dépend uniquement du `domain` et de Spring Context.

## Traitements

- `MouvementCompteEventListener` ajoute le message de l'événement à l'historique du compte.
- `AgiosEventListener` applique les agios aux événements de type `UTILISATEUR`. Les événements `AUTOMATIQUE` sont ignorés pour éviter une nouvelle application en boucle.

Les deux listeners s'exécutent de façon asynchrone (`@Async`, activé dans `ArchitectureApplication`). Les erreurs de persistance ou de calcul sont journalisées puis remontées sous forme d'exception.

## Flux

1. Une méthode métier annotée avec `@MouvementCompteObserver` réussit.
2. L'aspect de l'adaptateur sortant publie un `MouvementCompteEvent`.
3. Le listener d'historique ajoute le message au compte.
4. Le listener des agios appelle `ComptesService.applyAgiosIfNeeded` pour un mouvement utilisateur.
5. Si les agios sont appliqués, un événement `AUTOMATIQUE` est publié et ajouté à l'historique, sans réappliquer les agios.

Le contrat d'événement est un modèle métier pur (`domain`), pas une classe Spring. Ainsi, cet adaptateur n'a aucune dépendance vers `adapters-out-writers`.

## Tests

Les tests vérifient l'ajout du message à l'historique, la gestion des erreurs et le filtrage des événements automatiques dans le traitement des agios.
