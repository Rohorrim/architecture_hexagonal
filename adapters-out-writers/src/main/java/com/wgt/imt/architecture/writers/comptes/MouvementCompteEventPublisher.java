package com.wgt.imt.architecture.writers.comptes;

import com.wgt.imt.architecture.business.comptes.annotations.MouvementCompteObserver;
import com.wgt.imt.architecture.business.comptes.events.MouvementCompteEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.UUID;

/**
 * Publie les événements déclarés sur les méthodes métier observées.
 */
@Aspect
@Component
@AllArgsConstructor
@Slf4j
public class MouvementCompteEventPublisher {
    private static final ExpressionParser EXPRESSION_PARSER = new SpelExpressionParser();
    private static final DefaultParameterNameDiscoverer PARAMETER_NAME_DISCOVERER = new DefaultParameterNameDiscoverer();

    /**
     * Publisher d'événements Spring
     */
    private final ApplicationEventPublisher publisher;

    /**
     * Publie l'événement associé après le retour réussi d'une méthode métier observée.
     *
     * @param joinPoint invocation de la méthode observée
     * @param observer métadonnées déclarées sur la méthode
     * @param result résultat de la méthode observée
     */
    @AfterReturning(
            value = "@annotation(observer)",
            returning = "result",
            argNames = "joinPoint,observer,result"
    )
    public void publierMouvement(final JoinPoint joinPoint, final MouvementCompteObserver observer, final Object result) {
        final Boolean shouldPublish = EXPRESSION_PARSER
                .parseExpression(observer.doesPublish())
                .getValue(this.createEvaluationContext(joinPoint, result), Boolean.class);
        if (!Objects.requireNonNull(shouldPublish, "La condition SpEL de publication a renvoyé null")) {
            return;
        }

        final UUID clientIdentifier = this.extractIdentifier(joinPoint, observer.clientIdentifierExpression());
        final UUID compteIdentifier = this.extractIdentifier(joinPoint, observer.compteIdentifierExpression());
        log.info("Publication de l'événement de mouvement pour le compte {} du client {} ({}: {})",
                compteIdentifier, clientIdentifier, observer.type(), observer.message());
        this.publisher.publishEvent(new MouvementCompteEvent(
                clientIdentifier, compteIdentifier, observer.message(), observer.type()
        ));
    }

    private UUID extractIdentifier(final JoinPoint joinPoint, final String expression) {
        final UUID identifier = EXPRESSION_PARSER
                .parseExpression(expression)
                .getValue(this.createEvaluationContext(joinPoint, null), UUID.class);
        if (identifier == null) {
            throw new IllegalStateException("L'expression SpEL a renvoyé null");
        }
        return identifier;
    }

    private MethodBasedEvaluationContext createEvaluationContext(final JoinPoint joinPoint, final Object result) {
        final Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        final MethodBasedEvaluationContext context = new MethodBasedEvaluationContext(
                joinPoint.getTarget(), method, joinPoint.getArgs(), PARAMETER_NAME_DISCOVERER
        );
        context.setVariable("result", result);
        return context;
    }
}
