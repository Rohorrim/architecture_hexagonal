package com.wgt.imt.architecture.business.comptes.validators;

import com.wgt.imt.architecture.business.clients.ClientStorageProvider;
import com.wgt.imt.architecture.business.clients.model.Client;
import com.wgt.imt.architecture.business.common.exception.ConflictException;
import com.wgt.imt.architecture.business.common.exception.ImtException;
import com.wgt.imt.architecture.business.common.validators.AbstractValidatorStep;
import com.wgt.imt.architecture.business.comptes.model.Compte;
import lombok.AllArgsConstructor;

import java.util.Collection;
import java.util.UUID;

/**
 * Validateur pour l'unicité d'un compte.
 * Vérifie qu'un compte avec le même nom et le même type n'existe pas déjà pour le client.
 *
 * @author Emmanuel WAGUET
 * @version 1.0
 */
@AllArgsConstructor
public class CompteAlreadyExistValidatorStep extends AbstractValidatorStep<Compte> {
    /**
     * Service d'accès à la base de données pour les clients
     */
    private ClientStorageProvider service;

    /**
     * Identifiant du client pour lequel vérifier l'unicité du compte
     */
    private UUID clientIdentifier;

    /**
     * Vérifie qu'aucun compte avec le même nom et le même type n'existe déjà pour le client.
     *
     * @param toValidate le compte à valider
     * @throws ConflictException si un compte similaire existe déjà
     */
    @Override
    public void check(final Compte toValidate) throws ImtException {
        if (this.service.get(clientIdentifier).map(Client::getComptes).stream().flatMap(Collection::stream).anyMatch(alreadySaved -> this.isSameThing(toValidate, alreadySaved))) {
            throw new ConflictException(String.format("Un compte ayant les mêmes caractéristique existe déjà pour le client %s : name : %s, type : %s", this.clientIdentifier.toString(), toValidate.getName(), toValidate.getType().name()));
        }
    }

    /**
     * Détermine si deux comptes sont identiques (même nom et même type).
     *
     * @param input        le compte à vérifier
     * @param alreadySaved le compte existant
     * @return true si les comptes ont le même nom et le même type, false sinon
     */
    private boolean isSameThing(final Compte input, final Compte alreadySaved) {
        return alreadySaved.getName().equalsIgnoreCase(input.getName())
                && alreadySaved.getType().equals(input.getType());
    }
}
