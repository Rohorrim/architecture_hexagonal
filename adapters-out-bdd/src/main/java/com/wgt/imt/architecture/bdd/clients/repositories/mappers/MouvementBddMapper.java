package com.wgt.imt.architecture.bdd.clients.repositories.mappers;

import com.wgt.imt.architecture.bdd.clients.repositories.entities.MouvementEntity;
import com.wgt.imt.architecture.bdd.common.model.mappers.AbstractBddMapper;
import com.wgt.imt.architecture.business.comptes.model.Mouvement;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Mapper entre un mouvement métier et son entité de persistance.
 */
@Service
@AllArgsConstructor
public class MouvementBddMapper extends AbstractBddMapper<Mouvement, MouvementEntity> {

    @Override
    public Mouvement from(final MouvementEntity input) {
        return Mouvement.builder()
                .identifier(UUID.fromString(input.getIdentifier()))
                .date(input.getDate())
                .message(input.getMessage())
                .build();
    }

    @Override
    public MouvementEntity to(final Mouvement object) {
        return MouvementEntity.builder()
                .identifier(object.getIdentifier().toString())
                .date(object.getDate())
                .message(object.getMessage())
                .build();
    }
}
