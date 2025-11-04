package com.wgt.imt.architecture.business.comptes.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Mouvement tracé dans l'historique d'un compte.
 */
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class Mouvement {
    @Builder.Default
    private final UUID identifier = UUID.randomUUID();

    @Builder.Default
    private final LocalDateTime date = LocalDateTime.now();

    private final String message;
}
