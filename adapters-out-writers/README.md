# Adaptateur sortant — publication des événements

Ce module adapte les métadonnées métier de `MouvementCompteObserver` au bus d'événements Spring. Il n'implémente pas de port technique dans le domaine et ne connaît pas les listeners.

## Publication

`MouvementCompteEventPublisher` est un aspect Spring qui intercepte les méthodes annotées après leur retour réussi. Il évalue les expressions SpEL d'identifiants et la condition `doesPublish`, puis publie le contrat métier `MouvementCompteEvent`.

Les services du domaine déclarent uniquement les données d'observation (`message`, `type`, expressions d'identifiants et condition). Ils ne dépendent ni de Spring ni de cet adaptateur.

```java
@MouvementCompteObserver(
    message = "Application automatique des agios",
    type = MouvementCompteType.AUTOMATIQUE,
    compteIdentifierExpression = "#p1",
    doesPublish = "#result"
)
public boolean applyAgiosIfNeeded(UUID clientIdentifier, UUID compteIdentifier) {
    // Traitement métier
}
```

Le contrat d'événement est défini dans `domain` et ne référence aucune classe Spring. L'adaptateur entrant écoute ce contrat avec `@EventListener`; il n'a donc pas à dépendre de ce module.

## Dépendances et tests

Le module dépend du `domain`, de Spring Context/AOP et de l'API SLF4J. Les tests vérifient la publication après succès, l'extraction des identifiants et l'absence de publication lorsque la condition SpEL est fausse.
