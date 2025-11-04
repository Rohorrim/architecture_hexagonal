package com.wgt.imt.architecture.bdd.clients.repositories.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Mouvement embarqué dans l'historique d'un compte MongoDB.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MouvementEntity {
    private String identifier;
    private LocalDateTime date;
    private String message;
}
