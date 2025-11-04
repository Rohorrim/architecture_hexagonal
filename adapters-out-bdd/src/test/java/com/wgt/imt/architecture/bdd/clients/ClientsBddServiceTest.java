package com.wgt.imt.architecture.bdd.clients;

import com.wgt.imt.architecture.bdd.clients.repositories.ClientRepository;
import com.wgt.imt.architecture.bdd.clients.repositories.entities.ClientEntity;
import com.wgt.imt.architecture.bdd.clients.repositories.mappers.ClientBddMapper;
import com.wgt.imt.architecture.business.clients.model.Client;
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
 * Tests unitaires pour la classe ClientsBddService.
 * Utilise Mockito pour simuler les dépendances (repository et mapper).
 *
 * @author Emmanuel WAGUET
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class ClientsBddServiceTest {

    @Mock
    private ClientRepository repository;

    @Mock
    private ClientBddMapper mapper;

    @InjectMocks
    private ClientsBddService service;

    private UUID testUuid;
    private String testUuidString;
    private Client testClient;
    private ClientEntity testClientEntity;

    @BeforeEach
    void setUp() {
        testUuid = UUID.randomUUID();
        testUuidString = testUuid.toString();

        testClient = Client.builder()
                .identifier(testUuid)
                .lastname("Dupont")
                .firstname("Jean")
                .build();

        testClientEntity = ClientEntity.builder()
                .identifier(testUuidString)
                .lastname("Dupont")
                .firstname("Jean")
                .build();
    }

    @Test
    void exist_shouldReturnTrue_whenClientExists() {
        // Given
        when(repository.existsById(testUuidString)).thenReturn(true);

        // When
        boolean result = service.exist(testUuid);

        // Then
        assertTrue(result);
        verify(repository).existsById(testUuidString);
    }

    @Test
    void exist_shouldReturnFalse_whenClientDoesNotExist() {
        // Given
        when(repository.existsById(testUuidString)).thenReturn(false);

        // When
        boolean result = service.exist(testUuid);

        // Then
        assertFalse(result);
        verify(repository).existsById(testUuidString);
    }

    @Test
    void exist_shouldReturnFalse_whenIdentifierIsNull() {
        // When
        boolean result = service.exist(null);

        // Then
        assertFalse(result);
        verify(repository, never()).existsById(any());
    }

    @Test
    void getAll_shouldReturnAllClients_whenClientsExist() {
        // Given
        ClientEntity entity1 = ClientEntity.builder()
                .identifier(UUID.randomUUID().toString())
                .lastname("Dupont")
                .firstname("Jean")
                .build();
        ClientEntity entity2 = ClientEntity.builder()
                .identifier(UUID.randomUUID().toString())
                .lastname("Martin")
                .firstname("Marie")
                .build();

        Client client1 = Client.builder()
                .identifier(UUID.fromString(entity1.getIdentifier()))
                .lastname("Dupont")
                .firstname("Jean")
                .build();
        Client client2 = Client.builder()
                .identifier(UUID.fromString(entity2.getIdentifier()))
                .lastname("Martin")
                .firstname("Marie")
                .build();

        List<ClientEntity> entities = Arrays.asList(entity1, entity2);

        when(repository.findAll()).thenReturn(entities);
        when(mapper.from(entity1)).thenReturn(client1);
        when(mapper.from(entity2)).thenReturn(client2);

        // When
        Collection<Client> result = service.getAll();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(client1));
        assertTrue(result.contains(client2));
        verify(repository).findAll();
        verify(mapper, times(2)).from(any(ClientEntity.class));
    }

    @Test
    void getAll_shouldReturnEmptyCollection_whenNoClientsExist() {
        // Given
        when(repository.findAll()).thenReturn(Collections.emptyList());

        // When
        Collection<Client> result = service.getAll();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(repository).findAll();
        verify(mapper, never()).from(any(ClientEntity.class));
    }

    @Test
    void getAll_shouldReturnEmptyCollection_whenRepositoryReturnsNull() {
        // Given
        when(repository.findAll()).thenReturn(null);

        // When
        Collection<Client> result = service.getAll();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(repository).findAll();
        verify(mapper, never()).from(any(ClientEntity.class));
    }

    @Test
    void get_shouldReturnClient_whenClientExists() {
        // Given
        when(repository.findById(testUuidString)).thenReturn(Optional.of(testClientEntity));
        when(mapper.from(testClientEntity)).thenReturn(testClient);

        // When
        Optional<Client> result = service.get(testUuid);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testClient, result.get());
        verify(repository).findById(testUuidString);
        verify(mapper).from(testClientEntity);
    }

    @Test
    void get_shouldReturnEmpty_whenClientDoesNotExist() {
        // Given
        when(repository.findById(testUuidString)).thenReturn(Optional.empty());

        // When
        Optional<Client> result = service.get(testUuid);

        // Then
        assertFalse(result.isPresent());
        verify(repository).findById(testUuidString);
        verify(mapper, never()).from(any(ClientEntity.class));
    }

    @Test
    void get_shouldReturnEmpty_whenIdentifierIsNull() {
        // When
        Optional<Client> result = service.get(null);

        // Then
        assertFalse(result.isPresent());
        verify(repository, never()).findById(any());
        verify(mapper, never()).from(any(ClientEntity.class));
    }

    @Test
    void save_shouldSaveAndReturnClient_whenClientIsValid() {
        // Given
        when(mapper.to(testClient)).thenReturn(testClientEntity);
        when(repository.save(testClientEntity)).thenReturn(testClientEntity);
        when(mapper.from(testClientEntity)).thenReturn(testClient);

        // When
        Client result = service.save(testClient);

        // Then
        assertNotNull(result);
        assertEquals(testClient, result);
        verify(mapper).to(testClient);
        verify(repository).save(testClientEntity);
        verify(mapper).from(testClientEntity);
    }

    @Test
    void save_shouldThrowException_whenClientIsNull() {
        // When & Then
        NullPointerException exception = assertThrows(NullPointerException.class, () -> {
            service.save(null);
        });

        assertEquals("Impossible de sauvegarder un client nul", exception.getMessage());
        verify(mapper, never()).to(any());
        verify(repository, never()).save(any());
    }

    @Test
    void delete_shouldDeleteClient_whenIdentifierIsValid() {
        // When
        service.delete(testUuid);

        // Then
        verify(repository).deleteById(testUuidString);
    }

    @Test
    void delete_shouldNotDeleteClient_whenIdentifierIsNull() {
        // When
        service.delete(null);

        // Then
        verify(repository, never()).deleteById(any());
    }
}

