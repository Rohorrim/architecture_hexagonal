package com.wgt.imt.architecture.business.clients;

import com.wgt.imt.architecture.business.clients.model.Client;
import com.wgt.imt.architecture.business.common.exception.ImtException;
import lombok.AllArgsConstructor;

import java.util.*;

/**
 * Service métier de gestion des clients.
 * Fournit les opérations CRUD (Create, Read, Update, Delete) pour les clients.
 *
 * @author Emmanuel WAGUET
 * @version 1.0
 */
@AllArgsConstructor
public class ClientsService {
    /**
     * Service d'accès à la base de données pour les clients
     */
    protected ClientStorageProvider service;

    /**
     * Récupère tous les clients.
     *
     * @return une collection de tous les clients, ou une collection vide si aucun client n'existe
     */
    public Collection<Client> getAll() {
        return Objects.requireNonNullElse(this.service.getAll(), Collections.emptySet());
    }

    /**
     * Récupère un client spécifique par son identifiant.
     *
     * @param identifier l'identifiant unique du client
     * @return un Optional contenant le client s'il existe, Optional.empty() sinon
     */
    public Optional<Client> getOne(final UUID identifier) {
        return this.service.get(identifier);
    }

    /**
     * Crée un nouveau client.
     *
     * @param newClient le nouveau client à créer
     * @return le client créé avec son identifiant généré
     */
    public Client create(final Client newClient) throws ImtException {
        return this.service.save(newClient);
    }

    /**
     * Met à jour un client existant.
     *
     * @param updatedClient le client avec les informations mises à jour
     */
    public void update(final Client updatedClient) throws ImtException {
        this.service.save(updatedClient);
    }

    /**
     * Supprime un client par son identifiant.
     *
     * @param identifier l'identifiant unique du client à supprimer
     */
    public void delete(final UUID identifier) throws ImtException {
        this.service.delete(identifier);
    }
}
