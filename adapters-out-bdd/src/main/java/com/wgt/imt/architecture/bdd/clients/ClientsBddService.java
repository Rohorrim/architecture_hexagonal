package com.wgt.imt.architecture.bdd.clients;

import com.wgt.imt.architecture.bdd.clients.repositories.ClientRepository;
import com.wgt.imt.architecture.bdd.clients.repositories.entities.ClientEntity;
import com.wgt.imt.architecture.bdd.clients.repositories.mappers.ClientBddMapper;
import com.wgt.imt.architecture.business.clients.ClientStorageProvider;
import com.wgt.imt.architecture.business.clients.model.Client;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service d'accès à la base de données pour les clients.
 * Fait le pont entre la couche métier et la couche de persistence MongoDB.
 * Utilise un mapper pour convertir entre les modèles métier et les entités de base de données.
 *
 * @author Emmanuel WAGUET
 * @version 1.0
 */
@Service
@AllArgsConstructor
public class ClientsBddService implements ClientStorageProvider {
    /**
     * Repository Spring Data MongoDB pour les clients
     */
    private ClientRepository repository;

    /**
     * Mapper pour convertir entre Client et ClientEntity
     */
    private ClientBddMapper mapper;

    /**
     * Vérifie si un client existe en base de données.
     *
     * @param identifier l'identifiant du client
     * @return true si le client existe, false sinon
     */
    @Override
    public boolean exist(final UUID identifier) {
        return Optional.ofNullable(identifier)
                .map(UUID::toString)
                .map(this.repository::existsById)
                .orElse(false);
    }

    /**
     * Récupère tous les clients de la base de données.
     *
     * @return une collection de tous les clients, ou une collection vide si aucun client n'existe
     */
    @Override
    public Collection<Client> getAll() {
        return Objects.requireNonNullElse(this.repository.findAll(), Collections.<ClientEntity>emptyList())
                .stream()
                .map(this.mapper::from)
                .collect(Collectors.toSet());
    }

    /**
     * Récupère un client spécifique par son identifiant.
     *
     * @param identifier l'identifiant unique du client
     * @return un Optional contenant le client s'il existe, Optional.empty() sinon
     */
    @Override
    public Optional<Client> get(final UUID identifier) {
        return Optional.ofNullable(identifier)
                .map(UUID::toString)
                .flatMap(this.repository::findById)
                .map(this.mapper::from);
    }

    /**
     * Sauvegarde un client en base de données (création ou mise à jour).
     *
     * @param client le client à sauvegarder
     * @return le client sauvegardé
     * @throws NullPointerException si le client est null
     */
    @Override
    public Client save(final Client client) {
        Objects.requireNonNull(client, "Impossible de sauvegarder un client nul");
        return this.mapper.from(
                this.repository.save(
                        this.mapper.to(client)
                )
        );
    }

    /**
     * Supprime un client de la base de données.
     *
     * @param identifier l'identifiant du client à supprimer
     */
    @Override
    public void delete(final UUID identifier) {
        Optional.ofNullable(identifier)
                .map(UUID::toString)
                .ifPresent(this.repository::deleteById);
    }
}
