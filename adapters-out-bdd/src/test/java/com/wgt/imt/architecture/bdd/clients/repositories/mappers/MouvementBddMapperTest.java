package com.wgt.imt.architecture.bdd.clients.repositories.mappers;

import com.wgt.imt.architecture.bdd.clients.repositories.entities.MouvementEntity;
import com.wgt.imt.architecture.business.comptes.model.Mouvement;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MouvementBddMapperTest {
    private final MouvementBddMapper mapper = new MouvementBddMapper();

    @Test
    void from_shouldMapEntityToDomainMovement() {
        UUID identifier = UUID.randomUUID();
        LocalDateTime date = LocalDateTime.now();
        MouvementEntity entity = MouvementEntity.builder()
                .identifier(identifier.toString())
                .date(date)
                .message("Virement")
                .build();

        Mouvement result = mapper.from(entity);

        assertEquals(identifier, result.getIdentifier());
        assertEquals(date, result.getDate());
        assertEquals("Virement", result.getMessage());
    }

    @Test
    void to_shouldMapDomainMovementToEntity() {
        UUID identifier = UUID.randomUUID();
        LocalDateTime date = LocalDateTime.now();
        Mouvement mouvement = Mouvement.builder()
                .identifier(identifier)
                .date(date)
                .message("Prélèvement")
                .build();

        MouvementEntity result = mapper.to(mouvement);

        assertEquals(identifier.toString(), result.getIdentifier());
        assertEquals(date, result.getDate());
        assertEquals("Prélèvement", result.getMessage());
    }
}
