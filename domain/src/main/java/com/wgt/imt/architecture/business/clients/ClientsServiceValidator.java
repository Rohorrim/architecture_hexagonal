package com.wgt.imt.architecture.business.clients;

import com.wgt.imt.architecture.business.clients.model.Client;
import com.wgt.imt.architecture.business.clients.validators.ClientAlreadyExistValidatorStep;
import com.wgt.imt.architecture.business.clients.validators.ClientGenreValidatorStep;
import com.wgt.imt.architecture.business.common.exception.ImtException;
import com.wgt.imt.architecture.business.common.validators.ConstraintValidatorStep;

/**
 * Service métier de gestion des clients avec validation.
 * Étend ClientsService en ajoutant des validations métier avant les opérations de création et modification.
 * Utilise le pattern Chain of Responsibility pour enchaîner les validations.
 *
 * @author Emmanuel WAGUET
 * @version 1.0
 */
public class ClientsServiceValidator extends ClientsService {

    /**
     * Constructeur du service de validation des clients.
     *
     * @param service le service d'accès à la base de données pour les clients
     */
    public ClientsServiceValidator(final ClientStorageProvider service) {
        super(service);
    }

    /**
     * Crée un nouveau client après validation.
     * Valide que :
     * - Les contraintes sur l'objet sont respectées
     * - Le client n'existe pas déjà
     * - Le genre est valide
     *
     * @param newClient le nouveau client à créer
     * @return le client créé
     * @throws com.wgt.imt.architecture.business.common.exception.BadRequestException si les contraintes ne sont pas respectées
     * @throws com.wgt.imt.architecture.business.common.exception.ConflictException   si le client existe déjà
     */
    public Client create(final Client newClient) throws ImtException {
        new ConstraintValidatorStep<Client>()
                .linkWith(new ClientAlreadyExistValidatorStep(this.service))
                .linkWith(new ClientGenreValidatorStep())
                .validate(newClient)
                .throwIfInvalid();

        return super.create(newClient);
    }

    /**
     * Met à jour un client existant après validation.
     * Valide que :
     * - Les contraintes sur l'objet sont respectées
     * - Le genre est valide
     *
     * @param updatedClient le client avec les informations mises à jour
     * @throws com.wgt.imt.architecture.business.common.exception.BadRequestException si les contraintes ne sont pas respectées
     */
    public void update(final Client updatedClient) throws ImtException {
        new ConstraintValidatorStep<Client>()
                .linkWith(new ClientGenreValidatorStep())
                .validate(updatedClient)
                .throwIfInvalid();

        super.update(updatedClient);
    }
}
