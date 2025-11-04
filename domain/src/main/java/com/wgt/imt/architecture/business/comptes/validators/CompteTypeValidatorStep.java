package com.wgt.imt.architecture.business.comptes.validators;

import com.wgt.imt.architecture.business.common.exception.BadRequestException;
import com.wgt.imt.architecture.business.common.exception.ImtException;
import com.wgt.imt.architecture.business.common.model.TypeCompteEnum;
import com.wgt.imt.architecture.business.common.validators.AbstractValidatorStep;
import com.wgt.imt.architecture.business.comptes.model.Compte;
import lombok.AllArgsConstructor;

/**
 * Validateur pour le type d'un compte bancaire.
 * Vérifie que le type fourni correspond à une valeur acceptable (COMPTE_COURANT, LIVRET_A, LDDS).
 *
 * @author Emmanuel WAGUET
 * @version 1.0
 */
@AllArgsConstructor
public class CompteTypeValidatorStep extends AbstractValidatorStep<Compte> {

    /**
     * Vérifie que le type du compte n'est pas INCONNU.
     *
     * @param toValidate le compte à valider
     * @throws BadRequestException si le type est INCONNU
     */
    @Override
    public void check(final Compte toValidate) throws ImtException {
        if (TypeCompteEnum.INCONNU.equals(toValidate.getType())) {
            throw new BadRequestException(String.format("Le type communiqué ne correspond pas aux valeurs acceptables : %s", TypeCompteEnum.ACCEPTABLE_VALUES));
        }
    }
}
