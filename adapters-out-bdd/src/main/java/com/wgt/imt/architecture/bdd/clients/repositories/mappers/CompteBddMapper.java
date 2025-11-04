package com.wgt.imt.architecture.bdd.clients.repositories.mappers;

import com.wgt.imt.architecture.bdd.clients.repositories.entities.CompteEntity;
import com.wgt.imt.architecture.bdd.clients.repositories.entities.MouvementEntity;
import com.wgt.imt.architecture.bdd.common.model.mappers.AbstractBddMapper;
import com.wgt.imt.architecture.business.common.model.TypeCompteEnum;
import com.wgt.imt.architecture.business.comptes.model.Compte;
import com.wgt.imt.architecture.business.comptes.model.Mouvement;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Mapper pour convertir entre les objets métier Compte et les entités CompteEntity.
 *
 * @author Emmanuel WAGUET
 * @version 1.0
 */
@Service
@AllArgsConstructor
public class CompteBddMapper extends AbstractBddMapper<Compte, CompteEntity> {
    private final MouvementBddMapper mouvementMapper;

    /**
     * Convertit une entité CompteEntity en objet métier Compte.
     *
     * @param input l'entité de base de données à convertir
     * @return l'objet métier Compte correspondant
     */
    @Override
    public Compte from(final CompteEntity input) {
        return Compte.builder()
                .identifier(UUID.fromString(input.getIdentifier()))
                .name(input.getName())
                .solde(input.getSolde())
                .type(TypeCompteEnum.fromOrDefault(input.getType()))
                .historique(Objects.requireNonNullElse(input.getHistorique(), Collections.<MouvementEntity>emptySet()).stream()
                        .map(this.mouvementMapper::from)
                        .collect(Collectors.toList()))
                .build();
    }

    /**
     * Convertit un objet métier Compte en entité CompteEntity.
     *
     * @param object l'objet métier à convertir
     * @return l'entité de base de données correspondante
     */
    @Override
    public CompteEntity to(final Compte object) {
        return CompteEntity.builder()
                .identifier(object.getIdentifier().toString())
                .name(object.getName())
                .solde(object.getSolde())
                .type(object.getType().name())
                .historique(Objects.requireNonNullElse(object.getHistorique(), Collections.<Mouvement>emptySet()).stream()
                        .map(this.mouvementMapper::to)
                        .collect(Collectors.toList()))
                .build();
    }
}
