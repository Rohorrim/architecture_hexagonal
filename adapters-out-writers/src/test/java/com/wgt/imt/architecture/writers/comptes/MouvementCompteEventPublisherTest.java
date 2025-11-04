package com.wgt.imt.architecture.writers.comptes;

import com.wgt.imt.architecture.business.comptes.annotations.MouvementCompteObserver;
import com.wgt.imt.architecture.business.common.model.MouvementCompteType;
import com.wgt.imt.architecture.business.comptes.events.MouvementCompteEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.context.ApplicationEventPublisher;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class MouvementCompteEventPublisherTest {
    @Mock
    private ApplicationEventPublisher publisher;

    private ObservedService observedService;

    @BeforeEach
    void setUp() {
        AspectJProxyFactory proxyFactory = new AspectJProxyFactory(new ObservedService());
        proxyFactory.addAspect(new MouvementCompteEventPublisher(publisher));
        observedService = proxyFactory.getProxy();
    }

    @Test
    void shouldPublishEventAfterObservedMethodSucceeds() {
        UUID clientIdentifier = UUID.randomUUID();
        UUID compteIdentifier = UUID.randomUUID();

        observedService.apply(clientIdentifier, compteIdentifier, true);

        ArgumentCaptor<MouvementCompteEvent> eventCaptor = ArgumentCaptor.forClass(MouvementCompteEvent.class);
        verify(publisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().clientIdentifier()).isEqualTo(clientIdentifier);
        assertThat(eventCaptor.getValue().compteIdentifier()).isEqualTo(compteIdentifier);
        assertThat(eventCaptor.getValue().message()).isEqualTo("Agios appliqués");
        assertThat(eventCaptor.getValue().type()).isEqualTo(MouvementCompteType.AUTOMATIQUE);
    }

    @Test
    void shouldNotPublishWhenConditionIsFalse() {
        observedService.apply(UUID.randomUUID(), UUID.randomUUID(), false);

        verifyNoInteractions(publisher);
    }

    static class ObservedService {
        @MouvementCompteObserver(
                message = "Agios appliqués",
                type = MouvementCompteType.AUTOMATIQUE,
                compteIdentifierExpression = "#p1",
                doesPublish = "#result"
        )
        public boolean apply(final UUID clientIdentifier, final UUID compteIdentifier, final boolean apply) {
            return apply;
        }
    }
}
