package com.wgt.imt.architecture.listeners.comptes;

import com.wgt.imt.architecture.business.common.exception.BadRequestException;
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MouvementCompteEventListenerTest {
    @Mock
    private ComptesService comptesService;

    @InjectMocks
    private MouvementCompteEventListener listener;

    @Test
    void shouldAddEventMessageToAccountHistory() throws ImtException {
        UUID clientIdentifier = UUID.randomUUID();
        UUID compteIdentifier = UUID.randomUUID();
        MouvementCompteEvent event = new MouvementCompteEvent(
                clientIdentifier, compteIdentifier, "Dépôt", MouvementCompteType.UTILISATEUR
        );

        listener.onMouvementCompteEvent(event);

        verify(comptesService).addIntoHistory(clientIdentifier, compteIdentifier, "Dépôt");
    }

    @Test
    void shouldSurfaceHistoryUpdateFailure() throws ImtException {
        UUID clientIdentifier = UUID.randomUUID();
        UUID compteIdentifier = UUID.randomUUID();
        MouvementCompteEvent event = new MouvementCompteEvent(
                clientIdentifier, compteIdentifier, "Dépôt", MouvementCompteType.UTILISATEUR
        );
        ImtException failure = new BadRequestException("Échec du stockage");
        doThrow(failure).when(comptesService).addIntoHistory(clientIdentifier, compteIdentifier, "Dépôt");

        assertThatThrownBy(() -> listener.onMouvementCompteEvent(event))
                .isInstanceOf(IllegalStateException.class)
                .hasCause(failure);
    }
}
