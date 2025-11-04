package com.wgt.imt.architecture.business.comptes.validators;

import com.wgt.imt.architecture.business.clients.ClientStorageProvider;
import com.wgt.imt.architecture.business.clients.model.Client;
import com.wgt.imt.architecture.business.common.exception.ConflictException;
import com.wgt.imt.architecture.business.common.model.GenreEnum;
import com.wgt.imt.architecture.business.common.model.TypeCompteEnum;
import com.wgt.imt.architecture.business.common.model.ValidatorResult;
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
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour la classe CompteAlreadyExistValidatorStep.
 * Vérifie la validation de l'unicité d'un compte pour un client.
 *
 * @author Emmanuel WAGUET
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CompteAlreadyExistValidatorStep - Tests unitaires")
class CompteAlreadyExistValidatorStepTest {

    @Mock
    private ClientStorageProvider storageProvider;

    private CompteAlreadyExistValidatorStep validator;

    private UUID clientIdentifier;
    private Client client;
    private Compte existingCompte1;
    private Compte existingCompte2;

    @BeforeEach
    void setUp() {
        clientIdentifier = UUID.randomUUID();

        existingCompte1 = Compte.builder()
                .clientIdentifier(clientIdentifier)
                .name("Compte_Courant")
                .type(TypeCompteEnum.COMPTE_COURANT)
                .build();

        existingCompte2 = Compte.builder()
                .clientIdentifier(clientIdentifier)
                .name("Livret_Epargne")
                .type(TypeCompteEnum.LIVRET_A)
                .build();

        Set<Compte> comptes = new HashSet<>(Arrays.asList(existingCompte1, existingCompte2));

        client = Client.builder()
                .lastname("Dupont")
                .firstname("Jean")
                .genre(GenreEnum.HOMME)
                .comptes(comptes)
                .build();

        validator = new CompteAlreadyExistValidatorStep(storageProvider, clientIdentifier);
    }

    @Nested
    @DisplayName("check() - Vérification de l'unicité du compte")
    class CheckTests {

        @Test
        @DisplayName("Doit accepter un compte qui n'existe pas")
        void shouldAcceptCompteThatDoesNotExist() throws Exception {
            // Given
            Compte newCompte = Compte.builder()
                    .clientIdentifier(clientIdentifier)
                    .name("Nouveau_Compte")
                    .type(TypeCompteEnum.LDDS)
                    .build();

            when(storageProvider.get(clientIdentifier)).thenReturn(Optional.of(client));

            // When & Then - Pas d'exception levée
            validator.check(newCompte);
            verify(storageProvider, times(1)).get(clientIdentifier);
        }

        @Test
        @DisplayName("Doit accepter un compte quand le client n'a aucun compte")
        void shouldAcceptCompteWhenClientHasNoAccounts() throws Exception {
            // Given
            Compte newCompte = Compte.builder()
                    .clientIdentifier(clientIdentifier)
                    .name("Premier_Compte")
                    .type(TypeCompteEnum.COMPTE_COURANT)
                    .build();

            Client clientSansComptes = Client.builder()
                    .lastname("Martin")
                    .firstname("Marie")
                    .genre(GenreEnum.FEMME)
                    .comptes(Collections.emptySet())
                    .build();

            when(storageProvider.get(clientIdentifier)).thenReturn(Optional.of(clientSansComptes));

            // When & Then - Pas d'exception levée
            validator.check(newCompte);
            verify(storageProvider, times(1)).get(clientIdentifier);
        }

        @Test
        @DisplayName("Doit accepter un compte quand le client n'existe pas")
        void shouldAcceptCompteWhenClientDoesNotExist() throws Exception {
            // Given
            Compte newCompte = Compte.builder()
                    .clientIdentifier(clientIdentifier)
                    .name("Nouveau_Compte")
                    .type(TypeCompteEnum.COMPTE_COURANT)
                    .build();

            when(storageProvider.get(clientIdentifier)).thenReturn(Optional.empty());

            // When & Then - Pas d'exception levée
            validator.check(newCompte);
            verify(storageProvider, times(1)).get(clientIdentifier);
        }

        @Test
        @DisplayName("Doit rejeter un compte qui existe déjà (même nom et même type)")
        void shouldRejectCompteThatAlreadyExists() {
            // Given
            Compte duplicateCompte = Compte.builder()
                    .clientIdentifier(clientIdentifier)
                    .name("Compte_Courant")
                    .type(TypeCompteEnum.COMPTE_COURANT)
                    .build();

            when(storageProvider.get(clientIdentifier)).thenReturn(Optional.of(client));

            // When & Then
            assertThatThrownBy(() -> validator.check(duplicateCompte))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("Un compte ayant les mêmes caractéristique existe déjà pour le client")
                    .hasMessageContaining(clientIdentifier.toString())
                    .hasMessageContaining("name : Compte_Courant")
                    .hasMessageContaining("type : COMPTE_COURANT");

            verify(storageProvider, times(1)).get(clientIdentifier);
        }

        @Test
        @DisplayName("Doit rejeter un compte avec le même nom en ignorant la casse")
        void shouldRejectCompteIgnoringCase() {
            // Given - Nom avec différentes casses
            Compte compteWithDifferentCase = Compte.builder()
                    .clientIdentifier(clientIdentifier)
                    .name("COMPTE_COURANT") // Majuscules
                    .type(TypeCompteEnum.COMPTE_COURANT)
                    .build();

            when(storageProvider.get(clientIdentifier)).thenReturn(Optional.of(client));

            // When & Then
            assertThatThrownBy(() -> validator.check(compteWithDifferentCase))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("Un compte ayant les mêmes caractéristique existe déjà pour le client");

            verify(storageProvider, times(1)).get(clientIdentifier);
        }

        @Test
        @DisplayName("Doit rejeter un compte avec nom en minuscules")
        void shouldRejectCompteWithLowercaseName() {
            // Given
            Compte compteWithLowercaseName = Compte.builder()
                    .clientIdentifier(clientIdentifier)
                    .name("compte_courant") // minuscules
                    .type(TypeCompteEnum.COMPTE_COURANT)
                    .build();

            when(storageProvider.get(clientIdentifier)).thenReturn(Optional.of(client));

            // When & Then
            assertThatThrownBy(() -> validator.check(compteWithLowercaseName))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("Un compte ayant les mêmes caractéristique existe déjà pour le client");

            verify(storageProvider, times(1)).get(clientIdentifier);
        }

        @Test
        @DisplayName("Doit accepter un compte avec le même nom mais un type différent")
        void shouldAcceptCompteWithSameNameButDifferentType() throws Exception {
            // Given - Même nom mais type différent
            Compte compteWithDifferentType = Compte.builder()
                    .clientIdentifier(clientIdentifier)
                    .name("Compte_Courant")
                    .type(TypeCompteEnum.LIVRET_A) // Type différent
                    .build();

            when(storageProvider.get(clientIdentifier)).thenReturn(Optional.of(client));

            // When & Then - Pas d'exception levée
            validator.check(compteWithDifferentType);
            verify(storageProvider, times(1)).get(clientIdentifier);
        }

        @Test
        @DisplayName("Doit accepter un compte avec un nom différent mais le même type")
        void shouldAcceptCompteWithDifferentNameButSameType() throws Exception {
            // Given - Nom différent mais même type
            Compte compteWithDifferentName = Compte.builder()
                    .clientIdentifier(clientIdentifier)
                    .name("Autre_Compte_Courant")
                    .type(TypeCompteEnum.COMPTE_COURANT) // Même type
                    .build();

            when(storageProvider.get(clientIdentifier)).thenReturn(Optional.of(client));

            // When & Then - Pas d'exception levée
            validator.check(compteWithDifferentName);
            verify(storageProvider, times(1)).get(clientIdentifier);
        }

        @Test
        @DisplayName("Doit rejeter un compte quand plusieurs comptes existent et qu'un correspond")
        void shouldRejectCompteWhenMultipleAccountsExistAndOneMatches() {
            // Given
            Compte duplicateCompte = Compte.builder()
                    .clientIdentifier(clientIdentifier)
                    .name("Livret_Epargne")
                    .type(TypeCompteEnum.LIVRET_A)
                    .build();

            when(storageProvider.get(clientIdentifier)).thenReturn(Optional.of(client));

            // When & Then
            assertThatThrownBy(() -> validator.check(duplicateCompte))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("Un compte ayant les mêmes caractéristique existe déjà pour le client")
                    .hasMessageContaining("name : Livret_Epargne")
                    .hasMessageContaining("type : LIVRET_A");

            verify(storageProvider, times(1)).get(clientIdentifier);
        }

        @Test
        @DisplayName("Doit accepter un compte avec tous les types acceptables si nom différent")
        void shouldAcceptCompteWithAllAcceptableTypesIfNameDifferent() throws Exception {
            // Given
            when(storageProvider.get(clientIdentifier)).thenReturn(Optional.of(client));

            // Test COMPTE_COURANT
            Compte compteCourant = Compte.builder()
                    .clientIdentifier(clientIdentifier)
                    .name("Nouveau_Compte_Courant")
                    .type(TypeCompteEnum.COMPTE_COURANT)
                    .build();
            validator.check(compteCourant);

            // Test LIVRET_A
            Compte livretA = Compte.builder()
                    .clientIdentifier(clientIdentifier)
                    .name("Nouveau_Livret_A")
                    .type(TypeCompteEnum.LIVRET_A)
                    .build();
            validator.check(livretA);

            // Test LDDS
            Compte ldds = Compte.builder()
                    .clientIdentifier(clientIdentifier)
                    .name("Nouveau_LDDS")
                    .type(TypeCompteEnum.LDDS)
                    .build();
            validator.check(ldds);

            // Then
            verify(storageProvider, times(3)).get(clientIdentifier);
        }

        @Test
        @DisplayName("Doit accepter un compte quand les comptes du client sont null")
        void shouldAcceptCompteWhenClientAccountsAreNull() throws Exception {
            // Given
            Compte newCompte = Compte.builder()
                    .clientIdentifier(clientIdentifier)
                    .name("Nouveau_Compte")
                    .type(TypeCompteEnum.COMPTE_COURANT)
                    .build();

            Client clientAvecComptesNull = Client.builder()
                    .lastname("Test")
                    .firstname("Test")
                    .genre(GenreEnum.HOMME)
                    .comptes(null)
                    .build();

            when(storageProvider.get(clientIdentifier)).thenReturn(Optional.of(clientAvecComptesNull));

            // When & Then - Pas d'exception levée
            validator.check(newCompte);
            verify(storageProvider, times(1)).get(clientIdentifier);
        }
    }

    @Nested
    @DisplayName("validate() - Validation complète")
    class ValidateTests {

        @Test
        @DisplayName("Doit retourner un résultat valide pour un nouveau compte")
        void shouldReturnValidResultForNewCompte() {
            // Given
            Compte newCompte = Compte.builder()
                    .clientIdentifier(clientIdentifier)
                    .name("Nouveau_Compte")
                    .type(TypeCompteEnum.LDDS)
                    .build();

            when(storageProvider.get(clientIdentifier)).thenReturn(Optional.of(client));

            // When
            ValidatorResult result = validator.validate(newCompte);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.isValid()).isTrue();
            assertThat(result.getExceptionToThrow()).isNull();
            verify(storageProvider, times(1)).get(clientIdentifier);
        }

        @Test
        @DisplayName("Doit retourner un résultat invalide pour un compte qui existe déjà")
        void shouldReturnInvalidResultForExistingCompte() {
            // Given
            Compte duplicateCompte = Compte.builder()
                    .clientIdentifier(clientIdentifier)
                    .name("Compte_Courant")
                    .type(TypeCompteEnum.COMPTE_COURANT)
                    .build();

            when(storageProvider.get(clientIdentifier)).thenReturn(Optional.of(client));

            // When
            ValidatorResult result = validator.validate(duplicateCompte);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.isValid()).isFalse();
            assertThat(result.getExceptionToThrow())
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("Un compte ayant les mêmes caractéristique existe déjà pour le client");
            verify(storageProvider, times(1)).get(clientIdentifier);
        }

        @Test
        @DisplayName("Doit lever une exception lors de throwIfInvalid pour un compte existant")
        void shouldThrowExceptionWhenThrowIfInvalidForExistingCompte() {
            // Given
            Compte duplicateCompte = Compte.builder()
                    .clientIdentifier(clientIdentifier)
                    .name("Compte_Courant")
                    .type(TypeCompteEnum.COMPTE_COURANT)
                    .build();

            when(storageProvider.get(clientIdentifier)).thenReturn(Optional.of(client));

            // When
            ValidatorResult result = validator.validate(duplicateCompte);

            // Then
            assertThatThrownBy(result::throwIfInvalid)
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("Un compte ayant les mêmes caractéristique existe déjà pour le client");
        }

        @Test
        @DisplayName("Doit rejeter un compte null")
        void shouldRejectNullCompte() {
            // When & Then
            assertThatThrownBy(() -> validator.validate(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Impossible de valider un objet nul");

            verify(storageProvider, never()).get(any());
        }

        @Test
        @DisplayName("Doit retourner un résultat valide quand le client n'a pas de comptes")
        void shouldReturnValidResultWhenClientHasNoAccounts() {
            // Given
            Compte newCompte = Compte.builder()
                    .clientIdentifier(clientIdentifier)
                    .name("Premier_Compte")
                    .type(TypeCompteEnum.COMPTE_COURANT)
                    .build();

            Client clientSansComptes = Client.builder()
                    .lastname("Martin")
                    .firstname("Marie")
                    .genre(GenreEnum.FEMME)
                    .comptes(Collections.emptySet())
                    .build();

            when(storageProvider.get(clientIdentifier)).thenReturn(Optional.of(clientSansComptes));

            // When
            ValidatorResult result = validator.validate(newCompte);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.isValid()).isTrue();
            verify(storageProvider, times(1)).get(clientIdentifier);
        }
    }

    @Nested
    @DisplayName("Tests d'intégration avec chaînage de validateurs")
    class ChainTests {

        @Test
        @DisplayName("Doit chaîner avec un autre validateur et exécuter les deux")
        void shouldChainWithAnotherValidator() {
            // Given
            CompteTypeValidatorStep typeValidator = new CompteTypeValidatorStep();
            validator.linkWith(typeValidator);

            Compte compte = Compte.builder()
                    .clientIdentifier(clientIdentifier)
                    .name("Nouveau_Compte")
                    .type(TypeCompteEnum.COMPTE_COURANT)
                    .build();

            when(storageProvider.get(clientIdentifier)).thenReturn(Optional.of(client));

            // When
            ValidatorResult result = validator.validate(compte);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.isValid()).isTrue();
            verify(storageProvider, times(1)).get(clientIdentifier);
        }

        @Test
        @DisplayName("Doit échouer sur le premier validateur si le compte existe déjà")
        void shouldFailOnFirstValidatorIfCompteExists() {
            // Given
            CompteTypeValidatorStep typeValidator = new CompteTypeValidatorStep();
            validator.linkWith(typeValidator);

            Compte duplicateCompte = Compte.builder()
                    .clientIdentifier(clientIdentifier)
                    .name("Compte_Courant")
                    .type(TypeCompteEnum.COMPTE_COURANT)
                    .build();

            when(storageProvider.get(clientIdentifier)).thenReturn(Optional.of(client));

            // When
            ValidatorResult result = validator.validate(duplicateCompte);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.isValid()).isFalse();
            assertThat(result.getExceptionToThrow()).isInstanceOf(ConflictException.class);
            verify(storageProvider, times(1)).get(clientIdentifier);
        }

        @Test
        @DisplayName("Doit échouer sur le second validateur si le type est INCONNU")
        void shouldFailOnSecondValidatorIfTypeIsInconnu() {
            // Given
            CompteTypeValidatorStep typeValidator = new CompteTypeValidatorStep();
            validator.linkWith(typeValidator);

            Compte compteWithInvalidType = Compte.builder()
                    .clientIdentifier(clientIdentifier)
                    .name("Nouveau_Compte")
                    .type(TypeCompteEnum.INCONNU)
                    .build();

            when(storageProvider.get(clientIdentifier)).thenReturn(Optional.of(client));

            // When
            ValidatorResult result = validator.validate(compteWithInvalidType);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.isValid()).isFalse();
            assertThat(result.getExceptionToThrow())
                    .isInstanceOf(com.wgt.imt.architecture.business.common.exception.BadRequestException.class)
                    .hasMessageContaining("Le type communiqué ne correspond pas aux valeurs acceptables");
            verify(storageProvider, times(1)).get(clientIdentifier);
        }
    }

    @Nested
    @DisplayName("Tests avec différents identifiants de clients")
    class DifferentClientIdentifiersTests {

        @Test
        @DisplayName("Doit valider pour le bon client spécifié dans le validateur")
        void shouldValidateForCorrectClient() throws Exception {
            // Given
            UUID autreClientId = UUID.randomUUID();
            CompteAlreadyExistValidatorStep autreValidator = new CompteAlreadyExistValidatorStep(storageProvider, autreClientId);

            Compte newCompte = Compte.builder()
                    .clientIdentifier(autreClientId)
                    .name("Compte_Test")
                    .type(TypeCompteEnum.COMPTE_COURANT)
                    .build();

            Client autreClient = Client.builder()
                    .lastname("Bernard")
                    .firstname("Paul")
                    .genre(GenreEnum.HOMME)
                    .comptes(Collections.emptySet())
                    .build();

            when(storageProvider.get(autreClientId)).thenReturn(Optional.of(autreClient));

            // When & Then - Pas d'exception levée
            autreValidator.check(newCompte);
            verify(storageProvider, times(1)).get(autreClientId);
            verify(storageProvider, never()).get(clientIdentifier);
        }

        @Test
        @DisplayName("Doit vérifier l'unicité uniquement pour le client spécifié")
        void shouldCheckUniquenessOnlyForSpecifiedClient() throws Exception {
            // Given - Un compte avec le même nom existe pour un autre client
            UUID autreClientId = UUID.randomUUID();

            Compte newCompte = Compte.builder()
                    .clientIdentifier(clientIdentifier)
                    .name("Compte_Courant") // Même nom qu'un compte existant
                    .type(TypeCompteEnum.LDDS) // Mais type différent
                    .build();

            when(storageProvider.get(clientIdentifier)).thenReturn(Optional.of(client));

            // When & Then - Doit passer car le type est différent
            validator.check(newCompte);
            verify(storageProvider, times(1)).get(clientIdentifier);
        }
    }
}

