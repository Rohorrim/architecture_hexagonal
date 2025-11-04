package com.wgt.imt.architecture.business.clients;

import com.wgt.imt.architecture.business.clients.model.Client;
import com.wgt.imt.architecture.business.common.exception.BadRequestException;
import com.wgt.imt.architecture.business.common.exception.ConflictException;
import com.wgt.imt.architecture.business.common.exception.ImtException;
import com.wgt.imt.architecture.business.common.model.GenreEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour la classe ClientsServiceValidator.
 * Vérifie le bon fonctionnement des validations métier lors des opérations de création et modification.
 *
 * @author Emmanuel WAGUET
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ClientsServiceValidator - Tests unitaires")
class ClientsServiceValidatorTest {

    @Mock
    private ClientStorageProvider storageProvider;

    private ClientsServiceValidator clientsServiceValidator;

    private Client validClient;
    private UUID testUuid;

    @BeforeEach
    void setUp() {
        clientsServiceValidator = new ClientsServiceValidator(storageProvider);
        testUuid = UUID.randomUUID();
        validClient = Client.builder()
                .lastname("Dupont")
                .firstname("Jean")
                .genre(GenreEnum.HOMME)
                .comptes(Collections.emptySet())
                .build();
    }

    @Nested
    @DisplayName("create() - Création d'un nouveau client avec validation")
    class CreateTests {

        @Test
        @DisplayName("Doit créer un client valide avec succès")
        void shouldCreateValidClientSuccessfully() throws ImtException {
            // Given
            when(storageProvider.getAll()).thenReturn(Collections.emptyList());
            when(storageProvider.save(validClient)).thenReturn(validClient);

            // When
            Client result = clientsServiceValidator.create(validClient);

            // Then
            assertThat(result).isNotNull().isEqualTo(validClient);
            verify(storageProvider, times(1)).getAll();
            verify(storageProvider, times(1)).save(validClient);
        }

        @Test
        @DisplayName("Doit rejeter un client avec un genre INCONNU")
        void shouldRejectClientWithInvalidGenre() {
            // Given
            Client clientWithInvalidGenre = Client.builder()
                    .lastname("Dupont")
                    .firstname("Jean")
                    .genre(GenreEnum.INCONNU)
                    .comptes(Collections.emptySet())
                    .build();
            when(storageProvider.getAll()).thenReturn(Collections.emptyList());

            // When & Then
            assertThatThrownBy(() -> clientsServiceValidator.create(clientWithInvalidGenre))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Le genre communiqué ne correspond pas aux valeurs acceptables");

            // Note: getAll() est appelé car ConstraintValidatorStep passe, puis ClientAlreadyExistValidatorStep appelle getAll(),
            // et ensuite ClientGenreValidatorStep échoue
            verify(storageProvider, times(1)).getAll();
            verify(storageProvider, never()).save(any(Client.class));
        }

        @Test
        @DisplayName("Doit rejeter un client qui existe déjà")
        void shouldRejectClientThatAlreadyExists() {
            // Given
            Client existingClient = Client.builder()
                    .lastname("Dupont")
                    .firstname("Jean")
                    .genre(GenreEnum.HOMME)
                    .comptes(Collections.emptySet())
                    .build();

            when(storageProvider.getAll()).thenReturn(Collections.singletonList(existingClient));

            // When & Then
            assertThatThrownBy(() -> clientsServiceValidator.create(validClient))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("Un client ayant ses infos existe déjà")
                    .hasMessageContaining("Dupont")
                    .hasMessageContaining("Jean")
                    .hasMessageContaining("HOMME");

            verify(storageProvider, times(1)).getAll();
            verify(storageProvider, never()).save(any(Client.class));
        }

        @Test
        @DisplayName("Doit rejeter un client avec un nom de famille null")
        void shouldRejectClientWithNullLastname() {
            // Given
            Client clientWithNullLastname = Client.builder()
                    .lastname(null)
                    .firstname("Jean")
                    .genre(GenreEnum.HOMME)
                    .comptes(Collections.emptySet())
                    .build();

            // When & Then
            assertThatThrownBy(() -> clientsServiceValidator.create(clientWithNullLastname))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Au moins une contrainte sur l'objet n'est pas respectée");

            // ConstraintValidatorStep échoue en premier, donc getAll() n'est jamais appelé
            verify(storageProvider, never()).getAll();
            verify(storageProvider, never()).save(any(Client.class));
        }

        @Test
        @DisplayName("Doit rejeter un client avec un prénom null")
        void shouldRejectClientWithNullFirstname() {
            // Given
            Client clientWithNullFirstname = Client.builder()
                    .lastname("Dupont")
                    .firstname(null)
                    .genre(GenreEnum.HOMME)
                    .comptes(Collections.emptySet())
                    .build();

            // When & Then
            assertThatThrownBy(() -> clientsServiceValidator.create(clientWithNullFirstname))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Au moins une contrainte sur l'objet n'est pas respectée");

            verify(storageProvider, never()).getAll();
            verify(storageProvider, never()).save(any(Client.class));
        }

        @Test
        @DisplayName("Doit rejeter un client avec un nom invalide (contient des chiffres)")
        void shouldRejectClientWithInvalidLastname() {
            // Given
            Client clientWithInvalidLastname = Client.builder()
                    .lastname("Dupont123")
                    .firstname("Jean")
                    .genre(GenreEnum.HOMME)
                    .comptes(Collections.emptySet())
                    .build();

            // When & Then
            assertThatThrownBy(() -> clientsServiceValidator.create(clientWithInvalidLastname))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Au moins une contrainte sur l'objet n'est pas respectée");

            verify(storageProvider, never()).getAll();
            verify(storageProvider, never()).save(any(Client.class));
        }

        @Test
        @DisplayName("Doit rejeter un client avec un prénom invalide (contient des caractères spéciaux)")
        void shouldRejectClientWithInvalidFirstname() {
            // Given
            Client clientWithInvalidFirstname = Client.builder()
                    .lastname("Dupont")
                    .firstname("Jean@")
                    .genre(GenreEnum.HOMME)
                    .comptes(Collections.emptySet())
                    .build();

            // When & Then
            assertThatThrownBy(() -> clientsServiceValidator.create(clientWithInvalidFirstname))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Au moins une contrainte sur l'objet n'est pas respectée");

            verify(storageProvider, never()).getAll();
            verify(storageProvider, never()).save(any(Client.class));
        }

        @Test
        @DisplayName("Doit permettre la création de clients avec le même nom mais des genres différents")
        void shouldAllowCreationOfClientsWithSameNameButDifferentGenres() throws ImtException {
            // Given
            Client existingClientHomme = Client.builder()
                    .lastname("Dupont")
                    .firstname("Jean")
                    .genre(GenreEnum.HOMME)
                    .comptes(Collections.emptySet())
                    .build();

            Client newClientFemme = Client.builder()
                    .lastname("Dupont")
                    .firstname("Jean")
                    .genre(GenreEnum.FEMME)
                    .comptes(Collections.emptySet())
                    .build();

            when(storageProvider.getAll()).thenReturn(Collections.singletonList(existingClientHomme));
            when(storageProvider.save(newClientFemme)).thenReturn(newClientFemme);

            // When
            Client result = clientsServiceValidator.create(newClientFemme);

            // Then
            assertThat(result).isNotNull().isEqualTo(newClientFemme);
            verify(storageProvider, times(1)).getAll();
            verify(storageProvider, times(1)).save(newClientFemme);
        }

        @Test
        @DisplayName("Doit vérifier l'unicité en ignorant la casse des noms")
        void shouldCheckUniquenessIgnoringCase() {
            // Given
            Client existingClient = Client.builder()
                    .lastname("dupont")
                    .firstname("jean")
                    .genre(GenreEnum.HOMME)
                    .comptes(Collections.emptySet())
                    .build();

            Client newClientWithDifferentCase = Client.builder()
                    .lastname("DUPONT")
                    .firstname("JEAN")
                    .genre(GenreEnum.HOMME)
                    .comptes(Collections.emptySet())
                    .build();

            when(storageProvider.getAll()).thenReturn(Collections.singletonList(existingClient));

            // When & Then
            assertThatThrownBy(() -> clientsServiceValidator.create(newClientWithDifferentCase))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("Un client ayant ses infos existe déjà");

            verify(storageProvider, times(1)).getAll();
            verify(storageProvider, never()).save(any(Client.class));
        }

        @Test
        @DisplayName("Doit accepter un client avec le genre FEMME")
        void shouldAcceptClientWithGenreFemme() throws ImtException {
            // Given
            Client clientFemme = Client.builder()
                    .lastname("Martin")
                    .firstname("Marie")
                    .genre(GenreEnum.FEMME)
                    .comptes(Collections.emptySet())
                    .build();

            when(storageProvider.getAll()).thenReturn(Collections.emptyList());
            when(storageProvider.save(clientFemme)).thenReturn(clientFemme);

            // When
            Client result = clientsServiceValidator.create(clientFemme);

            // Then
            assertThat(result).isNotNull().isEqualTo(clientFemme);
            verify(storageProvider, times(1)).save(clientFemme);
        }

        @Test
        @DisplayName("Doit accepter un client avec le genre NON_COMMUNIQUER")
        void shouldAcceptClientWithGenreNonCommuniquer() throws ImtException {
            // Given
            Client clientNonCommunique = Client.builder()
                    .lastname("Bernard")
                    .firstname("Alex")
                    .genre(GenreEnum.NON_COMMUNIQUER)
                    .comptes(Collections.emptySet())
                    .build();

            when(storageProvider.getAll()).thenReturn(Collections.emptyList());
            when(storageProvider.save(clientNonCommunique)).thenReturn(clientNonCommunique);

            // When
            Client result = clientsServiceValidator.create(clientNonCommunique);

            // Then
            assertThat(result).isNotNull().isEqualTo(clientNonCommunique);
            verify(storageProvider, times(1)).save(clientNonCommunique);
        }

        @Test
        @DisplayName("Doit créer un client même s'il existe d'autres clients")
        void shouldCreateClientEvenIfOtherClientsExist() throws ImtException {
            // Given
            Client existingClient1 = Client.builder()
                    .lastname("Martin")
                    .firstname("Marie")
                    .genre(GenreEnum.FEMME)
                    .comptes(Collections.emptySet())
                    .build();

            Client existingClient2 = Client.builder()
                    .lastname("Bernard")
                    .firstname("Paul")
                    .genre(GenreEnum.HOMME)
                    .comptes(Collections.emptySet())
                    .build();

            when(storageProvider.getAll()).thenReturn(Arrays.asList(existingClient1, existingClient2));
            when(storageProvider.save(validClient)).thenReturn(validClient);

            // When
            Client result = clientsServiceValidator.create(validClient);

            // Then
            assertThat(result).isNotNull().isEqualTo(validClient);
            verify(storageProvider, times(1)).getAll();
            verify(storageProvider, times(1)).save(validClient);
        }
    }

    @Nested
    @DisplayName("update() - Mise à jour d'un client existant avec validation")
    class UpdateTests {

        @Test
        @DisplayName("Doit mettre à jour un client valide avec succès")
        void shouldUpdateValidClientSuccessfully() throws ImtException {
            // Given
            Client updatedClient = validClient.toBuilder()
                    .lastname("Dupont-Martin")
                    .build();

            when(storageProvider.save(updatedClient)).thenReturn(updatedClient);

            // When
            clientsServiceValidator.update(updatedClient);

            // Then
            verify(storageProvider, times(1)).save(updatedClient);
            verify(storageProvider, never()).getAll();
        }

        @Test
        @DisplayName("Doit rejeter une mise à jour avec un genre INCONNU")
        void shouldRejectUpdateWithInvalidGenre() {
            // Given
            Client clientWithInvalidGenre = validClient.toBuilder()
                    .genre(GenreEnum.INCONNU)
                    .build();

            // When & Then
            assertThatThrownBy(() -> clientsServiceValidator.update(clientWithInvalidGenre))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Le genre communiqué ne correspond pas aux valeurs acceptables");

            verify(storageProvider, never()).save(any(Client.class));
        }

        @Test
        @DisplayName("Doit rejeter une mise à jour avec un nom null")
        void shouldRejectUpdateWithNullLastname() {
            // Given
            Client clientWithNullLastname = validClient.toBuilder()
                    .lastname(null)
                    .build();

            // When & Then
            assertThatThrownBy(() -> clientsServiceValidator.update(clientWithNullLastname))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Au moins une contrainte sur l'objet n'est pas respectée");

            verify(storageProvider, never()).save(any(Client.class));
        }

        @Test
        @DisplayName("Doit rejeter une mise à jour avec un prénom null")
        void shouldRejectUpdateWithNullFirstname() {
            // Given
            Client clientWithNullFirstname = validClient.toBuilder()
                    .firstname(null)
                    .build();

            // When & Then
            assertThatThrownBy(() -> clientsServiceValidator.update(clientWithNullFirstname))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Au moins une contrainte sur l'objet n'est pas respectée");

            verify(storageProvider, never()).save(any(Client.class));
        }

        @Test
        @DisplayName("Doit rejeter une mise à jour avec un nom invalide")
        void shouldRejectUpdateWithInvalidLastname() {
            // Given
            Client clientWithInvalidLastname = validClient.toBuilder()
                    .lastname("Dupont123")
                    .build();

            // When & Then
            assertThatThrownBy(() -> clientsServiceValidator.update(clientWithInvalidLastname))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Au moins une contrainte sur l'objet n'est pas respectée");

            verify(storageProvider, never()).save(any(Client.class));
        }

        @Test
        @DisplayName("Doit rejeter une mise à jour avec un prénom invalide")
        void shouldRejectUpdateWithInvalidFirstname() {
            // Given
            Client clientWithInvalidFirstname = validClient.toBuilder()
                    .firstname("Jean#")
                    .build();

            // When & Then
            assertThatThrownBy(() -> clientsServiceValidator.update(clientWithInvalidFirstname))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Au moins une contrainte sur l'objet n'est pas respectée");

            verify(storageProvider, never()).save(any(Client.class));
        }

        @Test
        @DisplayName("Doit permettre la mise à jour du genre vers FEMME")
        void shouldAllowUpdateToGenreFemme() throws ImtException {
            // Given
            Client updatedClient = validClient.toBuilder()
                    .genre(GenreEnum.FEMME)
                    .build();

            when(storageProvider.save(updatedClient)).thenReturn(updatedClient);

            // When
            clientsServiceValidator.update(updatedClient);

            // Then
            verify(storageProvider, times(1)).save(updatedClient);
        }

        @Test
        @DisplayName("Doit permettre la mise à jour du genre vers NON_COMMUNIQUER")
        void shouldAllowUpdateToGenreNonCommuniquer() throws ImtException {
            // Given
            Client updatedClient = validClient.toBuilder()
                    .genre(GenreEnum.NON_COMMUNIQUER)
                    .build();

            when(storageProvider.save(updatedClient)).thenReturn(updatedClient);

            // When
            clientsServiceValidator.update(updatedClient);

            // Then
            verify(storageProvider, times(1)).save(updatedClient);
        }

        @Test
        @DisplayName("Doit permettre la mise à jour uniquement du prénom")
        void shouldAllowUpdateOfOnlyFirstname() throws ImtException {
            // Given
            Client updatedClient = validClient.toBuilder()
                    .firstname("Jacques")
                    .build();

            when(storageProvider.save(updatedClient)).thenReturn(updatedClient);

            // When
            clientsServiceValidator.update(updatedClient);

            // Then
            verify(storageProvider, times(1)).save(updatedClient);
        }

        @Test
        @DisplayName("Doit permettre la mise à jour uniquement du nom")
        void shouldAllowUpdateOfOnlyLastname() throws ImtException {
            // Given
            Client updatedClient = validClient.toBuilder()
                    .lastname("Martin")
                    .build();

            when(storageProvider.save(updatedClient)).thenReturn(updatedClient);

            // When
            clientsServiceValidator.update(updatedClient);

            // Then
            verify(storageProvider, times(1)).save(updatedClient);
        }

        @Test
        @DisplayName("Ne doit pas vérifier l'unicité lors de la mise à jour")
        void shouldNotCheckUniquenessOnUpdate() throws ImtException {
            // Given - on ne vérifie pas getAll() lors d'une mise à jour
            Client updatedClient = validClient.toBuilder()
                    .firstname("Pierre")
                    .build();

            when(storageProvider.save(updatedClient)).thenReturn(updatedClient);

            // When
            clientsServiceValidator.update(updatedClient);

            // Then
            verify(storageProvider, never()).getAll();
            verify(storageProvider, times(1)).save(updatedClient);
        }
    }

    @Nested
    @DisplayName("Tests de validation des contraintes")
    class ConstraintValidationTests {

        @Test
        @DisplayName("Doit rejeter un nom trop long (plus de 250 caractères)")
        void shouldRejectTooLongLastname() {
            // Given
            String tooLongName = "a".repeat(251);
            Client clientWithTooLongLastname = Client.builder()
                    .lastname(tooLongName)
                    .firstname("Jean")
                    .genre(GenreEnum.HOMME)
                    .comptes(Collections.emptySet())
                    .build();

            // When & Then
            assertThatThrownBy(() -> clientsServiceValidator.create(clientWithTooLongLastname))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Au moins une contrainte sur l'objet n'est pas respectée");

            verify(storageProvider, never()).getAll();
            verify(storageProvider, never()).save(any(Client.class));
        }

        @Test
        @DisplayName("Doit rejeter un prénom trop long (plus de 250 caractères)")
        void shouldRejectTooLongFirstname() {
            // Given
            String tooLongName = "a".repeat(251);
            Client clientWithTooLongFirstname = Client.builder()
                    .lastname("Dupont")
                    .firstname(tooLongName)
                    .genre(GenreEnum.HOMME)
                    .comptes(Collections.emptySet())
                    .build();

            // When & Then
            assertThatThrownBy(() -> clientsServiceValidator.create(clientWithTooLongFirstname))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Au moins une contrainte sur l'objet n'est pas respectée");

            verify(storageProvider, never()).getAll();
            verify(storageProvider, never()).save(any(Client.class));
        }

        @Test
        @DisplayName("Doit accepter un nom avec tiret")
        void shouldAcceptLastnameWithHyphen() throws ImtException {
            // Given
            Client clientWithHyphen = Client.builder()
                    .lastname("Dupont-Martin")
                    .firstname("Jean")
                    .genre(GenreEnum.HOMME)
                    .comptes(Collections.emptySet())
                    .build();

            when(storageProvider.getAll()).thenReturn(Collections.emptyList());
            when(storageProvider.save(clientWithHyphen)).thenReturn(clientWithHyphen);

            // When
            Client result = clientsServiceValidator.create(clientWithHyphen);

            // Then
            assertThat(result).isNotNull();
            verify(storageProvider, times(1)).save(clientWithHyphen);
        }

        @Test
        @DisplayName("Doit accepter un nom avec espace")
        void shouldAcceptLastnameWithSpace() throws ImtException {
            // Given
            Client clientWithSpace = Client.builder()
                    .lastname("De La Fontaine")
                    .firstname("Jean")
                    .genre(GenreEnum.HOMME)
                    .comptes(Collections.emptySet())
                    .build();

            when(storageProvider.getAll()).thenReturn(Collections.emptyList());
            when(storageProvider.save(clientWithSpace)).thenReturn(clientWithSpace);

            // When
            Client result = clientsServiceValidator.create(clientWithSpace);

            // Then
            assertThat(result).isNotNull();
            verify(storageProvider, times(1)).save(clientWithSpace);
        }
    }
}

