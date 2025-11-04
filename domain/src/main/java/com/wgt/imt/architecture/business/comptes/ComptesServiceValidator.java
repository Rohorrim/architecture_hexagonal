package com.wgt.imt.architecture.business.comptes;

import com.wgt.imt.architecture.business.clients.ClientStorageProvider;
import com.wgt.imt.architecture.business.common.model.MouvementCompteType;
import com.wgt.imt.architecture.business.common.exception.ImtException;
import com.wgt.imt.architecture.business.common.validators.ConstraintValidatorStep;
import com.wgt.imt.architecture.business.comptes.annotations.MouvementCompteObserver;
import com.wgt.imt.architecture.business.comptes.model.Compte;
import com.wgt.imt.architecture.business.comptes.validators.CompteAlreadyExistValidatorStep;
import com.wgt.imt.architecture.business.comptes.validators.CompteTypeValidatorStep;

import java.util.UUID;

public class ComptesServiceValidator extends ComptesService {

    public ComptesServiceValidator(final ClientStorageProvider service) {
        super(service);
    }

    @Override
    @MouvementCompteObserver(
            message = "Création du compte",
            type = MouvementCompteType.UTILISATEUR,
            compteIdentifierExpression = "#p1.identifier"
    )
    public Compte create(final UUID clientIdentifier, final Compte newCompte) throws ImtException {
        new ConstraintValidatorStep<Compte>()
                .linkWith(new CompteAlreadyExistValidatorStep(this.service, clientIdentifier))
                .linkWith(new CompteTypeValidatorStep())
                .validate(newCompte)
                .throwIfInvalid();

        return super.create(clientIdentifier, newCompte);
    }

    @Override
    @MouvementCompteObserver(
            message = "Mise à jour du compte",
            type = MouvementCompteType.UTILISATEUR,
            compteIdentifierExpression = "#p1.identifier"
    )
    public void update(final UUID clientIdentifier, final Compte updatedCompte) throws ImtException {
        new ConstraintValidatorStep<Compte>()
                .linkWith(new CompteTypeValidatorStep())
                .validate(updatedCompte)
                .throwIfInvalid();

        super.update(clientIdentifier, updatedCompte);
    }
}
