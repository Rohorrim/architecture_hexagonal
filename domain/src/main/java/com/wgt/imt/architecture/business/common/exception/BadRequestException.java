package com.wgt.imt.architecture.business.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.io.Serial;

/**
 * Exception levée lorsqu'une requête est mal formée ou invalide.
 * Utilisée notamment pour les erreurs de validation de contraintes.
 *
 * @author Emmanuel WAGUET
 * @version 1.0
 */
@Getter
@RequiredArgsConstructor
@ToString
public class BadRequestException extends ImtException {

    @Serial
    private static final long serialVersionUID = 2344650719915007302L;

    /**
     * Message d'erreur décrivant le problème de validation
     */
    private final String message;
}
