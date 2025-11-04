package com.wgt.imt.architecture.rest.clients;

import com.wgt.imt.architecture.business.clients.ClientsServiceValidator;
import com.wgt.imt.architecture.business.clients.model.Client;
import com.wgt.imt.architecture.business.common.exception.ImtException;
import com.wgt.imt.architecture.business.common.exception.NotFoundException;
import com.wgt.imt.architecture.business.common.model.GenreEnum;
import com.wgt.imt.architecture.rest.clients.model.input.ClientInput;
import com.wgt.imt.architecture.rest.clients.model.input.ClientUpdateInput;
import com.wgt.imt.architecture.rest.clients.model.output.ClientOutput;
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
 * Tests unitaires pour le contrôleur ClientsController.
 *
 * @author Test Generator
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class ClientsControllerTest {

    @Mock
    private ClientsServiceValidator service;

    @InjectMocks
    private ClientsController controller;

    private UUID testClientId;
    private Client testClient;
    private ClientInput testClientInput;
    private ClientUpdateInput testClientUpdateInput;

    @BeforeEach
    void setUp() {
        testClientId = UUID.randomUUID();
        testClient = createTestClient(testClientId);
        testClientInput = createTestClientInput();
        testClientUpdateInput = createTestClientUpdateInput();
    }

    /**
     * Test du endpoint GET /api/imt/v1/clients
     * Vérifie que tous les clients sont retournés correctement
     */
    @Test
    void getAll_ShouldReturnAllClients() {
        // Arrange
        Set<Client> clients = new HashSet<>();
        clients.add(testClient);
        clients.add(createTestClient(UUID.randomUUID()));

        when(service.getAll()).thenReturn(clients);

        // Act
        Collection<ClientOutput> result = controller.getAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(service, times(1)).getAll();
    }

    /**
     * Test du endpoint GET /api/imt/v1/clients quand aucun client n'existe
     * Vérifie qu'une collection vide est retournée
     */
    @Test
    void getAll_WhenNoClients_ShouldReturnEmptyCollection() {
        // Arrange
        when(service.getAll()).thenReturn(Collections.emptySet());

        // Act
        Collection<ClientOutput> result = controller.getAll();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(service, times(1)).getAll();
    }

    /**
     * Test du endpoint GET /api/imt/v1/clients quand le service retourne null
     * Vérifie qu'une collection vide est retournée au lieu de null
     */
    @Test
    void getAll_WhenServiceReturnsNull_ShouldReturnEmptyCollection() {
        // Arrange
        when(service.getAll()).thenReturn(null);

        // Act
        Collection<ClientOutput> result = controller.getAll();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(service, times(1)).getAll();
    }

    /**
     * Test du endpoint POST /api/imt/v1/clients
     * Vérifie qu'un nouveau client est créé avec succès
     */
    @Test
    void create_ShouldCreateClientSuccessfully() throws ImtException {
        // Arrange
        when(service.create(any(Client.class))).thenReturn(testClient);

        // Act
        ClientOutput result = controller.create(testClientInput);

        // Assert
        assertNotNull(result);
        verify(service, times(1)).create(any(Client.class));
    }

    /**
     * Test du endpoint POST /api/imt/v1/clients avec une exception
     * Vérifie que l'exception est propagée correctement
     */
    @Test
    void create_WhenServiceThrowsException_ShouldPropagateException() throws ImtException {
        // Arrange
        when(service.create(any(Client.class))).thenThrow(new NotFoundException("Erreur de création"));

        // Act & Assert
        assertThrows(NotFoundException.class, () -> controller.create(testClientInput));
        verify(service, times(1)).create(any(Client.class));
    }

    /**
     * Test du endpoint GET /api/imt/v1/clients/{idClient}
     * Vérifie qu'un client existant est retourné correctement
     */
    @Test
    void getOne_WhenClientExists_ShouldReturnClient() throws ImtException {
        // Arrange
        when(service.getOne(testClientId)).thenReturn(Optional.of(testClient));

        // Act
        ClientOutput result = controller.getOne(testClientId.toString());

        // Assert
        assertNotNull(result);
        verify(service, times(1)).getOne(testClientId);
    }

    /**
     * Test du endpoint GET /api/imt/v1/clients/{idClient}
     * Vérifie qu'une NotFoundException est levée si le client n'existe pas
     */
    @Test
    void getOne_WhenClientNotFound_ShouldThrowNotFoundException() {
        // Arrange
        when(service.getOne(testClientId)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> controller.getOne(testClientId.toString())
        );

        assertTrue(exception.getMessage().contains(testClientId.toString()));
        verify(service, times(1)).getOne(testClientId);
    }

    /**
     * Test du endpoint GET /api/imt/v1/clients/{idClient} avec un UUID invalide
     * Vérifie qu'une exception est levée pour un format d'UUID incorrect
     */
    @Test
    void getOne_WhenInvalidUUID_ShouldThrowIllegalArgumentException() {
        // Arrange
        String invalidUuid = "invalid-uuid-format";

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> controller.getOne(invalidUuid));
        verifyNoInteractions(service);
    }

    /**
     * Test du endpoint PATCH /api/imt/v1/clients/{idClient}
     * Vérifie qu'un client existant est mis à jour correctement
     */
    @Test
    void update_WhenClientExists_ShouldUpdateSuccessfully() throws ImtException {
        // Arrange
        when(service.getOne(testClientId)).thenReturn(Optional.of(testClient));
        doNothing().when(service).update(any(Client.class));

        // Act
        controller.update(testClientId.toString(), testClientUpdateInput);

        // Assert
        verify(service, times(1)).getOne(testClientId);
        verify(service, times(1)).update(any(Client.class));
    }

    /**
     * Test du endpoint PATCH /api/imt/v1/clients/{idClient}
     * Vérifie qu'une NotFoundException est levée si le client n'existe pas
     */
    @Test
    void update_WhenClientNotFound_ShouldThrowNotFoundException() {
        // Arrange
        when(service.getOne(testClientId)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> controller.update(testClientId.toString(), testClientUpdateInput)
        );

        assertTrue(exception.getMessage().contains(testClientId.toString()));
        verify(service, times(1)).getOne(testClientId);
    }

    /**
     * Test du endpoint PATCH /api/imt/v1/clients/{idClient} avec un UUID invalide
     * Vérifie qu'une exception est levée pour un format d'UUID incorrect
     */
    @Test
    void update_WhenInvalidUUID_ShouldThrowIllegalArgumentException() {
        // Arrange
        String invalidUuid = "invalid-uuid-format";

        // Act & Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> controller.update(invalidUuid, testClientUpdateInput)
        );
        verify(service, never()).getOne(any(UUID.class));
    }

    /**
     * Test du endpoint DELETE /api/imt/v1/clients/{idClient}
     * Vérifie qu'un client est supprimé avec succès
     */
    @Test
    void delete_ShouldDeleteClientSuccessfully() throws ImtException {
        // Arrange
        doNothing().when(service).delete(testClientId);

        // Act
        controller.delete(testClientId.toString());

        // Assert
        verify(service, times(1)).delete(testClientId);
    }

    /**
     * Test du endpoint DELETE /api/imt/v1/clients/{idClient} avec une exception
     * Vérifie que l'exception est propagée correctement
     */
    @Test
    void delete_WhenServiceThrowsException_ShouldPropagateException() throws ImtException {
        // Arrange
        doThrow(new NotFoundException("Erreur de suppression")).when(service).delete(testClientId);

        // Act & Assert
        assertThrows(NotFoundException.class, () -> controller.delete(testClientId.toString()));
        verify(service, times(1)).delete(testClientId);
    }

    /**
     * Test du endpoint DELETE /api/imt/v1/clients/{idClient} avec un UUID invalide
     * Vérifie qu'une exception est levée pour un format d'UUID incorrect
     */
    @Test
    void delete_WhenInvalidUUID_ShouldThrowIllegalArgumentException() {
        // Arrange
        String invalidUuid = "invalid-uuid-format";

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> controller.delete(invalidUuid));
        verifyNoInteractions(service);
    }

    // ========== Méthodes utilitaires pour créer des objets de test ==========

    /**
     * Crée un client de test
     */
    private Client createTestClient(UUID id) {
        return Client.builder()
                .identifier(id)
                .lastname("Dupont")
                .firstname("Jean")
                .genre(GenreEnum.HOMME)
                .build();
    }

    /**
     * Crée un ClientInput de test
     */
    private ClientInput createTestClientInput() {
        ClientInput input = new ClientInput();
        input.setLastname("Martin");
        input.setFirstname("Marie");
        input.setGenre("FEMME");
        return input;
    }

    /**
     * Crée un ClientUpdateInput de test
     */
    private ClientUpdateInput createTestClientUpdateInput() {
        ClientUpdateInput input = new ClientUpdateInput();
        input.setLastname("Bernard");
        input.setFirstname("Pierre");
        input.setGenre("HOMME");
        return input;
    }
}

