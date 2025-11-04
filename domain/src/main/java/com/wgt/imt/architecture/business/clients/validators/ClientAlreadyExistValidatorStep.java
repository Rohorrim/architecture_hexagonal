package com.wgt.imt.architecture.business.clients.validators;

import com.wgt.imt.architecture.business.clients.ClientStorageProvider;
import com.wgt.imt.architecture.business.clients.model.Client;
import com.wgt.imt.architecture.business.common.exception.ConflictException;
import com.wgt.imt.architecture.business.common.exception.ImtException;
import com.wgt.imt.architecture.business.common.validators.AbstractValidatorStep;
import lombok.AllArgsConstructor;

import java.util.Collections;
import java.util.Objects;

/**
 * Validateur pour l'unicité d'un client.
 * Vérifie qu'un client avec le même nom, prénom et genre n'existe pas déjà.
 *
 * @author Emmanuel WAGUET
 * @version 1.0
 */
@AllArgsConstructor
public class ClientAlreadyExistValidatorStep extends AbstractValidatorStep<Client> {
    /**
     * Service d'accès à la base de données pour les clients
     */
    protected ClientStorageProvider service;

    /**
     * Vérifie qu'aucun client avec les mêmes caractéristiques n'existe déjà.
     *
     * @param toValidate le client à valider
     * @throws ConflictException si un client similaire existe déjà
     */
    @Override
    public void check(final Client toValidate) throws ImtException {
        if (Objects.requireNonNullElse(this.service.getAll(), Collections.<Client>emptySet()).stream().anyMatch(alreadySaved -> this.isSameThing(toValidate, alreadySaved))) {
            throw new ConflictException(String.format("Un client ayant ses infos existe déjà : lastname : %s, firstname : %s, genre : %s", toValidate.getLastname(), toValidate.getFirstname(), toValidate.getGenre().name()));
        }
    }

    /**
     * Détermine si deux clients sont identiques (même nom, prénom et genre).
     *
     * @param input        le client à vérifier
     * @param alreadySaved le client existant
     * @return true si les clients ont le même nom, prénom et genre, false sinon
     */
    private boolean isSameThing(final Client input, final Client alreadySaved) {
        return alreadySaved.getLastname().equalsIgnoreCase(input.getLastname())
                && alreadySaved.getFirstname().equalsIgnoreCase(input.getFirstname())
                && alreadySaved.getGenre().equals(input.getGenre());
    }
}
