package com.wgt.imt.architecture.listeners.comptes;

import com.wgt.imt.architecture.business.common.exception.ImtException;
import com.wgt.imt.architecture.business.common.model.MouvementCompteType;
import com.wgt.imt.architecture.business.comptes.ComptesService;
import com.wgt.imt.architecture.business.comptes.events.MouvementCompteEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Applique les agios aux événements de mouvement utilisateur.
 */
@Service
@AllArgsConstructor
@Slf4j
public class AgiosEventListener {
    private final ComptesService comptesService;

    @Async
    @EventListener
    public void onMouvementCompteEvent(final MouvementCompteEvent event) {
        if (Objects.isNull(event.type()) || MouvementCompteType.AUTOMATIQUE.equals(event.type())) {
            return;
        }

        try {
            if (this.comptesService.applyAgiosIfNeeded(event.clientIdentifier(), event.compteIdentifier())) {
                log.info("Agios appliqués au compte {}", event.compteIdentifier());
            } else {
                log.info("Aucun agio à appliquer au compte {}", event.compteIdentifier());
            }
        } catch (final ImtException e) {
            log.error("Échec de l'application des agios au compte {}", event.compteIdentifier(), e);
            throw new IllegalStateException("Impossible d'appliquer les agios au compte", e);
        }
    }
}
