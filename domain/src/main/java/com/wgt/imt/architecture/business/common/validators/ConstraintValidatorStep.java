package com.wgt.imt.architecture.business.common.validators;

import com.wgt.imt.architecture.business.common.exception.BadRequestException;
import com.wgt.imt.architecture.business.common.exception.ImtException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import lombok.AllArgsConstructor;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Validateur pour les contraintes Jakarta Validation (anciennement Bean Validation).
 * Vérifie que toutes les annotations de validation (@NotNull, @Pattern, etc.) sont respectées.
 *
 * @param <T> le type d'objet à valider
 * @author Emmanuel WAGUET
 * @version 1.0
 */
@AllArgsConstructor
public class ConstraintValidatorStep<T> extends AbstractValidatorStep<T> {

    /**
     * Vérifie que toutes les contraintes de validation sur l'objet sont respectées.
     *
     * @param toValidate l'objet à valider
     * @throws BadRequestException si au moins une contrainte n'est pas respectée
     */
    @Override
    public void check(final T toValidate) throws ImtException {
        try (final ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory()) {
            final Set<ConstraintViolation<T>> violations = validatorFactory.getValidator().validate(toValidate);

            if (!violations.isEmpty()) {
                throw new BadRequestException(String.format(
                        "Au moins une contrainte sur l'objet n'est pas respectée : %s",
                        violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.joining(", ")))
                );
            }
        }
    }
}
