package com.wgt.imt.architecture.business.comptes;

import com.wgt.imt.architecture.business.clients.ClientStorageProvider;
import com.wgt.imt.architecture.business.clients.model.Client;
import com.wgt.imt.architecture.business.common.exception.BadRequestException;
import com.wgt.imt.architecture.business.common.exception.ConflictException;
import com.wgt.imt.architecture.business.common.exception.ImtException;
import com.wgt.imt.architecture.business.common.model.GenreEnum;
import com.wgt.imt.architecture.business.common.model.TypeCompteEnum;
import com.wgt.imt.architecture.business.comptes.model.Compte;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour la classe ComptesServiceValidator.
 * Vérifie le bon fonctionnement des validations métier lors des opérations sur les comptes.
 *
 * @author Emmanuel WAGUET
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ComptesServiceValidator - Tests unitaires")
class ComptesServiceValidatorTest {

    @Mock
    private ClientStorageProvider storageProvider;

    private ComptesServiceValidator comptesServiceValidator;

    private UUID clientIdentifier;
    private Client client;
    private Compte existingCompte;

    @BeforeEach
    void setUp() {
        comptesServiceValidator = new ComptesServiceValidator(storageProvider);

        clientIdentifier = UUID.randomUUID();

        existingCompte = Compte.builder()
                .clientIdentifier(clientIdentifier)
                .name("Compte_Courant")
                .type(TypeCompteEnum.COMPTE_COURANT)
                .solde(1000.0)
                .build();

        Set<Compte> comptes = new HashSet<>(Collections.singletonList(existingCompte));

        client = Client.builder()
                .lastname("Dupont")
                .firstname("Jean")
                .genre(GenreEnum.HOMME)
                .comptes(comptes)
                .build();
    }

    @Nested
    @DisplayName("create() - Création d'un compte avec validation")
    class CreateTests {

        @Test
        @DisplayName("Doit créer un compte valide avec succès")
        void shouldCreateValidAccountSuccessfully() throws ImtException {
            // Given
            Compte newCompte = Compte.builder()
                    .clientIdentifier(clientIdentifier)
                    .name("Livret_A")
                    .type(TypeCompteEnum.LIVRET_A)
                    .solde(0.0)
                    .build();

            when(storageProvider.get(clientIdentifier)).thenReturn(Optional.of(client));
            when(storageProvider.save(any(Client.class))).thenReturn(client);

            // When
            Compte result = comptesServiceValidator.create(clientIdentifier, newCompte);

            // Then
            assertThat(result).isNotNull().isEqualTo(newCompte);
            verify(storageProvider, times(2)).get(clientIdentifier);
            verify(storageProvider, times(1)).save(any(Client.class));
        }

        @Test
        @DisplayName("Doit rejeter un compte avec un type INCONNU")
        void shouldRejectAccountWithInvalidType() {
            // Given
            Compte compteWithInvalidType = Compte.builder()
                    .clientIdentifier(clientIdentifier)
                    .name("Compte_Test")
                    .type(TypeCompteEnum.INCONNU)
                    .solde(0.0)
                    .build();

            when(storageProvider.get(clientIdentifier)).thenReturn(Optional.of(client));

            // When & Then
            assertThatThrownBy(() -> comptesServiceValidator.create(clientIdentifier, compteWithInvalidType))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Le type communiqué ne correspond pas aux valeurs acceptables");

            verify(storageProvider, times(1)).get(clientIdentifier);
            verify(storageProvider, never()).save(any(Client.class));
        }

        @Test
        @DisplayName("Doit rejeter un compte qui existe déjà")
        void shouldRejectAccountThatAlreadyExists() {
            // Given
            Compte duplicateCompte = Compte.builder()
                    .clientIdentifier(clientIdentifier)
                    .name("Compte_Courant")
                    .type(TypeCompteEnum.COMPTE_COURANT)
                    .solde(0.0)
                    .build();

            when(storageProvider.get(clientIdentifier)).thenReturn(Optional.of(client));

            // When & Then
            assertThatThrownBy(() -> comptesServiceValidator.create(clientIdentifier, duplicateCompte))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("Un compte ayant les mêmes caractéristique existe déjà pour le client");

            verify(storageProvider, times(1)).get(clientIdentifier);
            verify(storageProvider, never()).save(any(Client.class));
        }

        @Test
        @DisplayName("Doit rejeter un compte avec un nom null")
        void shouldRejectAccountWithNullName() {
            // Given
            Compte compteWithNullName = Compte.builder()
                    .clientIdentifier(clientIdentifier)
                    .name(null)
                    .type(TypeCompteEnum.COMPTE_COURANT)
                    .build();

            // When & Then
            assertThatThrownBy(() -> comptesServiceValidator.create(clientIdentifier, compteWithNullName))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Au moins une contrainte sur l'objet n'est pas respectée");

            verify(storageProvider, never()).get(any());
            verify(storageProvider, never()).save(any(Client.class));
        }

        @Test
        @DisplayName("Doit rejeter un compte avec un nom invalide (caractères spéciaux)")
        void shouldRejectAccountWithInvalidName() {
            // Given
            Compte compteWithInvalidName = Compte.builder()
                    .clientIdentifier(clientIdentifier)
                    .name("Compte@Invalid")
                    .type(TypeCompteEnum.COMPTE_COURANT)
                    .build();

            // When & Then
            assertThatThrownBy(() -> comptesServiceValidator.create(clientIdentifier, compteWithInvalidName))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Au moins une contrainte sur l'objet n'est pas respectée");

            verify(storageProvider, never()).get(any());
            verify(storageProvider, never()).save(any(Client.class));
        }

        @Test
        @DisplayName("Doit rejeter un compte avec un type null")
        void shouldRejectAccountWithNullType() {
            // Given
            Compte compteWithNullType = Compte.builder()
                    .clientIdentifier(clientIdentifier)
                    .name("Compte_Test")
                    .type(null)
                    .build();

            // When & Then
            assertThatThrownBy(() -> comptesServiceValidator.create(clientIdentifier, compteWithNullType))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Au moins une contrainte sur l'objet n'est pas respectée");

            verify(storageProvider, never()).get(any());
            verify(storageProvider, never()).save(any(Client.class));
        }

        @Test
        @DisplayName("Doit accepter un compte avec tous les types acceptables")
        void shouldAcceptAccountWithAllAcceptableTypes() throws ImtException {
            // Given
            when(storageProvider.get(clientIdentifier)).thenReturn(Optional.of(client));
            when(storageProvider.save(any(Client.class))).thenReturn(client);

            // Test COMPTE_COURANT
            Compte compteCourant = Compte.builder()
                    .clientIdentifier(clientIdentifier)
                    .name("Nouveau_Compte_Courant")
                    .type(TypeCompteEnum.COMPTE_COURANT)
                    .build();
            comptesServiceValidator.create(clientIdentifier, compteCourant);

            // Test LIVRET_A
            Compte livretA = Compte.builder()
                    .clientIdentifier(clientIdentifier)
                    .name("Livret_A")
                    .type(TypeCompteEnum.LIVRET_A)
                    .build();
            comptesServiceValidator.create(clientIdentifier, livretA);

            // Test LDDS
            Compte ldds = Compte.builder()
                    .clientIdentifier(clientIdentifier)
                    .name("LDDS")
                    .type(TypeCompteEnum.LDDS)
                    .build();
            comptesServiceValidator.create(clientIdentifier, ldds);

            // Then
            verify(storageProvider, times(3)).save(any(Client.class));
        }

        @Test
        @DisplayName("Doit permettre de créer un compte avec le même nom mais un type différent")
        void shouldAllowAccountWithSameNameButDifferentType() throws ImtException {
            // Given
            Compte compteWithSameName = Compte.builder()
                    .clientIdentifier(clientIdentifier)
                    .name("Compte_Courant")
                    .type(TypeCompteEnum.LIVRET_A) // Type différent
                    .build();

            when(storageProvider.get(clientIdentifier)).thenReturn(Optional.of(client));
            when(storageProvider.save(any(Client.class))).thenReturn(client);

            // When
            Compte result = comptesServiceValidator.create(clientIdentifier, compteWithSameName);

            // Then
            assertThat(result).isNotNull();
            verify(storageProvider, times(1)).save(any(Client.class));
        }

        @Test
        @DisplayName("Doit vérifier l'unicité en ignorant la casse")
        void shouldCheckUniquenessIgnoringCase() {
            // Given
            Compte compteWithDifferentCase = Compte.builder()
                    .clientIdentifier(clientIdentifier)
                    .name("COMPTE_COURANT")
                    .type(TypeCompteEnum.COMPTE_COURANT)
                    .build();

            when(storageProvider.get(clientIdentifier)).thenReturn(Optional.of(client));

            // When & Then
            assertThatThrownBy(() -> comptesServiceValidator.create(clientIdentifier, compteWithDifferentCase))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("Un compte ayant les mêmes caractéristique existe déjà pour le client");

            verify(storageProvider, times(1)).get(clientIdentifier);
            verify(storageProvider, never()).save(any(Client.class));
        }
    }

    @Nested
    @DisplayName("update() - Mise à jour d'un compte avec validation")
    class UpdateTests {

        @Test
        @DisplayName("Doit mettre à jour un compte valide avec succès")
        void shouldUpdateValidAccountSuccessfully() throws ImtException {
            // Given
            Compte updatedCompte = existingCompte.toBuilder()
                    .solde(2000.0)
                    .build();

            when(storageProvider.get(clientIdentifier)).thenReturn(Optional.of(client));
            when(storageProvider.save(any(Client.class))).thenReturn(client);

            // When
            comptesServiceValidator.update(clientIdentifier, updatedCompte);

            // Then
            verify(storageProvider, times(1)).save(any(Client.class));
        }

        @Test
        @DisplayName("Doit rejeter une mise à jour avec un type INCONNU")
        void shouldRejectUpdateWithInvalidType() {
            // Given
            Compte compteWithInvalidType = existingCompte.toBuilder()
                    .type(TypeCompteEnum.INCONNU)
                    .build();

            // When & Then
            assertThatThrownBy(() -> comptesServiceValidator.update(clientIdentifier, compteWithInvalidType))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Le type communiqué ne correspond pas aux valeurs acceptables");

            verify(storageProvider, never()).save(any(Client.class));
        }

        @Test
        @DisplayName("Doit rejeter une mise à jour avec un nom null")
        void shouldRejectUpdateWithNullName() {
            // Given
            Compte compteWithNullName = existingCompte.toBuilder()
                    .name(null)
                    .build();

            // When & Then
            assertThatThrownBy(() -> comptesServiceValidator.update(clientIdentifier, compteWithNullName))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Au moins une contrainte sur l'objet n'est pas respectée");

            verify(storageProvider, never()).save(any(Client.class));
        }

        @Test
        @DisplayName("Doit rejeter une mise à jour avec un nom invalide")
        void shouldRejectUpdateWithInvalidName() {
            // Given
            Compte compteWithInvalidName = existingCompte.toBuilder()
                    .name("Invalid@Name")
                    .build();

            // When & Then
            assertThatThrownBy(() -> comptesServiceValidator.update(clientIdentifier, compteWithInvalidName))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Au moins une contrainte sur l'objet n'est pas respectée");

            verify(storageProvider, never()).save(any(Client.class));
        }

        @Test
        @DisplayName("Doit permettre de mettre à jour le type vers tous les types acceptables")
        void shouldAllowUpdateToAllAcceptableTypes() throws ImtException {
            // Given
            when(storageProvider.get(clientIdentifier)).thenReturn(Optional.of(client));
            when(storageProvider.save(any(Client.class))).thenReturn(client);

            // Test LIVRET_A
            Compte updatedToLivretA = existingCompte.toBuilder()
                    .type(TypeCompteEnum.LIVRET_A)
                    .build();
            comptesServiceValidator.update(clientIdentifier, updatedToLivretA);

            // Test LDDS
            Compte updatedToLDDS = existingCompte.toBuilder()
                    .type(TypeCompteEnum.LDDS)
                    .build();
            comptesServiceValidator.update(clientIdentifier, updatedToLDDS);

            // Then
            verify(storageProvider, times(2)).save(any(Client.class));
        }

        @Test
        @DisplayName("Doit permettre de mettre à jour uniquement le solde")
        void shouldAllowUpdateOfOnlyBalance() throws ImtException {
            // Given
            Compte updatedCompte = existingCompte.toBuilder()
                    .solde(5000.0)
                    .build();

            when(storageProvider.get(clientIdentifier)).thenReturn(Optional.of(client));
            when(storageProvider.save(any(Client.class))).thenReturn(client);

            // When
            comptesServiceValidator.update(clientIdentifier, updatedCompte);

            // Then
            verify(storageProvider, times(1)).save(any(Client.class));
        }

        @Test
        @DisplayName("Doit permettre de mettre à jour uniquement le nom")
        void shouldAllowUpdateOfOnlyName() throws ImtException {
            // Given
            Compte updatedCompte = existingCompte.toBuilder()
                    .name("Nouveau_Nom")
                    .build();

            when(storageProvider.get(clientIdentifier)).thenReturn(Optional.of(client));
            when(storageProvider.save(any(Client.class))).thenReturn(client);

            // When
            comptesServiceValidator.update(clientIdentifier, updatedCompte);

            // Then
            verify(storageProvider, times(1)).save(any(Client.class));
        }

        @Test
        @DisplayName("Ne doit pas vérifier l'unicité lors de la mise à jour")
        void shouldNotCheckUniquenessOnUpdate() throws ImtException {
            // Given - On ne vérifie pas getAll() lors d'une mise à jour
            Compte updatedCompte = existingCompte.toBuilder()
                    .solde(3000.0)
                    .build();

            when(storageProvider.get(clientIdentifier)).thenReturn(Optional.of(client));
            when(storageProvider.save(any(Client.class))).thenReturn(client);

            // When
            comptesServiceValidator.update(clientIdentifier, updatedCompte);

            // Then
            // Note: get() est appelé par la méthode parente update(), pas par la validation
            verify(storageProvider, times(1)).get(clientIdentifier);
            verify(storageProvider, times(1)).save(any(Client.class));
        }
    }

    @Nested
    @DisplayName("Tests de validation des contraintes")
    class ConstraintValidationTests {

        @Test
        @DisplayName("Doit rejeter un nom trop long (plus de 250 caractères)")
        void shouldRejectTooLongName() {
            // Given
            String tooLongName = "a".repeat(251);
            Compte compteWithTooLongName = Compte.builder()
                    .clientIdentifier(clientIdentifier)
                    .name(tooLongName)
                    .type(TypeCompteEnum.COMPTE_COURANT)
                    .build();

            // When & Then
            assertThatThrownBy(() -> comptesServiceValidator.create(clientIdentifier, compteWithTooLongName))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Au moins une contrainte sur l'objet n'est pas respectée");

            verify(storageProvider, never()).save(any(Client.class));
        }

        @Test
        @DisplayName("Doit accepter un nom avec tiret")
        void shouldAcceptNameWithHyphen() throws ImtException {
            // Given
            Compte compteWithHyphen = Compte.builder()
                    .clientIdentifier(clientIdentifier)
                    .name("Mon-Compte-Courant")
                    .type(TypeCompteEnum.COMPTE_COURANT)
                    .build();

            when(storageProvider.get(clientIdentifier)).thenReturn(Optional.of(client));
            when(storageProvider.save(any(Client.class))).thenReturn(client);

            // When
            Compte result = comptesServiceValidator.create(clientIdentifier, compteWithHyphen);

            // Then
            assertThat(result).isNotNull();
            verify(storageProvider, times(1)).save(any(Client.class));
        }

        @Test
        @DisplayName("Doit accepter un nom avec underscore")
        void shouldAcceptNameWithUnderscore() throws ImtException {
            // Given
            Compte compteWithUnderscore = Compte.builder()
                    .clientIdentifier(clientIdentifier)
                    .name("Mon_Compte_Courant")
                    .type(TypeCompteEnum.COMPTE_COURANT)
                    .build();

            when(storageProvider.get(clientIdentifier)).thenReturn(Optional.of(client));
            when(storageProvider.save(any(Client.class))).thenReturn(client);

            // When
            Compte result = comptesServiceValidator.create(clientIdentifier, compteWithUnderscore);

            // Then
            assertThat(result).isNotNull();
            verify(storageProvider, times(1)).save(any(Client.class));
        }

        @Test
        @DisplayName("Doit accepter un nom avec chiffres")
        void shouldAcceptNameWithNumbers() throws ImtException {
            // Given
            Compte compteWithNumbers = Compte.builder()
                    .clientIdentifier(clientIdentifier)
                    .name("Compte123")
                    .type(TypeCompteEnum.COMPTE_COURANT)
                    .build();

            when(storageProvider.get(clientIdentifier)).thenReturn(Optional.of(client));
            when(storageProvider.save(any(Client.class))).thenReturn(client);

            // When
            Compte result = comptesServiceValidator.create(clientIdentifier, compteWithNumbers);

            // Then
            assertThat(result).isNotNull();
            verify(storageProvider, times(1)).save(any(Client.class));
        }
    }
}
