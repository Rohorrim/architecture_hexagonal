package com.wgt.imt.architecture.listeners.comptes;

import com.wgt.imt.architecture.business.common.exception.ImtException;
import com.wgt.imt.architecture.business.common.model.MouvementCompteType;
import com.wgt.imt.architecture.business.comptes.ComptesService;
import com.wgt.imt.architecture.business.comptes.events.MouvementCompteEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgiosEventListenerTest {
    @Mock
    private ComptesService comptesService;

    @InjectMocks
    private AgiosEventListener listener;

    @Test
    void shouldApplyAgiosForUserMovement() throws ImtException {
        UUID clientIdentifier = UUID.randomUUID();
        UUID compteIdentifier = UUID.randomUUID();
        MouvementCompteEvent event = new MouvementCompteEvent(
                clientIdentifier, compteIdentifier, "Dépôt", MouvementCompteType.UTILISATEUR
        );
        when(comptesService.applyAgiosIfNeeded(clientIdentifier, compteIdentifier)).thenReturn(true);

        listener.onMouvementCompteEvent(event);

        verify(comptesService).applyAgiosIfNeeded(clientIdentifier, compteIdentifier);
    }

    @Test
    void shouldNotApplyAgiosForAutomaticMovement() {
        MouvementCompteEvent event = new MouvementCompteEvent(
                UUID.randomUUID(), UUID.randomUUID(), "Agios", MouvementCompteType.AUTOMATIQUE
        );

        listener.onMouvementCompteEvent(event);

        verifyNoInteractions(comptesService);
    }
}
