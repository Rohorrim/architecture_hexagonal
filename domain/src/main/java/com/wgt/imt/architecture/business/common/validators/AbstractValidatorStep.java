package com.wgt.imt.architecture.business.common.validators;

import com.wgt.imt.architecture.business.common.exception.ImtException;
import com.wgt.imt.architecture.business.common.model.ValidatorResult;

import java.util.Objects;

/**
 * Classe abstraite pour la validation en chaîne (Chain of Responsibility pattern).
 * Permet de lier plusieurs étapes de validation qui s'exécutent séquentiellement.
 *
 * @param <T> le type d'objet à valider
 * @author Emmanuel WAGUET
 * @version 1.0
 */
public abstract class AbstractValidatorStep<T> {
    /**
     * La prochaine étape de validation dans la chaîne
     */
    private AbstractValidatorStep<T> nextStep;

    /**
     * Effectue la vérification métier spécifique à cette étape de validation.
     *
     * @param toValidate l'objet à valider
     * @throws RuntimeException si la validation échoue
     */
    public abstract void check(final T toValidate) throws ImtException;

    /**
     * Valide l'objet en exécutant cette étape et toutes les étapes suivantes.
     *
     * @param toValidate l'objet à valider
     * @return le résultat de la validation (valide ou invalide avec l'exception)
     * @throws NullPointerException si toValidate est null
     */
    public ValidatorResult validate(final T toValidate) {
        Objects.requireNonNull(toValidate, "Impossible de valider un objet nul");
        try {
            this.check(toValidate);
        } catch (final ImtException e) {
            return ValidatorResult.invalid(e);
        }

        // Continue avec la prochaine étape si elle existe
        if (Objects.nonNull(this.nextStep)) {
            return this.nextStep.validate(toValidate);
        }
        return ValidatorResult.valid();
    }

    /**
     * Lie cette étape de validation avec la suivante pour créer une chaîne.
     *
     * @param nextStep la prochaine étape de validation à exécuter
     * @return cette instance pour permettre le chaînage
     */
    public AbstractValidatorStep<T> linkWith(final AbstractValidatorStep<T> nextStep) {
        if (Objects.isNull(this.nextStep)) {
            this.nextStep = nextStep;
        } else {
            this.nextStep.linkWith(nextStep);
        }
        return this;
    }
}
