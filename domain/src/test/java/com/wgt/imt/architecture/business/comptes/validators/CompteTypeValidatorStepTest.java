package com.wgt.imt.architecture.business.comptes.validators;

import com.wgt.imt.architecture.business.common.exception.BadRequestException;
import com.wgt.imt.architecture.business.common.model.TypeCompteEnum;
import com.wgt.imt.architecture.business.common.model.ValidatorResult;
import com.wgt.imt.architecture.business.comptes.model.Compte;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests unitaires pour la classe CompteTypeValidatorStep.
 * Vérifie la validation du type d'un compte bancaire.
 *
 * @author Emmanuel WAGUET
 * @version 1.0
 */
@DisplayName("CompteTypeValidatorStep - Tests unitaires")
class CompteTypeValidatorStepTest {

    private CompteTypeValidatorStep validator;
    private UUID clientIdentifier;

    @BeforeEach
    void setUp() {
        validator = new CompteTypeValidatorStep();
        clientIdentifier = UUID.randomUUID();
    }

    @Nested
    @DisplayName("check() - Vérification du type de compte")
    class CheckTests {

        @Test
        @DisplayName("Doit accepter un compte avec le type COMPTE_COURANT")
        void shouldAcceptCompteWithTypeCompteCourant() throws Exception {
            // Given
            Compte compte = Compte.builder()
                    .clientIdentifier(clientIdentifier)
                    .name("Mon_Compte_Courant")
                    .type(TypeCompteEnum.COMPTE_COURANT)
                    .build();

            // When & Then - Pas d'exception levée
            validator.check(compte);
        }

        @Test
        @DisplayName("Doit accepter un compte avec le type LIVRET_A")
        void shouldAcceptCompteWithTypeLivretA() throws Exception {
            // Given
            Compte compte = Compte.builder()
                    .clientIdentifier(clientIdentifier)
                    .name("Mon_Livret_A")
                    .type(TypeCompteEnum.LIVRET_A)
                    .build();

            // When & Then - Pas d'exception levée
            validator.check(compte);
        }

        @Test
        @DisplayName("Doit accepter un compte avec le type LDDS")
        void shouldAcceptCompteWithTypeLDDS() throws Exception {
            // Given
            Compte compte = Compte.builder()
                    .clientIdentifier(clientIdentifier)
                    .name("Mon_LDDS")
                    .type(TypeCompteEnum.LDDS)
                    .build();

            // When & Then - Pas d'exception levée
            validator.check(compte);
        }

        @Test
        @DisplayName("Doit rejeter un compte avec le type INCONNU")
        void shouldRejectCompteWithTypeInconnu() {
            // Given
            Compte compte = Compte.builder()
                    .clientIdentifier(clientIdentifier)
                    .name("Mon_Compte")
                    .type(TypeCompteEnum.INCONNU)
                    .build();

            // When & Then
            assertThatThrownBy(() -> validator.check(compte))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Le type communiqué ne correspond pas aux valeurs acceptables")
                    .hasMessageContaining(TypeCompteEnum.ACCEPTABLE_VALUES);
        }

        @Test
        @DisplayName("Doit accepter tous les types de comptes acceptables")
        void shouldAcceptAllAcceptableTypes() throws Exception {
            // Given & When & Then - COMPTE_COURANT
            Compte compteCourant = Compte.builder()
                    .clientIdentifier(clientIdentifier)
                    .name("Compte1")
                    .type(TypeCompteEnum.COMPTE_COURANT)
                    .build();
            validator.check(compteCourant);

            // Given & When & Then - LIVRET_A
            Compte livretA = Compte.builder()
                    .clientIdentifier(clientIdentifier)
                    .name("Compte2")
                    .type(TypeCompteEnum.LIVRET_A)
                    .build();
            validator.check(livretA);

            // Given & When & Then - LDDS
            Compte ldds = Compte.builder()
                    .clientIdentifier(clientIdentifier)
                    .name("Compte3")
                    .type(TypeCompteEnum.LDDS)
                    .build();
            validator.check(ldds);
        }
    }

    @Nested
    @DisplayName("validate() - Validation complète")
    class ValidateTests {

        @Test
        @DisplayName("Doit retourner un résultat valide pour le type COMPTE_COURANT")
        void shouldReturnValidResultForTypeCompteCourant() {
            // Given
            Compte compte = Compte.builder()
                    .clientIdentifier(clientIdentifier)
                    .name("Mon_Compte")
                    .type(TypeCompteEnum.COMPTE_COURANT)
                    .build();

            // When
            ValidatorResult result = validator.validate(compte);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.isValid()).isTrue();
            assertThat(result.getExceptionToThrow()).isNull();
        }

        @Test
        @DisplayName("Doit retourner un résultat valide pour le type LIVRET_A")
        void shouldReturnValidResultForTypeLivretA() {
            // Given
            Compte compte = Compte.builder()
                    .clientIdentifier(clientIdentifier)
                    .name("Mon_Livret")
                    .type(TypeCompteEnum.LIVRET_A)
                    .build();

            // When
            ValidatorResult result = validator.validate(compte);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("Doit retourner un résultat valide pour le type LDDS")
        void shouldReturnValidResultForTypeLDDS() {
            // Given
            Compte compte = Compte.builder()
                    .clientIdentifier(clientIdentifier)
                    .name("Mon_LDDS")
                    .type(TypeCompteEnum.LDDS)
                    .build();

            // When
            ValidatorResult result = validator.validate(compte);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("Doit retourner un résultat invalide pour le type INCONNU")
        void shouldReturnInvalidResultForTypeInconnu() {
            // Given
            Compte compte = Compte.builder()
                    .clientIdentifier(clientIdentifier)
                    .name("Mon_Compte")
                    .type(TypeCompteEnum.INCONNU)
                    .build();

            // When
            ValidatorResult result = validator.validate(compte);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.isValid()).isFalse();
            assertThat(result.getExceptionToThrow())
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Le type communiqué ne correspond pas aux valeurs acceptables");
        }

        @Test
        @DisplayName("Doit lever une exception lors de throwIfInvalid pour le type INCONNU")
        void shouldThrowExceptionWhenThrowIfInvalidForTypeInconnu() {
            // Given
            Compte compte = Compte.builder()
                    .clientIdentifier(clientIdentifier)
                    .name("Mon_Compte")
                    .type(TypeCompteEnum.INCONNU)
                    .build();

            // When
            ValidatorResult result = validator.validate(compte);

            // Then
            assertThatThrownBy(result::throwIfInvalid)
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Le type communiqué ne correspond pas aux valeurs acceptables");
        }

        @Test
        @DisplayName("Doit rejeter un compte null")
        void shouldRejectNullCompte() {
            // When & Then
            assertThatThrownBy(() -> validator.validate(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Impossible de valider un objet nul");
        }
    }

    @Nested
    @DisplayName("Tests avec différentes configurations de comptes")
    class DifferentAccountConfigurationsTests {

        @Test
        @DisplayName("Doit valider indépendamment du nom du compte")
        void shouldValidateIndependentlyOfAccountName() throws Exception {
            // Given - Noms différents mais type valide
            Compte compte1 = Compte.builder()
                    .clientIdentifier(clientIdentifier)
                    .name("Compte_Principal")
                    .type(TypeCompteEnum.COMPTE_COURANT)
                    .build();

            Compte compte2 = Compte.builder()
                    .clientIdentifier(clientIdentifier)
                    .name("Mon-Epargne")
                    .type(TypeCompteEnum.LIVRET_A)
                    .build();

            // When & Then
            validator.check(compte1);
            validator.check(compte2);
        }

        @Test
        @DisplayName("Doit valider indépendamment du solde du compte")
        void shouldValidateIndependentlyOfAccountBalance() throws Exception {
            // Given
            Compte compteAvecSolde = Compte.builder()
                    .clientIdentifier(clientIdentifier)
                    .name("Mon_Compte")
                    .type(TypeCompteEnum.COMPTE_COURANT)
                    .solde(1000.0)
                    .build();

            // When & Then
            validator.check(compteAvecSolde);
        }

        @Test
        @DisplayName("Doit valider indépendamment de l'identifiant du client")
        void shouldValidateIndependentlyOfClientIdentifier() throws Exception {
            // Given
            UUID autreClient = UUID.randomUUID();
            Compte compte = Compte.builder()
                    .clientIdentifier(autreClient)
                    .name("Mon_Compte")
                    .type(TypeCompteEnum.LDDS)
                    .build();

            // When & Then
            validator.check(compte);
        }
    }
}

