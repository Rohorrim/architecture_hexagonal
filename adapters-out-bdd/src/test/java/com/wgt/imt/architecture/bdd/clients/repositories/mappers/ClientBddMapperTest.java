package com.wgt.imt.architecture.bdd.clients.repositories.mappers;

import com.wgt.imt.architecture.bdd.clients.repositories.entities.ClientEntity;
import com.wgt.imt.architecture.bdd.clients.repositories.entities.CompteEntity;
import com.wgt.imt.architecture.business.clients.model.Client;
import com.wgt.imt.architecture.business.common.model.GenreEnum;
import com.wgt.imt.architecture.business.common.model.TypeCompteEnum;
import com.wgt.imt.architecture.business.comptes.model.Compte;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour la classe ClientBddMapper.
 * Teste les conversions entre Client (objet métier) et ClientEntity (entité BDD).
 * Utilise Mockito pour simuler le CompteBddMapper.
 *
 * @author Emmanuel WAGUET
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class ClientBddMapperTest {

    @Mock
    private CompteBddMapper compteMapper;

    @InjectMocks
    private ClientBddMapper clientMapper;

    private UUID testClientUuid;
    private String testClientUuidString;
    private UUID testCompteUuid;
    private String testCompteUuidString;

    @BeforeEach
    void setUp() {
        testClientUuid = UUID.randomUUID();
        testClientUuidString = testClientUuid.toString();
        testCompteUuid = UUID.randomUUID();
        testCompteUuidString = testCompteUuid.toString();
    }

    @Test
    void from_shouldConvertEntityToBusinessObject_whenEntityHasNoComptes() {
        // Given
        ClientEntity entity = ClientEntity.builder()
                .identifier(testClientUuidString)
                .lastname("Dupont")
                .firstname("Jean")
                .genre("HOMME")
                .comptes(Collections.emptySet())
                .build();

        // When
        Client result = clientMapper.from(entity);

        // Then
        assertNotNull(result);
        assertEquals(testClientUuid, result.getIdentifier());
        assertEquals("Dupont", result.getLastname());
        assertEquals("Jean", result.getFirstname());
        assertEquals(GenreEnum.HOMME, result.getGenre());
        assertNotNull(result.getComptes());
        assertTrue(result.getComptes().isEmpty());
        verify(compteMapper, never()).from(any(CompteEntity.class));
    }

    @Test
    void from_shouldConvertEntityToBusinessObject_whenEntityHasComptes() {
        // Given
        CompteEntity compteEntity1 = CompteEntity.builder()
                .identifier(testCompteUuidString)
                .name("Compte Courant")
                .solde(1000.0)
                .type("COMPTE_COURANT")
                .build();

        CompteEntity compteEntity2 = CompteEntity.builder()
                .identifier(UUID.randomUUID().toString())
                .name("Compte Épargne")
                .solde(5000.0)
                .type("LDDS")
                .build();

        Set<CompteEntity> compteEntities = new HashSet<>(Arrays.asList(compteEntity1, compteEntity2));

        ClientEntity entity = ClientEntity.builder()
                .identifier(testClientUuidString)
                .lastname("Martin")
                .firstname("Marie")
                .genre("FEMME")
                .comptes(compteEntities)
                .build();

        Compte compte1 = Compte.builder()
                .identifier(UUID.fromString(compteEntity1.getIdentifier()))
                .name("Compte Courant")
                .solde(1000.0)
                .type(TypeCompteEnum.COMPTE_COURANT)
                .build();

        Compte compte2 = Compte.builder()
                .identifier(UUID.fromString(compteEntity2.getIdentifier()))
                .name("Compte Épargne")
                .solde(5000.0)
                .type(TypeCompteEnum.LDDS)
                .build();

        when(compteMapper.from(compteEntity1)).thenReturn(compte1);
        when(compteMapper.from(compteEntity2)).thenReturn(compte2);

        // When
        Client result = clientMapper.from(entity);

        // Then
        assertNotNull(result);
        assertEquals(testClientUuid, result.getIdentifier());
        assertEquals("Martin", result.getLastname());
        assertEquals("Marie", result.getFirstname());
        assertEquals(GenreEnum.FEMME, result.getGenre());
        assertNotNull(result.getComptes());
        assertEquals(2, result.getComptes().size());
        assertTrue(result.getComptes().contains(compte1));
        assertTrue(result.getComptes().contains(compte2));
        verify(compteMapper, times(2)).from(any(CompteEntity.class));
    }

    @Test
    void from_shouldHandleNullComptes_whenComptesIsNull() {
        // Given
        ClientEntity entity = ClientEntity.builder()
                .identifier(testClientUuidString)
                .lastname("Durand")
                .firstname("Pierre")
                .genre("HOMME")
                .comptes(null)
                .build();

        // When
        Client result = clientMapper.from(entity);

        // Then
        assertNotNull(result);
        assertEquals(testClientUuid, result.getIdentifier());
        assertNotNull(result.getComptes());
        assertTrue(result.getComptes().isEmpty());
        verify(compteMapper, never()).from(any(CompteEntity.class));
    }

    @Test
    void from_shouldHandleDefaultGenre_whenGenreIsInvalid() {
        // Given
        ClientEntity entity = ClientEntity.builder()
                .identifier(testClientUuidString)
                .lastname("Dupont")
                .firstname("Jean")
                .genre("INVALID_GENRE")
                .comptes(Collections.emptySet())
                .build();

        // When
        Client result = clientMapper.from(entity);

        // Then
        assertNotNull(result);
        assertNotNull(result.getGenre());
        // Le genre devrait être la valeur par défaut retournée par fromOrDefault
    }

    @Test
    void from_shouldHandleNullGenre_whenGenreIsNull() {
        // Given
        ClientEntity entity = ClientEntity.builder()
                .identifier(testClientUuidString)
                .lastname("Dupont")
                .firstname("Jean")
                .genre(null)
                .comptes(Collections.emptySet())
                .build();

        // When
        Client result = clientMapper.from(entity);

        // Then
        assertNotNull(result);
        assertNotNull(result.getGenre());
    }

    @Test
    void from_shouldConvertAllGenreEnumValues() {
        // Test pour HOMME
        ClientEntity entityMasculin = ClientEntity.builder()
                .identifier(testClientUuidString)
                .lastname("Dupont")
                .firstname("Jean")
                .genre("HOMME")
                .comptes(Collections.emptySet())
                .build();

        Client clientMasculin = clientMapper.from(entityMasculin);
        assertEquals(GenreEnum.HOMME, clientMasculin.getGenre());

        // Test pour FEMME
        ClientEntity entityFeminin = ClientEntity.builder()
                .identifier(testClientUuidString)
                .lastname("Martin")
                .firstname("Marie")
                .genre("FEMME")
                .comptes(Collections.emptySet())
                .build();

        Client clientFeminin = clientMapper.from(entityFeminin);
        assertEquals(GenreEnum.FEMME, clientFeminin.getGenre());
    }

    @Test
    void to_shouldConvertBusinessObjectToEntity_whenClientHasNoComptes() {
        // Given
        Client client = Client.builder()
                .identifier(testClientUuid)
                .lastname("Dubois")
                .firstname("Sophie")
                .genre(GenreEnum.FEMME)
                .comptes(Collections.emptySet())
                .build();

        // When
        ClientEntity result = clientMapper.to(client);

        // Then
        assertNotNull(result);
        assertEquals(testClientUuidString, result.getIdentifier());
        assertEquals("Dubois", result.getLastname());
        assertEquals("Sophie", result.getFirstname());
        assertEquals("FEMME", result.getGenre());
        assertNotNull(result.getComptes());
        assertTrue(result.getComptes().isEmpty());
        verify(compteMapper, never()).to(any(Compte.class));
    }

    @Test
    void to_shouldConvertBusinessObjectToEntity_whenClientHasComptes() {
        // Given
        Compte compte1 = Compte.builder()
                .identifier(testCompteUuid)
                .name("Compte Courant")
                .solde(2000.0)
                .type(TypeCompteEnum.COMPTE_COURANT)
                .build();

        Compte compte2 = Compte.builder()
                .identifier(UUID.randomUUID())
                .name("Livret A")
                .solde(10000.0)
                .type(TypeCompteEnum.LDDS)
                .build();

        Set<Compte> comptes = new HashSet<>(Arrays.asList(compte1, compte2));

        Client client = Client.builder()
                .identifier(testClientUuid)
                .lastname("Bernard")
                .firstname("Luc")
                .genre(GenreEnum.HOMME)
                .comptes(comptes)
                .build();

        CompteEntity compteEntity1 = CompteEntity.builder()
                .identifier(testCompteUuidString)
                .name("Compte Courant")
                .solde(2000.0)
                .type("COMPTE_COURANT")
                .build();

        CompteEntity compteEntity2 = CompteEntity.builder()
                .identifier(compte2.getIdentifier().toString())
                .name("Livret A")
                .solde(10000.0)
                .type("LDDS")
                .build();

        when(compteMapper.to(compte1)).thenReturn(compteEntity1);
        when(compteMapper.to(compte2)).thenReturn(compteEntity2);

        // When
        ClientEntity result = clientMapper.to(client);

        // Then
        assertNotNull(result);
        assertEquals(testClientUuidString, result.getIdentifier());
        assertEquals("Bernard", result.getLastname());
        assertEquals("Luc", result.getFirstname());
        assertEquals("HOMME", result.getGenre());
        assertNotNull(result.getComptes());
        assertEquals(2, result.getComptes().size());
        assertTrue(result.getComptes().contains(compteEntity1));
        assertTrue(result.getComptes().contains(compteEntity2));
        verify(compteMapper, times(2)).to(any(Compte.class));
    }

    @Test
    void to_shouldHandleNullComptes_whenComptesIsNull() {
        // Given
        Client client = Client.builder()
                .identifier(testClientUuid)
                .lastname("Petit")
                .firstname("Marc")
                .genre(GenreEnum.HOMME)
                .comptes(null)
                .build();

        // When
        ClientEntity result = clientMapper.to(client);

        // Then
        assertNotNull(result);
        assertEquals(testClientUuidString, result.getIdentifier());
        assertNotNull(result.getComptes());
        assertTrue(result.getComptes().isEmpty());
        verify(compteMapper, never()).to(any(Compte.class));
    }

    @Test
    void to_shouldConvertAllGenreEnumValues() {
        // Test pour HOMME
        Client clientMasculin = Client.builder()
                .identifier(testClientUuid)
                .lastname("Dupont")
                .firstname("Jean")
                .genre(GenreEnum.HOMME)
                .comptes(Collections.emptySet())
                .build();

        ClientEntity entityMasculin = clientMapper.to(clientMasculin);
        assertEquals("HOMME", entityMasculin.getGenre());

        // Test pour FEMME
        Client clientFeminin = Client.builder()
                .identifier(testClientUuid)
                .lastname("Martin")
                .firstname("Marie")
                .genre(GenreEnum.FEMME)
                .comptes(Collections.emptySet())
                .build();

        ClientEntity entityFeminin = clientMapper.to(clientFeminin);
        assertEquals("FEMME", entityFeminin.getGenre());
    }

    @Test
    void fromAndTo_shouldBeReversible_whenClientHasNoComptes() {
        // Given
        Client originalClient = Client.builder()
                .identifier(testClientUuid)
                .lastname("Roux")
                .firstname("Anne")
                .genre(GenreEnum.FEMME)
                .comptes(Collections.emptySet())
                .build();

        // When
        ClientEntity entity = clientMapper.to(originalClient);
        Client resultClient = clientMapper.from(entity);

        // Then
        assertNotNull(resultClient);
        assertEquals(originalClient.getIdentifier(), resultClient.getIdentifier());
        assertEquals(originalClient.getLastname(), resultClient.getLastname());
        assertEquals(originalClient.getFirstname(), resultClient.getFirstname());
        assertEquals(originalClient.getGenre(), resultClient.getGenre());
        assertTrue(resultClient.getComptes().isEmpty());
    }

    @Test
    void toAndFrom_shouldBeReversible_whenEntityHasNoComptes() {
        // Given
        ClientEntity originalEntity = ClientEntity.builder()
                .identifier(testClientUuidString)
                .lastname("Moreau")
                .firstname("Paul")
                .genre("HOMME")
                .comptes(Collections.emptySet())
                .build();

        // When
        Client client = clientMapper.from(originalEntity);
        ClientEntity resultEntity = clientMapper.to(client);

        // Then
        assertNotNull(resultEntity);
        assertEquals(originalEntity.getIdentifier(), resultEntity.getIdentifier());
        assertEquals(originalEntity.getLastname(), resultEntity.getLastname());
        assertEquals(originalEntity.getFirstname(), resultEntity.getFirstname());
        assertEquals(originalEntity.getGenre(), resultEntity.getGenre());
        assertTrue(resultEntity.getComptes().isEmpty());
    }
}

