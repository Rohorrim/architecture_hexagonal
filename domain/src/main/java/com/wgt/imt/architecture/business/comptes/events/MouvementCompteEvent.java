package com.wgt.imt.architecture.business.comptes.events;

import com.wgt.imt.architecture.business.common.model.MouvementCompteType;

import java.util.UUID;

/**
 * Événement métier décrivant un mouvement réalisé sur un compte.
 */
public record MouvementCompteEvent(
        UUID clientIdentifier,
        UUID compteIdentifier,
        String message,
        MouvementCompteType type
) {
}
