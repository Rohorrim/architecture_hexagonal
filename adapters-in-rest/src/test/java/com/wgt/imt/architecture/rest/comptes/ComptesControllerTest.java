package com.wgt.imt.architecture.rest.comptes;

import com.wgt.imt.architecture.business.common.exception.ImtException;
import com.wgt.imt.architecture.business.common.model.TypeCompteEnum;
import com.wgt.imt.architecture.business.comptes.ComptesServiceValidator;
import com.wgt.imt.architecture.business.comptes.model.Compte;
import com.wgt.imt.architecture.rest.comptes.model.input.CompteInput;
import com.wgt.imt.architecture.rest.comptes.model.input.CompteUpdateInput;
import com.wgt.imt.architecture.rest.comptes.model.output.ComptesOutput;
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
 * Tests unitaires pour le contrôleur ComptesController.
 * Teste les opérations CRUD sur les comptes bancaires d'un client.
 *
 * @author Test Generator
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class ComptesControllerTest {

    @Mock
    private ComptesServiceValidator service;

    @InjectMocks
    private ComptesController controller;

    private UUID testClientId;
    private UUID testCompteId;
    private Compte testCompte;
    private CompteInput testCompteInput;
    private CompteUpdateInput testCompteUpdateInput;

    @BeforeEach
    void setUp() {
        testClientId = UUID.randomUUID();
        testCompteId = UUID.randomUUID();
        testCompte = createTestCompte(testCompteId, testClientId);
        testCompteInput = createTestCompteInput();
        testCompteUpdateInput = createTestCompteUpdateInput();
    }

    // ==================== Tests GET /api/imt/v1/clients/{idClient}/comptes ====================

    /**
     * Test du endpoint GET /api/imt/v1/clients/{idClient}/comptes
     * Vérifie que tous les comptes d'un client sont retournés correctement
     */
    @Test
    void getAll_ShouldReturnAllComptesForClient() {
        // Arrange
        Set<Compte> comptes = new HashSet<>();
        comptes.add(testCompte);
        comptes.add(createTestCompte(UUID.randomUUID(), testClientId));

        when(service.getAllFilteredByClient(testClientId)).thenReturn(comptes);

        // Act
        Collection<ComptesOutput> result = controller.getAll(testClientId.toString());

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(service, times(1)).getAllFilteredByClient(testClientId);
    }

    /**
     * Test du endpoint GET /api/imt/v1/clients/{idClient}/comptes
     * Vérifie qu'une collection vide est retournée quand le client n'a pas de comptes
     */
    @Test
    void getAll_WhenNoComptes_ShouldReturnEmptyCollection() {
        // Arrange
        when(service.getAllFilteredByClient(testClientId)).thenReturn(Collections.emptySet());

        // Act
        Collection<ComptesOutput> result = controller.getAll(testClientId.toString());

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(service, times(1)).getAllFilteredByClient(testClientId);
    }

    /**
     * Test du endpoint GET /api/imt/v1/clients/{idClient}/comptes
     * Vérifie qu'une collection vide est retournée quand le service retourne null
     */
    @Test
    void getAll_WhenServiceReturnsNull_ShouldReturnEmptyCollection() {
        // Arrange
        when(service.getAllFilteredByClient(testClientId)).thenReturn(null);

        // Act
        Collection<ComptesOutput> result = controller.getAll(testClientId.toString());

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(service, times(1)).getAllFilteredByClient(testClientId);
    }

    /**
     * Test du endpoint GET /api/imt/v1/clients/{idClient}/comptes avec un UUID client invalide
     * Vérifie qu'une exception est levée pour un format d'UUID incorrect
     */
    @Test
    void getAll_WhenInvalidClientUUID_ShouldThrowIllegalArgumentException() {
        // Arrange
        String invalidUuid = "invalid-uuid-format";

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> controller.getAll(invalidUuid));
        verifyNoInteractions(service);
    }

    // ==================== Tests POST /api/imt/v1/clients/{idClient}/comptes ====================

    /**
     * Test du endpoint POST /api/imt/v1/clients/{idClient}/comptes
     * Vérifie qu'un nouveau compte est créé avec succès pour le client
     */
    @Test
    void create_ShouldCreateCompteSuccessfully() throws ImtException {
        // Arrange
        when(service.create(eq(testClientId), any(Compte.class))).thenReturn(testCompte);

        // Act
        ComptesOutput result = controller.create(testClientId.toString(), testCompteInput);

        // Assert
        assertNotNull(result);
        verify(service, times(1)).create(eq(testClientId), any(Compte.class));
    }

    /**
     * Test du endpoint POST /api/imt/v1/clients/{idClient}/comptes avec une exception
     * Vérifie que l'exception est propagée correctement
     */
    @Test
    void create_WhenServiceThrowsException_ShouldPropagateException() throws ImtException {
        // Arrange
        when(service.create(eq(testClientId), any(Compte.class))).thenThrow(new RuntimeException("Erreur de création"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> controller.create(testClientId.toString(), testCompteInput));
        verify(service, times(1)).create(eq(testClientId), any(Compte.class));
    }

    /**
     * Test du endpoint POST /api/imt/v1/clients/{idClient}/comptes avec un UUID client invalide
     * Vérifie qu'une exception est levée pour un format d'UUID incorrect
     */
    @Test
    void create_WhenInvalidClientUUID_ShouldThrowIllegalArgumentException() {
        // Arrange
        String invalidUuid = "invalid-uuid-format";

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> controller.create(invalidUuid, testCompteInput));
        verifyNoInteractions(service);
    }

    // ==================== Tests GET /api/imt/v1/clients/{idClient}/comptes/{idCompte} ====================

    /**
     * Test du endpoint GET /api/imt/v1/clients/{idClient}/comptes/{idCompte}
     * Vérifie qu'un compte existant est retourné correctement
     */
    @Test
    void getOne_WhenCompteExists_ShouldReturnCompte() {
        // Arrange
        when(service.getOneFilteredByClient(testClientId, testCompteId)).thenReturn(Optional.of(testCompte));

        // Act
        ComptesOutput result = controller.getOne(testClientId.toString(), testCompteId.toString());

        // Assert
        assertNotNull(result);
        verify(service, times(1)).getOneFilteredByClient(testClientId, testCompteId);
    }

    /**
     * Test du endpoint GET /api/imt/v1/clients/{idClient}/comptes/{idCompte}
     * Vérifie qu'une RuntimeException est levée si le compte n'existe pas
     */
    @Test
    void getOne_WhenCompteNotFound_ShouldThrowRuntimeException() {
        // Arrange
        when(service.getOneFilteredByClient(testClientId, testCompteId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> controller.getOne(testClientId.toString(), testCompteId.toString())
        );

        assertTrue(exception.getMessage().contains(testCompteId.toString()));
        assertTrue(exception.getMessage().contains(testClientId.toString()));
        verify(service, times(1)).getOneFilteredByClient(testClientId, testCompteId);
    }

    /**
     * Test du endpoint GET /api/imt/v1/clients/{idClient}/comptes/{idCompte} avec UUID client invalide
     * Vérifie qu'une exception est levée pour un format d'UUID client incorrect
     */
    @Test
    void getOne_WhenInvalidClientUUID_ShouldThrowIllegalArgumentException() {
        // Arrange
        String invalidUuid = "invalid-uuid-format";

        // Act & Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> controller.getOne(invalidUuid, testCompteId.toString())
        );
        verifyNoInteractions(service);
    }

    /**
     * Test du endpoint GET /api/imt/v1/clients/{idClient}/comptes/{idCompte} avec UUID compte invalide
     * Vérifie qu'une exception est levée pour un format d'UUID compte incorrect
     */
    @Test
    void getOne_WhenInvalidCompteUUID_ShouldThrowIllegalArgumentException() {
        // Arrange
        String invalidUuid = "invalid-uuid-format";

        // Act & Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> controller.getOne(testClientId.toString(), invalidUuid)
        );
        verifyNoInteractions(service);
    }

    // ==================== Tests PATCH /api/imt/v1/clients/{idClient}/comptes/{idCompte} ====================

    /**
     * Test du endpoint PATCH /api/imt/v1/clients/{idClient}/comptes/{idCompte}
     * Vérifie qu'un compte existant est mis à jour correctement
     */
    @Test
    void update_WhenCompteExists_ShouldUpdateSuccessfully() throws ImtException {
        // Arrange
        when(service.getOneFilteredByClient(testClientId, testCompteId)).thenReturn(Optional.of(testCompte));
        doNothing().when(service).update(eq(testClientId), any(Compte.class));

        // Act
        controller.update(testClientId.toString(), testCompteId.toString(), testCompteUpdateInput);

        // Assert
        verify(service, times(1)).getOneFilteredByClient(testClientId, testCompteId);
        verify(service, times(1)).update(eq(testClientId), any(Compte.class));
    }

    /**
     * Test du endpoint PATCH /api/imt/v1/clients/{idClient}/comptes/{idCompte}
     * Vérifie qu'une RuntimeException est levée si le compte n'existe pas
     */
    @Test
    void update_WhenCompteNotFound_ShouldThrowRuntimeException() {
        // Arrange
        when(service.getOneFilteredByClient(testClientId, testCompteId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> controller.update(testClientId.toString(), testCompteId.toString(), testCompteUpdateInput)
        );

        assertTrue(exception.getMessage().contains(testCompteId.toString()));
        assertTrue(exception.getMessage().contains(testClientId.toString()));
        verify(service, times(1)).getOneFilteredByClient(testClientId, testCompteId);
    }

    /**
     * Test du endpoint PATCH /api/imt/v1/clients/{idClient}/comptes/{idCompte} avec UUID client invalide
     * Vérifie qu'une exception est levée pour un format d'UUID client incorrect
     */
    @Test
    void update_WhenInvalidClientUUID_ShouldThrowIllegalArgumentException() {
        // Arrange
        String invalidUuid = "invalid-uuid-format";

        // Act & Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> controller.update(invalidUuid, testCompteId.toString(), testCompteUpdateInput)
        );
        verifyNoInteractions(service);
    }

    /**
     * Test du endpoint PATCH /api/imt/v1/clients/{idClient}/comptes/{idCompte} avec UUID compte invalide
     * Vérifie qu'une exception est levée pour un format d'UUID compte incorrect
     */
    @Test
    void update_WhenInvalidCompteUUID_ShouldThrowIllegalArgumentException() {
        // Arrange
        String invalidUuid = "invalid-uuid-format";

        // Act & Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> controller.update(testClientId.toString(), invalidUuid, testCompteUpdateInput)
        );
        verifyNoInteractions(service);
    }

    // ==================== Tests DELETE /api/imt/v1/clients/{idClient}/comptes/{idCompte} ====================

    /**
     * Test du endpoint DELETE /api/imt/v1/clients/{idClient}/comptes/{idCompte}
     * Vérifie qu'un compte est supprimé avec succès
     */
    @Test
    void delete_ShouldDeleteCompteSuccessfully() throws ImtException {
        // Arrange
        doNothing().when(service).delete(testClientId, testCompteId);

        // Act
        controller.delete(testClientId.toString(), testCompteId.toString());

        // Assert
        verify(service, times(1)).delete(testClientId, testCompteId);
    }

    /**
     * Test du endpoint DELETE /api/imt/v1/clients/{idClient}/comptes/{idCompte} avec une exception
     * Vérifie que l'exception est propagée correctement
     */
    @Test
    void delete_WhenServiceThrowsException_ShouldPropagateException() throws ImtException {
        // Arrange
        doThrow(new RuntimeException("Erreur de suppression")).when(service).delete(testClientId, testCompteId);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> controller.delete(testClientId.toString(), testCompteId.toString()));
        verify(service, times(1)).delete(testClientId, testCompteId);
    }

    /**
     * Test du endpoint DELETE /api/imt/v1/clients/{idClient}/comptes/{idCompte} avec UUID client invalide
     * Vérifie qu'une exception est levée pour un format d'UUID client incorrect
     */
    @Test
    void delete_WhenInvalidClientUUID_ShouldThrowIllegalArgumentException() {
        // Arrange
        String invalidUuid = "invalid-uuid-format";

        // Act & Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> controller.delete(invalidUuid, testCompteId.toString())
        );
        verifyNoInteractions(service);
    }

    /**
     * Test du endpoint DELETE /api/imt/v1/clients/{idClient}/comptes/{idCompte} avec UUID compte invalide
     * Vérifie qu'une exception est levée pour un format d'UUID compte incorrect
     */
    @Test
    void delete_WhenInvalidCompteUUID_ShouldThrowIllegalArgumentException() {
        // Arrange
        String invalidUuid = "invalid-uuid-format";

        // Act & Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> controller.delete(testClientId.toString(), invalidUuid)
        );
        verifyNoInteractions(service);
    }

    // ========== Méthodes utilitaires pour créer des objets de test ==========

    /**
     * Crée un compte de test
     */
    private Compte createTestCompte(UUID compteId, UUID clientId) {
        return Compte.builder()
                .identifier(compteId)
                .clientIdentifier(clientId)
                .type(TypeCompteEnum.COMPTE_COURANT)
                .name("Compte Test")
                .solde(1000.0)
                .build();
    }

    /**
     * Crée un CompteInput de test
     */
    private CompteInput createTestCompteInput() {
        CompteInput input = new CompteInput();
        input.setName("Nouveau Compte");
        input.setType(TypeCompteEnum.LIVRET_A.name());
        return input;
    }

    /**
     * Crée un CompteUpdateInput de test
     */
    private CompteUpdateInput createTestCompteUpdateInput() {
        CompteUpdateInput input = new CompteUpdateInput();
        input.setName("Compte Modifié");
        input.setType(TypeCompteEnum.LDDS.name());
        return input;
    }
}

