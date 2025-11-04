package com.wgt.imt.architecture.business.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.io.Serial;

/**
 * Exception levée lorsqu'il y a un conflit avec l'état actuel de la ressource.
 * Utilisée notamment lorsqu'un client ou compte existe déjà.
 *
 * @author Emmanuel WAGUET
 * @version 1.0
 */
@Getter
@RequiredArgsConstructor
@ToString
public class ConflictException extends ImtException {

    @Serial
    private static final long serialVersionUID = 4299352217542538098L;

    /**
     * Message d'erreur décrivant le conflit
     */
    private final String message;
}
