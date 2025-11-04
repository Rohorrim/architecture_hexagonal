package com.wgt.imt.architecture.listeners.comptes;

import com.wgt.imt.architecture.business.common.exception.ImtException;
import com.wgt.imt.architecture.business.comptes.events.MouvementCompteEvent;
import com.wgt.imt.architecture.business.comptes.ComptesService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Écouteur d'événements pour les mouvements de compte.
 * L'exécution est asynchrone pour ne pas bloquer le traitement principal.
 *
 * @author Emmanuel WAGUET
 * @version 1.0
 */
@Service
@AllArgsConstructor
@Slf4j
public class MouvementCompteEventListener {

    /**
     * Service métier pour la gestion des comptes
     */
    private final ComptesService comptesService;

    /**
     * Ajoute le mouvement reçu à l'historique du compte.
     * Méthode exécutée de manière asynchrone.
     *
     * @param event l'événement contenant les informations du compte
     */
    @Async
    @EventListener
    public void onMouvementCompteEvent(final MouvementCompteEvent event) {
        try {
            this.comptesService.addIntoHistory(
                    event.clientIdentifier(), event.compteIdentifier(), event.message()
            );
        } catch (final ImtException e) {
            log.error("Échec de l'ajout du mouvement à l'historique du compte {}", event.compteIdentifier(), e);
            throw new IllegalStateException("Impossible de mettre à jour l'historique du compte", e);
        }
    }
}
