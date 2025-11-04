package com.wgt.imt.architecture.business.clients.validators;

import com.wgt.imt.architecture.business.clients.ClientStorageProvider;
import com.wgt.imt.architecture.business.clients.model.Client;
import com.wgt.imt.architecture.business.common.exception.ConflictException;
import com.wgt.imt.architecture.business.common.model.GenreEnum;
import com.wgt.imt.architecture.business.common.model.ValidatorResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour la classe ClientAlreadyExistValidatorStep.
 * Vérifie la validation de l'unicité d'un client.
 *
 * @author Emmanuel WAGUET
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ClientAlreadyExistValidatorStep - Tests unitaires")
class ClientAlreadyExistValidatorStepTest {

    @Mock
    private ClientStorageProvider storageProvider;

    private ClientAlreadyExistValidatorStep validator;

    private Client existingClient1;
    private Client existingClient2;

    @BeforeEach
    void setUp() {
        validator = new ClientAlreadyExistValidatorStep(storageProvider);

        existingClient1 = Client.builder()
                .lastname("Dupont")
                .firstname("Jean")
                .genre(GenreEnum.HOMME)
                .comptes(Collections.emptySet())
                .build();

        existingClient2 = Client.builder()
                .lastname("Martin")
                .firstname("Marie")
                .genre(GenreEnum.FEMME)
                .comptes(Collections.emptySet())
                .build();
    }

    @Nested
    @DisplayName("check() - Vérification de l'unicité")
    class CheckTests {

        @Test
        @DisplayName("Doit accepter un client qui n'existe pas")
        void shouldAcceptClientThatDoesNotExist() throws Exception {
            // Given
            Client newClient = Client.builder()
                    .lastname("Bernard")
                    .firstname("Paul")
                    .genre(GenreEnum.HOMME)
                    .comptes(Collections.emptySet())
                    .build();

            when(storageProvider.getAll()).thenReturn(Arrays.asList(existingClient1, existingClient2));

            // When & Then - Pas d'exception levée
            validator.check(newClient);
            verify(storageProvider, times(1)).getAll();
        }

        @Test
        @DisplayName("Doit accepter un client quand aucun client n'existe")
        void shouldAcceptClientWhenNoClientsExist() throws Exception {
            // Given
            Client newClient = Client.builder()
                    .lastname("Bernard")
                    .firstname("Paul")
                    .genre(GenreEnum.HOMME)
                    .comptes(Collections.emptySet())
                    .build();

            when(storageProvider.getAll()).thenReturn(Collections.emptyList());

            // When & Then - Pas d'exception levée
            validator.check(newClient);
            verify(storageProvider, times(1)).getAll();
        }

        @Test
        @DisplayName("Doit accepter un client quand getAll retourne null")
        void shouldAcceptClientWhenGetAllReturnsNull() throws Exception {
            // Given
            Client newClient = Client.builder()
                    .lastname("Bernard")
                    .firstname("Paul")
                    .genre(GenreEnum.HOMME)
                    .comptes(Collections.emptySet())
                    .build();

            when(storageProvider.getAll()).thenReturn(null);

            // When & Then - Pas d'exception levée
            validator.check(newClient);
            verify(storageProvider, times(1)).getAll();
        }

        @Test
        @DisplayName("Doit rejeter un client qui existe déjà (même nom, prénom et genre)")
        void shouldRejectClientThatAlreadyExists() {
            // Given
            Client duplicateClient = Client.builder()
                    .lastname("Dupont")
                    .firstname("Jean")
                    .genre(GenreEnum.HOMME)
                    .comptes(Collections.emptySet())
                    .build();

            when(storageProvider.getAll()).thenReturn(Collections.singletonList(existingClient1));

            // When & Then
            assertThatThrownBy(() -> validator.check(duplicateClient))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("Un client ayant ses infos existe déjà")
                    .hasMessageContaining("lastname : Dupont")
                    .hasMessageContaining("firstname : Jean")
                    .hasMessageContaining("genre : HOMME");

            verify(storageProvider, times(1)).getAll();
        }

        @Test
        @DisplayName("Doit rejeter un client avec la même identité en ignorant la casse")
        void shouldRejectClientIgnoringCase() {
            // Given - Nom et prénom avec différentes casses
            Client clientWithDifferentCase = Client.builder()
                    .lastname("DUPONT")
                    .firstname("JEAN")
                    .genre(GenreEnum.HOMME)
                    .comptes(Collections.emptySet())
                    .build();

            when(storageProvider.getAll()).thenReturn(Collections.singletonList(existingClient1));

            // When & Then
            assertThatThrownBy(() -> validator.check(clientWithDifferentCase))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("Un client ayant ses infos existe déjà");

            verify(storageProvider, times(1)).getAll();
        }

        @Test
        @DisplayName("Doit rejeter un client avec nom et prénom en minuscules")
        void shouldRejectClientWithLowercaseNames() {
            // Given
            Client clientWithLowercaseNames = Client.builder()
                    .lastname("dupont")
                    .firstname("jean")
                    .genre(GenreEnum.HOMME)
                    .comptes(Collections.emptySet())
                    .build();

            when(storageProvider.getAll()).thenReturn(Collections.singletonList(existingClient1));

            // When & Then
            assertThatThrownBy(() -> validator.check(clientWithLowercaseNames))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("Un client ayant ses infos existe déjà");

            verify(storageProvider, times(1)).getAll();
        }

        @Test
        @DisplayName("Doit accepter un client avec le même nom et prénom mais un genre différent")
        void shouldAcceptClientWithSameNameButDifferentGenre() throws Exception {
            // Given
            Client clientWithDifferentGenre = Client.builder()
                    .lastname("Dupont")
                    .firstname("Jean")
                    .genre(GenreEnum.FEMME) // Genre différent
                    .comptes(Collections.emptySet())
                    .build();

            when(storageProvider.getAll()).thenReturn(Collections.singletonList(existingClient1));

            // When & Then - Pas d'exception levée
            validator.check(clientWithDifferentGenre);
            verify(storageProvider, times(1)).getAll();
        }

        @Test
        @DisplayName("Doit accepter un client avec le même nom mais un prénom différent")
        void shouldAcceptClientWithSameLastnameButDifferentFirstname() throws Exception {
            // Given
            Client clientWithDifferentFirstname = Client.builder()
                    .lastname("Dupont")
                    .firstname("Pierre")
                    .genre(GenreEnum.HOMME)
                    .comptes(Collections.emptySet())
                    .build();

            when(storageProvider.getAll()).thenReturn(Collections.singletonList(existingClient1));

            // When & Then - Pas d'exception levée
            validator.check(clientWithDifferentFirstname);
            verify(storageProvider, times(1)).getAll();
        }

        @Test
        @DisplayName("Doit accepter un client avec le même prénom mais un nom différent")
        void shouldAcceptClientWithSameFirstnameButDifferentLastname() throws Exception {
            // Given
            Client clientWithDifferentLastname = Client.builder()
                    .lastname("Martin")
                    .firstname("Jean")
                    .genre(GenreEnum.HOMME)
                    .comptes(Collections.emptySet())
                    .build();

            when(storageProvider.getAll()).thenReturn(Collections.singletonList(existingClient1));

            // When & Then - Pas d'exception levée
            validator.check(clientWithDifferentLastname);
            verify(storageProvider, times(1)).getAll();
        }

        @Test
        @DisplayName("Doit rejeter un client quand plusieurs clients existent et qu'un correspond")
        void shouldRejectClientWhenMultipleClientsExistAndOneMatches() {
            // Given
            Client duplicateClient = Client.builder()
                    .lastname("Martin")
                    .firstname("Marie")
                    .genre(GenreEnum.FEMME)
                    .comptes(Collections.emptySet())
                    .build();

            when(storageProvider.getAll()).thenReturn(Arrays.asList(existingClient1, existingClient2));

            // When & Then
            assertThatThrownBy(() -> validator.check(duplicateClient))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("Un client ayant ses infos existe déjà")
                    .hasMessageContaining("lastname : Martin")
                    .hasMessageContaining("firstname : Marie")
                    .hasMessageContaining("genre : FEMME");

            verify(storageProvider, times(1)).getAll();
        }

        @Test
        @DisplayName("Doit accepter un client avec tous les genres acceptables")
        void shouldAcceptClientWithAllAcceptableGenres() throws Exception {
            // Given
            when(storageProvider.getAll()).thenReturn(Collections.emptyList());

            // Test HOMME
            Client clientHomme = Client.builder()
                    .lastname("Test")
                    .firstname("Homme")
                    .genre(GenreEnum.HOMME)
                    .comptes(Collections.emptySet())
                    .build();
            validator.check(clientHomme);

            // Test FEMME
            Client clientFemme = Client.builder()
                    .lastname("Test")
                    .firstname("Femme")
                    .genre(GenreEnum.FEMME)
                    .comptes(Collections.emptySet())
                    .build();
            validator.check(clientFemme);

            // Test NON_COMMUNIQUER
            Client clientNonCommunique = Client.builder()
                    .lastname("Test")
                    .firstname("NonCommunique")
                    .genre(GenreEnum.NON_COMMUNIQUER)
                    .comptes(Collections.emptySet())
                    .build();
            validator.check(clientNonCommunique);

            // Then
            verify(storageProvider, times(3)).getAll();
        }
    }

    @Nested
    @DisplayName("validate() - Validation complète")
    class ValidateTests {

        @Test
        @DisplayName("Doit retourner un résultat valide pour un nouveau client")
        void shouldReturnValidResultForNewClient() {
            // Given
            Client newClient = Client.builder()
                    .lastname("Bernard")
                    .firstname("Paul")
                    .genre(GenreEnum.HOMME)
                    .comptes(Collections.emptySet())
                    .build();

            when(storageProvider.getAll()).thenReturn(Collections.singletonList(existingClient1));

            // When
            ValidatorResult result = validator.validate(newClient);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.isValid()).isTrue();
            assertThat(result.getExceptionToThrow()).isNull();
            verify(storageProvider, times(1)).getAll();
        }

        @Test
        @DisplayName("Doit retourner un résultat invalide pour un client qui existe déjà")
        void shouldReturnInvalidResultForExistingClient() {
            // Given
            Client duplicateClient = Client.builder()
                    .lastname("Dupont")
                    .firstname("Jean")
                    .genre(GenreEnum.HOMME)
                    .comptes(Collections.emptySet())
                    .build();

            when(storageProvider.getAll()).thenReturn(Collections.singletonList(existingClient1));

            // When
            ValidatorResult result = validator.validate(duplicateClient);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.isValid()).isFalse();
            assertThat(result.getExceptionToThrow())
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("Un client ayant ses infos existe déjà");
            verify(storageProvider, times(1)).getAll();
        }

        @Test
        @DisplayName("Doit lever une exception lors de throwIfInvalid pour un client existant")
        void shouldThrowExceptionWhenThrowIfInvalidForExistingClient() {
            // Given
            Client duplicateClient = Client.builder()
                    .lastname("Dupont")
                    .firstname("Jean")
                    .genre(GenreEnum.HOMME)
                    .comptes(Collections.emptySet())
                    .build();

            when(storageProvider.getAll()).thenReturn(Collections.singletonList(existingClient1));

            // When
            ValidatorResult result = validator.validate(duplicateClient);

            // Then
            assertThatThrownBy(result::throwIfInvalid)
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("Un client ayant ses infos existe déjà");
        }

        @Test
        @DisplayName("Doit rejeter un client null")
        void shouldRejectNullClient() {
            // When & Then
            assertThatThrownBy(() -> validator.validate(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Impossible de valider un objet nul");

            verify(storageProvider, never()).getAll();
        }

        @Test
        @DisplayName("Doit retourner un résultat valide quand la liste est vide")
        void shouldReturnValidResultWhenListIsEmpty() {
            // Given
            Client newClient = Client.builder()
                    .lastname("Bernard")
                    .firstname("Paul")
                    .genre(GenreEnum.HOMME)
                    .comptes(Collections.emptySet())
                    .build();

            when(storageProvider.getAll()).thenReturn(Collections.emptyList());

            // When
            ValidatorResult result = validator.validate(newClient);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.isValid()).isTrue();
            verify(storageProvider, times(1)).getAll();
        }
    }

    @Nested
    @DisplayName("Tests d'intégration avec chaînage de validateurs")
    class ChainTests {

        @Test
        @DisplayName("Doit chaîner avec un autre validateur et exécuter les deux")
        void shouldChainWithAnotherValidator() {
            // Given
            ClientGenreValidatorStep genreValidator = new ClientGenreValidatorStep();
            validator.linkWith(genreValidator);

            Client client = Client.builder()
                    .lastname("Bernard")
                    .firstname("Paul")
                    .genre(GenreEnum.HOMME)
                    .comptes(Collections.emptySet())
                    .build();

            when(storageProvider.getAll()).thenReturn(Collections.emptyList());

            // When
            ValidatorResult result = validator.validate(client);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.isValid()).isTrue();
            verify(storageProvider, times(1)).getAll();
        }

        @Test
        @DisplayName("Doit échouer sur le premier validateur si le client existe déjà")
        void shouldFailOnFirstValidatorIfClientExists() {
            // Given
            ClientGenreValidatorStep genreValidator = new ClientGenreValidatorStep();
            validator.linkWith(genreValidator);

            Client duplicateClient = Client.builder()
                    .lastname("Dupont")
                    .firstname("Jean")
                    .genre(GenreEnum.HOMME)
                    .comptes(Collections.emptySet())
                    .build();

            when(storageProvider.getAll()).thenReturn(Collections.singletonList(existingClient1));

            // When
            ValidatorResult result = validator.validate(duplicateClient);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.isValid()).isFalse();
            assertThat(result.getExceptionToThrow()).isInstanceOf(ConflictException.class);
            verify(storageProvider, times(1)).getAll();
        }

        @Test
        @DisplayName("Doit échouer sur le second validateur si le genre est INCONNU")
        void shouldFailOnSecondValidatorIfGenreIsInconnu() {
            // Given
            ClientGenreValidatorStep genreValidator = new ClientGenreValidatorStep();
            validator.linkWith(genreValidator);

            Client clientWithInvalidGenre = Client.builder()
                    .lastname("Bernard")
                    .firstname("Paul")
                    .genre(GenreEnum.INCONNU)
                    .comptes(Collections.emptySet())
                    .build();

            when(storageProvider.getAll()).thenReturn(Collections.emptyList());

            // When
            ValidatorResult result = validator.validate(clientWithInvalidGenre);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.isValid()).isFalse();
            assertThat(result.getExceptionToThrow()).isInstanceOf(com.wgt.imt.architecture.business.common.exception.BadRequestException.class);
            verify(storageProvider, times(1)).getAll();
        }
    }
}

