package com.wgt.imt.architecture.business.clients.validators;

import com.wgt.imt.architecture.business.clients.model.Client;
import com.wgt.imt.architecture.business.common.exception.BadRequestException;
import com.wgt.imt.architecture.business.common.exception.ImtException;
import com.wgt.imt.architecture.business.common.model.GenreEnum;
import com.wgt.imt.architecture.business.common.validators.AbstractValidatorStep;
import lombok.AllArgsConstructor;

/**
 * Validateur pour le genre d'un client.
 * Vérifie que le genre fourni correspond à une valeur acceptable (HOMME, FEMME, NON_COMMUNIQUER).
 *
 * @author Emmanuel WAGUET
 * @version 1.0
 */
@AllArgsConstructor
public class ClientGenreValidatorStep extends AbstractValidatorStep<Client> {

    /**
     * Vérifie que le genre du client n'est pas INCONNU.
     *
     * @param toValidate le client à valider
     * @throws BadRequestException si le genre est INCONNU
     */
    @Override
    public void check(final Client toValidate) throws ImtException {
        if (GenreEnum.INCONNU.equals(toValidate.getGenre())) {
            throw new BadRequestException(String.format("Le genre communiqué ne correspond pas aux valeurs acceptables : %s", GenreEnum.ACCEPTABLE_VALUES));
        }
    }
}
