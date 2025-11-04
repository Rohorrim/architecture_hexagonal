package com.wgt.imt.architecture.business.common.exception;

import java.io.Serial;

public abstract class ImtException extends Exception {
    @Serial
    private static final long serialVersionUID = 8197177576707058924L;

    /**
     * Retourne le type de l'exception (nom de la classe).
     *
     * @return le nom simple de la classe d'exception
     */
    public String getType() {
        return this.getClass().getSimpleName();
    }
}
