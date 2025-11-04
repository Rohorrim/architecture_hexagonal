package com.wgt.imt.architecture.business.comptes;

import com.wgt.imt.architecture.business.clients.ClientStorageProvider;
import com.wgt.imt.architecture.business.clients.model.Client;
import com.wgt.imt.architecture.business.comptes.annotations.MouvementCompteObserver;
import com.wgt.imt.architecture.business.common.exception.ImtException;
import com.wgt.imt.architecture.business.common.exception.NotFoundException;
import com.wgt.imt.architecture.business.common.model.MouvementCompteType;
import com.wgt.imt.architecture.business.common.utils.CollectionImtUtils;
import com.wgt.imt.architecture.business.comptes.model.Compte;
import com.wgt.imt.architecture.business.comptes.model.Mouvement;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service métier pour la gestion des comptes bancaires associés aux clients.
 * Permet de filtrer, créer, mettre à jour, appliquer des agios et supprimer des comptes.
 *
 * @author Emmanuel WAGUET
 * @version 1.0
 */
@AllArgsConstructor
@Slf4j
public class ComptesService {
    private static final int MONTANT_AGIOS = 5;

    protected final ClientStorageProvider service;

    /**
     * Récupère tous les comptes associés à un client donné.
     *
     * @param clientIdentifier Identifiant du client
     * @return Collection des comptes du client, ou une collection vide si le client n'existe pas
     */
    public Collection<Compte> getAllFilteredByClient(final UUID clientIdentifier) {
        log.info("Récupération des comptes du client {}", clientIdentifier);
        return this.service.get(clientIdentifier).map(Client::getComptes).orElse(Collections.emptySet());
    }

    /**
     * Récupère un compte spécifique d'un client.
     *
     * @param clientIdentifier Identifiant du client
     * @param identifier       Identifiant du compte
     * @return Un Optional contenant le compte s'il existe, sinon vide
     */
    public Optional<Compte> getOneFilteredByClient(final UUID clientIdentifier, final UUID identifier) {
        log.info("Récupération du compte {} du client {}", identifier, clientIdentifier);
        return this.service.get(clientIdentifier)
                .map(Client::getComptes)
                .stream()
                .flatMap(Collection::stream)
                .filter(compte -> Objects.equals(compte.getIdentifier(), identifier))
                .findFirst();
    }

    /**
     * Crée un nouveau compte pour un client donné.
     *
     * @param clientIdentifier Identifiant du client
     * @param newCompte        Nouveau compte à ajouter
     * @return Le compte créé
     * @throws NotFoundException si le client n'existe pas
     */
    @MouvementCompteObserver(
            message = "Création du compte",
            type = MouvementCompteType.UTILISATEUR,
            compteIdentifierExpression = "#p1.identifier"
    )
    public Compte create(final UUID clientIdentifier, final Compte newCompte) throws ImtException {
        log.info("Création du compte {} pour le client {}", newCompte.getIdentifier(), clientIdentifier);
        final Client client = this.service.get(clientIdentifier).orElseThrow(() -> new NotFoundException(String.format("Le client %s n'existe pas", clientIdentifier)));
        this.service.save(client.toBuilder().comptes(CollectionImtUtils.append(client.getComptes(), newCompte)).build());
        return newCompte;
    }

    /**
     * Met à jour un compte existant d'un client.
     *
     * @param clientIdentifier Identifiant du client
     * @param updatedCompte    Compte mis à jour
     * @throws NotFoundException si le client n'existe pas
     */
    @MouvementCompteObserver(
            message = "Mise à jour du compte",
            type = MouvementCompteType.UTILISATEUR,
            compteIdentifierExpression = "#p1.identifier"
    )
    public void update(final UUID clientIdentifier, final Compte updatedCompte) throws ImtException {
        log.info("Mise à jour du compte {} du client {}", updatedCompte.getIdentifier(), clientIdentifier);
        final Client client = this.service.get(clientIdentifier).orElseThrow(() -> new NotFoundException(String.format("Le client %s n'existe pas", clientIdentifier)));
        this.update(client, updatedCompte);
    }

    private void update(final Client client, final Compte updatedCompte) {
        log.debug("Persistance du compte {} du client {}", updatedCompte.getIdentifier(), client.getIdentifier());
        this.service.save(
                client.toBuilder().comptes(CollectionImtUtils.append(
                        Objects.requireNonNullElse(client.getComptes(), Collections.<Compte>emptySet())
                                .stream()
                                .filter(compte -> !Objects.equals(compte.getIdentifier(), updatedCompte.getIdentifier()))
                                .collect(Collectors.toSet()),
                        updatedCompte
                )).build()
        );
    }

    /**
     * Applique les agios sur le compte d'un client si le solde est négatif.
     *
     * @param clientIdentifier Identifiant du client
     * @param identifier       Identifiant du compte
     * @throws NotFoundException si le client n'existe pas
     */
    @MouvementCompteObserver(
            message = "Application automatique des agios",
            type = MouvementCompteType.AUTOMATIQUE,
            compteIdentifierExpression = "#p1",
            doesPublish = "#result"
    )
    public boolean applyAgiosIfNeeded(final UUID clientIdentifier, final UUID identifier) throws ImtException {
        log.info("Vérification des agios du compte {} du client {}", identifier, clientIdentifier);
        final Client client = this.service.get(clientIdentifier).orElseThrow(() -> new NotFoundException(String.format("Le client %s n'existe pas", clientIdentifier)));
        return Objects.requireNonNullElse(client.getComptes(), Collections.<Compte>emptySet()).stream()
                .filter(compte -> Objects.equals(compte.getIdentifier(), identifier) && compte.getSolde() < 0)
                .findFirst()
                .map(compte -> {
                    this.update(client, compte.toBuilder().solde(compte.getSolde() - MONTANT_AGIOS).build());
                    log.info("Agios appliqués au compte {} du client {}", identifier, clientIdentifier);
                    return true;
                })
                .orElse(false);
    }

    /**
     * Ajoute un mouvement à l'historique d'un compte.
     *
     * @param clientIdentifier identifiant du client propriétaire du compte
     * @param compteIdentifier identifiant du compte
     * @param message description du mouvement
     * @throws NotFoundException si le client n'existe pas
     */
    public void addIntoHistory(final UUID clientIdentifier, final UUID compteIdentifier, final String message) throws ImtException {
        log.info("Ajout d'un mouvement à l'historique du compte {} du client {}", compteIdentifier, clientIdentifier);
        final Client client = this.service.get(clientIdentifier).orElseThrow(() -> new NotFoundException(String.format("Le client %s n'existe pas", clientIdentifier)));
        Objects.requireNonNullElse(client.getComptes(), Collections.<Compte>emptySet()).stream()
                .filter(compte -> Objects.equals(compte.getIdentifier(), compteIdentifier))
                .findFirst()
                .ifPresent(compte -> this.update(
                        client,
                        compte.toBuilder()
                                .historique(CollectionImtUtils.append(
                                        compte.getHistorique(),
                                        Mouvement.builder().message(message).build()
                                ))
                                .build()
                ));
    }

    /**
     * Supprime un compte d'un client.
     *
     * @param clientIdentifier Identifiant du client
     * @param identifier       Identifiant du compte à supprimer
     * @throws NotFoundException si le client n'existe pas
     */
    public void delete(final UUID clientIdentifier, final UUID identifier) throws ImtException {
        log.info("Suppression du compte {} du client {}", identifier, clientIdentifier);
        final Client client = this.service.get(clientIdentifier).orElseThrow(() -> new NotFoundException(String.format("Le client %s n'existe pas", clientIdentifier)));
        this.service.save(client.toBuilder().comptes(
                Objects.requireNonNullElse(client.getComptes(), Collections.<Compte>emptySet())
                        .stream()
                        .filter(compte -> !Objects.equals(compte.getIdentifier(), identifier))
                        .collect(Collectors.toSet())
        ).build());
    }
}
