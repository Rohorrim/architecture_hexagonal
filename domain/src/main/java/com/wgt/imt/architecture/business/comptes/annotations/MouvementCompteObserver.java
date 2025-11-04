package com.wgt.imt.architecture.business.comptes.annotations;

import com.wgt.imt.architecture.business.common.model.MouvementCompteType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Métadonnées de l'événement publié après la réussite de la méthode annotée.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MouvementCompteObserver {
    String message();

    MouvementCompteType type();

    String clientIdentifierExpression() default "#p0";

    String compteIdentifierExpression();

    String doesPublish() default "true";
}
