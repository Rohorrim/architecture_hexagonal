package com.wgt.imt.architecture.bdd.clients.repositories.mappers;

import com.wgt.imt.architecture.bdd.clients.repositories.entities.CompteEntity;
import com.wgt.imt.architecture.bdd.clients.repositories.entities.MouvementEntity;
import com.wgt.imt.architecture.business.common.model.TypeCompteEnum;
import com.wgt.imt.architecture.business.comptes.model.Compte;
import com.wgt.imt.architecture.business.comptes.model.Mouvement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests unitaires pour la classe CompteBddMapper.
 * Teste les conversions entre Compte (objet métier) et CompteEntity (entité BDD).
 *
 * @author Emmanuel WAGUET
 * @version 1.0
 */
class CompteBddMapperTest {

    private CompteBddMapper mapper;

    private UUID testUuid;
    private String testUuidString;
    private String testName;
    private Double testSolde;

    @BeforeEach
    void setUp() {
        mapper = new CompteBddMapper(new MouvementBddMapper());
        testUuid = UUID.randomUUID();
        testUuidString = testUuid.toString();
        testName = "Compte Courant";
        testSolde = 1500.50;
    }

    @Test
    void from_shouldConvertEntityToBusinessObject_whenEntityIsValid() {
        // Given
        CompteEntity entity = CompteEntity.builder()
                .identifier(testUuidString)
                .name(testName)
                .solde(testSolde)
                .type("COMPTE_COURANT")
                .build();

        // When
        Compte result = mapper.from(entity);

        // Then
        assertNotNull(result);
        assertEquals(testUuid, result.getIdentifier());
        assertEquals(testName, result.getName());
        assertEquals(testSolde, result.getSolde());
        assertEquals(TypeCompteEnum.COMPTE_COURANT, result.getType());
    }

    @Test
    void from_shouldHandleDefaultTypeEnum_whenTypeIsInvalid() {
        // Given
        CompteEntity entity = CompteEntity.builder()
                .identifier(testUuidString)
                .name(testName)
                .solde(testSolde)
                .type("INVALID_TYPE")
                .build();

        // When
        Compte result = mapper.from(entity);

        // Then
        assertNotNull(result);
        assertEquals(testUuid, result.getIdentifier());
        assertEquals(testName, result.getName());
        assertEquals(testSolde, result.getSolde());
        // Le type devrait être la valeur par défaut retournée par fromOrDefault
        assertNotNull(result.getType());
    }

    @Test
    void from_shouldHandleNullType_whenTypeIsNull() {
        // Given
        CompteEntity entity = CompteEntity.builder()
                .identifier(testUuidString)
                .name(testName)
                .solde(testSolde)
                .type(null)
                .build();

        // When
        Compte result = mapper.from(entity);

        // Then
        assertNotNull(result);
        assertEquals(testUuid, result.getIdentifier());
        assertNotNull(result.getType());
    }

    @Test
    void from_shouldConvertAllTypeCompteEnumValues() {
        // Test pour COURANT
        CompteEntity entityCourant = CompteEntity.builder()
                .identifier(testUuidString)
                .name(testName)
                .solde(testSolde)
                .type("COMPTE_COURANT")
                .build();

        Compte compteCourant = mapper.from(entityCourant);
        assertEquals(TypeCompteEnum.COMPTE_COURANT, compteCourant.getType());

        // Test pour EPARGNE
        CompteEntity entityEpargne = CompteEntity.builder()
                .identifier(testUuidString)
                .name(testName)
                .solde(testSolde)
                .type("LDDS")
                .build();

        Compte compteEpargne = mapper.from(entityEpargne);
        assertEquals(TypeCompteEnum.LDDS, compteEpargne.getType());
    }

    @Test
    void to_shouldConvertBusinessObjectToEntity_whenObjectIsValid() {
        // Given
        Compte compte = Compte.builder()
                .identifier(testUuid)
                .name(testName)
                .solde(testSolde)
                .type(TypeCompteEnum.LDDS)
                .build();

        // When
        CompteEntity result = mapper.to(compte);

        // Then
        assertNotNull(result);
        assertEquals(testUuidString, result.getIdentifier());
        assertEquals(testName, result.getName());
        assertEquals(testSolde, result.getSolde());
        assertEquals("LDDS", result.getType());
    }

    @Test
    void to_shouldConvertAllTypeCompteEnumValues() {
        // Test pour COURANT
        Compte compteCourant = Compte.builder()
                .identifier(testUuid)
                .name(testName)
                .solde(testSolde)
                .type(TypeCompteEnum.COMPTE_COURANT)
                .build();

        CompteEntity entityCourant = mapper.to(compteCourant);
        assertEquals("COMPTE_COURANT", entityCourant.getType());

        // Test pour EPARGNE
        Compte compteEpargne = Compte.builder()
                .identifier(testUuid)
                .name(testName)
                .solde(testSolde)
                .type(TypeCompteEnum.LDDS)
                .build();

        CompteEntity entityEpargne = mapper.to(compteEpargne);
        assertEquals("LDDS", entityEpargne.getType());
    }

    @Test
    void to_shouldHandleZeroSolde() {
        // Given
        Compte compte = Compte.builder()
                .identifier(testUuid)
                .name(testName)
                .solde(0.0)
                .type(TypeCompteEnum.COMPTE_COURANT)
                .build();

        // When
        CompteEntity result = mapper.to(compte);

        // Then
        assertNotNull(result);
        assertEquals(0.0, result.getSolde());
    }

    @Test
    void to_shouldHandleNegativeSolde() {
        // Given
        Compte compte = Compte.builder()
                .identifier(testUuid)
                .name(testName)
                .solde(-100.50)
                .type(TypeCompteEnum.COMPTE_COURANT)
                .build();

        // When
        CompteEntity result = mapper.to(compte);

        // Then
        assertNotNull(result);
        assertEquals(-100.50, result.getSolde());
    }

    @Test
    void fromAndTo_shouldBeReversible() {
        // Given
        Compte originalCompte = Compte.builder()
                .identifier(testUuid)
                .name(testName)
                .solde(testSolde)
                .type(TypeCompteEnum.COMPTE_COURANT)
                .build();

        // When
        CompteEntity entity = mapper.to(originalCompte);
        Compte resultCompte = mapper.from(entity);

        // Then
        assertNotNull(resultCompte);
        assertEquals(originalCompte.getIdentifier(), resultCompte.getIdentifier());
        assertEquals(originalCompte.getName(), resultCompte.getName());
        assertEquals(originalCompte.getSolde(), resultCompte.getSolde());
        assertEquals(originalCompte.getType(), resultCompte.getType());
    }

    @Test
    void toAndFrom_shouldBeReversible() {
        // Given
        CompteEntity originalEntity = CompteEntity.builder()
                .identifier(testUuidString)
                .name(testName)
                .solde(testSolde)
                .type("LDDS")
                .build();

        // When
        Compte compte = mapper.from(originalEntity);
        CompteEntity resultEntity = mapper.to(compte);

        // Then
        assertNotNull(resultEntity);
        assertEquals(originalEntity.getIdentifier(), resultEntity.getIdentifier());
        assertEquals(originalEntity.getName(), resultEntity.getName());
        assertEquals(originalEntity.getSolde(), resultEntity.getSolde());
        assertEquals(originalEntity.getType(), resultEntity.getType());
    }

    @Test
    void fromAndTo_shouldPreserveAccountHistory() {
        UUID mouvementIdentifier = UUID.randomUUID();
        LocalDateTime date = LocalDateTime.now();
        CompteEntity entity = CompteEntity.builder()
                .identifier(testUuidString)
                .name(testName)
                .solde(testSolde)
                .type("COMPTE_COURANT")
                .historique(List.of(MouvementEntity.builder()
                        .identifier(mouvementIdentifier.toString())
                        .date(date)
                        .message("Dépôt")
                        .build()))
                .build();

        Compte compte = mapper.from(entity);
        CompteEntity mappedEntity = mapper.to(compte);

        assertEquals(1, compte.getHistorique().size());
        assertEquals(mouvementIdentifier, compte.getHistorique().iterator().next().getIdentifier());
        assertEquals(date, compte.getHistorique().iterator().next().getDate());
        assertEquals("Dépôt", mappedEntity.getHistorique().iterator().next().getMessage());
    }

    @Test
    void to_shouldMapDomainHistoryToDatabaseEntity() {
        UUID mouvementIdentifier = UUID.randomUUID();
        LocalDateTime date = LocalDateTime.now();
        Compte compte = Compte.builder()
                .identifier(testUuid)
                .name(testName)
                .solde(testSolde)
                .type(TypeCompteEnum.COMPTE_COURANT)
                .historique(List.of(Mouvement.builder()
                        .identifier(mouvementIdentifier)
                        .date(date)
                        .message("Retrait")
                        .build()))
                .build();

        CompteEntity entity = mapper.to(compte);

        assertEquals(mouvementIdentifier.toString(), entity.getHistorique().iterator().next().getIdentifier());
        assertEquals(date, entity.getHistorique().iterator().next().getDate());
        assertEquals("Retrait", entity.getHistorique().iterator().next().getMessage());
    }
}
