package com.wgt.imt.architecture.business.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.io.Serial;

/**
 * Exception levée lorsqu'une ressource demandée n'est pas trouvée.
 *
 * @author Emmanuel WAGUET
 * @version 1.0
 */
@Getter
@RequiredArgsConstructor
@ToString
public class NotFoundException extends ImtException {

    @Serial
    private static final long serialVersionUID = -7599445934261061381L;

    /**
     * Message d'erreur décrivant la ressource non trouvée
     */
    private final String message;
}
