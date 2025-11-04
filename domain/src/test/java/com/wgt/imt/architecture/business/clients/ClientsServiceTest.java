package com.wgt.imt.architecture.business.clients;

import com.wgt.imt.architecture.business.clients.model.Client;
import com.wgt.imt.architecture.business.common.exception.ImtException;
import com.wgt.imt.architecture.business.common.model.GenreEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour la classe ClientsService.
 * Vérifie le bon fonctionnement des opérations CRUD sur les clients.
 *
 * @author Emmanuel WAGUET
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ClientsService - Tests unitaires")
class ClientsServiceTest {

    @Mock
    private ClientStorageProvider service;

    @InjectMocks
    private ClientsService clientsService;

    private Client clientTest;
    private UUID testUuid;

    @BeforeEach
    void setUp() {
        testUuid = UUID.randomUUID();
        clientTest = Client.builder()
                .lastname("Dupont")
                .firstname("Jean")
                .genre(GenreEnum.HOMME)
                .comptes(Collections.emptySet())
                .build();
    }

    @Nested
    @DisplayName("getAll() - Récupération de tous les clients")
    class GetAllTests {

        @Test
        @DisplayName("Doit retourner une collection de clients quand des clients existent")
        void shouldReturnClientsWhenClientsExist() {
            // Given
            Client client2 = Client.builder()
                    .lastname("Martin")
                    .firstname("Marie")
                    .genre(GenreEnum.FEMME)
                    .build();
            Collection<Client> expectedClients = Arrays.asList(clientTest, client2);
            when(service.getAll()).thenReturn(expectedClients);

            // When
            Collection<Client> result = clientsService.getAll();

            // Then
            assertThat(result)
                    .isNotNull()
                    .hasSize(2)
                    .containsExactlyInAnyOrderElementsOf(expectedClients);
            verify(service, times(1)).getAll();
        }

        @Test
        @DisplayName("Doit retourner une collection vide quand aucun client n'existe")
        void shouldReturnEmptyCollectionWhenNoClientsExist() {
            // Given
            when(service.getAll()).thenReturn(Collections.emptyList());

            // When
            Collection<Client> result = clientsService.getAll();

            // Then
            assertThat(result)
                    .isNotNull()
                    .isEmpty();
            verify(service, times(1)).getAll();
        }

        @Test
        @DisplayName("Doit retourner une collection vide quand le storage provider retourne null")
        void shouldReturnEmptyCollectionWhenStorageProviderReturnsNull() {
            // Given
            when(service.getAll()).thenReturn(null);

            // When
            Collection<Client> result = clientsService.getAll();

            // Then
            assertThat(result)
                    .isNotNull()
                    .isEmpty();
            verify(service, times(1)).getAll();
        }
    }

    @Nested
    @DisplayName("getOne() - Récupération d'un client par identifiant")
    class GetOneTests {

        @Test
        @DisplayName("Doit retourner le client quand il existe")
        void shouldReturnClientWhenExists() {
            // Given
            UUID clientId = clientTest.getIdentifier();
            when(service.get(clientId)).thenReturn(Optional.of(clientTest));

            // When
            Optional<Client> result = clientsService.getOne(clientId);

            // Then
            assertThat(result)
                    .isPresent()
                    .contains(clientTest);
            verify(service, times(1)).get(clientId);
        }

        @Test
        @DisplayName("Doit retourner Optional.empty() quand le client n'existe pas")
        void shouldReturnEmptyOptionalWhenClientDoesNotExist() {
            // Given
            when(service.get(testUuid)).thenReturn(Optional.empty());

            // When
            Optional<Client> result = clientsService.getOne(testUuid);

            // Then
            assertThat(result).isEmpty();
            verify(service, times(1)).get(testUuid);
        }

        @Test
        @DisplayName("Doit retourner Optional.empty() pour un UUID différent")
        void shouldReturnEmptyOptionalForDifferentUuid() {
            // Given
            UUID differentUuid = UUID.randomUUID();
            when(service.get(differentUuid)).thenReturn(Optional.empty());

            // When
            Optional<Client> result = clientsService.getOne(differentUuid);

            // Then
            assertThat(result).isEmpty();
            verify(service, times(1)).get(differentUuid);
        }
    }

    @Nested
    @DisplayName("create() - Création d'un nouveau client")
    class CreateTests {

        @Test
        @DisplayName("Doit créer et retourner le client avec succès")
        void shouldCreateAndReturnClient() throws ImtException {
            // Given
            Client newClient = Client.builder()
                    .lastname("Durand")
                    .firstname("Pierre")
                    .genre(GenreEnum.HOMME)
                    .build();
            when(service.save(newClient)).thenReturn(newClient);

            // When
            Client result = clientsService.create(newClient);

            // Then
            assertThat(result)
                    .isNotNull()
                    .isEqualTo(newClient);
            verify(service, times(1)).save(newClient);
        }

        @Test
        @DisplayName("Doit créer un client avec tous les genres possibles")
        void shouldCreateClientWithDifferentGenres() throws ImtException {
            // Test pour HOMME
            Client clientHomme = Client.builder()
                    .lastname("Test")
                    .firstname("Test")
                    .genre(GenreEnum.HOMME)
                    .build();
            when(service.save(clientHomme)).thenReturn(clientHomme);

            Client resultHomme = clientsService.create(clientHomme);
            assertThat(resultHomme.getGenre()).isEqualTo(GenreEnum.HOMME);

            // Test pour FEMME
            Client clientFemme = Client.builder()
                    .lastname("Test")
                    .firstname("Test")
                    .genre(GenreEnum.FEMME)
                    .build();
            when(service.save(clientFemme)).thenReturn(clientFemme);

            Client resultFemme = clientsService.create(clientFemme);
            assertThat(resultFemme.getGenre()).isEqualTo(GenreEnum.FEMME);

            // Vérification finale
            verify(service, times(2)).save(any(Client.class));
        }
    }

    @Nested
    @DisplayName("update() - Mise à jour d'un client existant")
    class UpdateTests {

        @Test
        @DisplayName("Doit mettre à jour le client avec succès")
        void shouldUpdateClientSuccessfully() throws ImtException {
            // Given
            Client updatedClient = clientTest.toBuilder()
                    .lastname("Dupont-Martin")
                    .build();
            when(service.save(updatedClient)).thenReturn(updatedClient);

            // When
            clientsService.update(updatedClient);

            // Then
            verify(service, times(1)).save(updatedClient);
        }

        @Test
        @DisplayName("Doit mettre à jour uniquement le prénom")
        void shouldUpdateOnlyFirstname() throws ImtException {
            // Given
            Client updatedClient = clientTest.toBuilder()
                    .firstname("Jacques")
                    .build();
            when(service.save(updatedClient)).thenReturn(updatedClient);

            // When
            clientsService.update(updatedClient);

            // Then
            verify(service, times(1)).save(updatedClient);
        }

        @Test
        @DisplayName("Doit mettre à jour le genre du client")
        void shouldUpdateClientGenre() throws ImtException {
            // Given
            Client updatedClient = clientTest.toBuilder()
                    .genre(GenreEnum.FEMME)
                    .build();
            when(service.save(updatedClient)).thenReturn(updatedClient);

            // When
            clientsService.update(updatedClient);

            // Then
            verify(service, times(1)).save(updatedClient);
        }
    }

    @Nested
    @DisplayName("delete() - Suppression d'un client")
    class DeleteTests {

        @Test
        @DisplayName("Doit supprimer le client avec succès")
        void shouldDeleteClientSuccessfully() throws ImtException {
            // Given
            doNothing().when(service).delete(testUuid);

            // When
            clientsService.delete(testUuid);

            // Then
            verify(service, times(1)).delete(testUuid);
        }

        @Test
        @DisplayName("Doit supprimer le client avec un UUID différent")
        void shouldDeleteClientWithDifferentUuid() throws ImtException {
            // Given
            UUID differentUuid = UUID.randomUUID();
            doNothing().when(service).delete(differentUuid);

            // When
            clientsService.delete(differentUuid);

            // Then
            verify(service, times(1)).delete(differentUuid);
        }
    }

    @Nested
    @DisplayName("Tests d'intégration des méthodes")
    class IntegrationTests {

        @Test
        @DisplayName("Scénario complet : création, récupération, mise à jour et suppression")
        void shouldExecuteCompleteScenario() throws ImtException {
            // Given
            Client newClient = Client.builder()
                    .lastname("Test")
                    .firstname("Integration")
                    .genre(GenreEnum.HOMME)
                    .build();

            // Le client sauvegardé avec son identifiant
            Client savedClient = Client.builder()
                    .lastname("Test")
                    .firstname("Integration")
                    .genre(GenreEnum.HOMME)
                    .build();
            UUID clientId = savedClient.getIdentifier();

            Client updatedClient = savedClient.toBuilder()
                    .firstname("IntegrationUpdated")
                    .build();

            when(service.save(any(Client.class))).thenReturn(savedClient).thenReturn(updatedClient);
            when(service.get(clientId)).thenReturn(Optional.of(savedClient)).thenReturn(Optional.of(updatedClient));
            doNothing().when(service).delete(clientId);

            // When - Create
            Client created = clientsService.create(newClient);
            assertThat(created).isEqualTo(savedClient);

            // When - Get
            Optional<Client> retrieved = clientsService.getOne(clientId);
            assertThat(retrieved).isPresent().contains(savedClient);

            // When - Update
            clientsService.update(updatedClient);

            // When - Get after update
            Optional<Client> retrievedAfterUpdate = clientsService.getOne(clientId);
            assertThat(retrievedAfterUpdate).isPresent().contains(updatedClient);

            // When - Delete
            clientsService.delete(clientId);

            // Then
            verify(service, times(2)).save(any(Client.class));
            verify(service, times(2)).get(clientId);
            verify(service, times(1)).delete(clientId);
        }
    }
}

