package com.wgt.imt.architecture.business.clients.validators;

import com.wgt.imt.architecture.business.clients.model.Client;
import com.wgt.imt.architecture.business.common.exception.BadRequestException;
import com.wgt.imt.architecture.business.common.model.GenreEnum;
import com.wgt.imt.architecture.business.common.model.ValidatorResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests unitaires pour la classe ClientGenreValidatorStep.
 * Vérifie la validation du genre d'un client.
 *
 * @author Emmanuel WAGUET
 * @version 1.0
 */
@DisplayName("ClientGenreValidatorStep - Tests unitaires")
class ClientGenreValidatorStepTest {

    private ClientGenreValidatorStep validator;

    @BeforeEach
    void setUp() {
        validator = new ClientGenreValidatorStep();
    }

    @Nested
    @DisplayName("check() - Vérification du genre")
    class CheckTests {

        @Test
        @DisplayName("Doit accepter un client avec le genre HOMME")
        void shouldAcceptClientWithGenreHomme() throws Exception {
            // Given
            Client client = Client.builder()
                    .lastname("Dupont")
                    .firstname("Jean")
                    .genre(GenreEnum.HOMME)
                    .comptes(Collections.emptySet())
                    .build();

            // When & Then - Pas d'exception levée
            validator.check(client);
        }

        @Test
        @DisplayName("Doit accepter un client avec le genre FEMME")
        void shouldAcceptClientWithGenreFemme() throws Exception {
            // Given
            Client client = Client.builder()
                    .lastname("Martin")
                    .firstname("Marie")
                    .genre(GenreEnum.FEMME)
                    .comptes(Collections.emptySet())
                    .build();

            // When & Then - Pas d'exception levée
            validator.check(client);
        }

        @Test
        @DisplayName("Doit accepter un client avec le genre NON_COMMUNIQUER")
        void shouldAcceptClientWithGenreNonCommuniquer() throws Exception {
            // Given
            Client client = Client.builder()
                    .lastname("Bernard")
                    .firstname("Alex")
                    .genre(GenreEnum.NON_COMMUNIQUER)
                    .comptes(Collections.emptySet())
                    .build();

            // When & Then - Pas d'exception levée
            validator.check(client);
        }

        @Test
        @DisplayName("Doit rejeter un client avec le genre INCONNU")
        void shouldRejectClientWithGenreInconnu() {
            // Given
            Client client = Client.builder()
                    .lastname("Durand")
                    .firstname("Pierre")
                    .genre(GenreEnum.INCONNU)
                    .comptes(Collections.emptySet())
                    .build();

            // When & Then
            assertThatThrownBy(() -> validator.check(client))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Le genre communiqué ne correspond pas aux valeurs acceptables")
                    .hasMessageContaining(GenreEnum.ACCEPTABLE_VALUES);
        }
    }

    @Nested
    @DisplayName("validate() - Validation complète")
    class ValidateTests {

        @Test
        @DisplayName("Doit retourner un résultat valide pour le genre HOMME")
        void shouldReturnValidResultForGenreHomme() {
            // Given
            Client client = Client.builder()
                    .lastname("Dupont")
                    .firstname("Jean")
                    .genre(GenreEnum.HOMME)
                    .comptes(Collections.emptySet())
                    .build();

            // When
            ValidatorResult result = validator.validate(client);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.isValid()).isTrue();
            assertThat(result.getExceptionToThrow()).isNull();
        }

        @Test
        @DisplayName("Doit retourner un résultat valide pour le genre FEMME")
        void shouldReturnValidResultForGenreFemme() {
            // Given
            Client client = Client.builder()
                    .lastname("Martin")
                    .firstname("Marie")
                    .genre(GenreEnum.FEMME)
                    .comptes(Collections.emptySet())
                    .build();

            // When
            ValidatorResult result = validator.validate(client);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("Doit retourner un résultat valide pour le genre NON_COMMUNIQUER")
        void shouldReturnValidResultForGenreNonCommuniquer() {
            // Given
            Client client = Client.builder()
                    .lastname("Bernard")
                    .firstname("Alex")
                    .genre(GenreEnum.NON_COMMUNIQUER)
                    .comptes(Collections.emptySet())
                    .build();

            // When
            ValidatorResult result = validator.validate(client);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("Doit retourner un résultat invalide pour le genre INCONNU")
        void shouldReturnInvalidResultForGenreInconnu() {
            // Given
            Client client = Client.builder()
                    .lastname("Durand")
                    .firstname("Pierre")
                    .genre(GenreEnum.INCONNU)
                    .comptes(Collections.emptySet())
                    .build();

            // When
            ValidatorResult result = validator.validate(client);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.isValid()).isFalse();
            assertThat(result.getExceptionToThrow())
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Le genre communiqué ne correspond pas aux valeurs acceptables");
        }

        @Test
        @DisplayName("Doit lever une exception lors de throwIfInvalid pour le genre INCONNU")
        void shouldThrowExceptionWhenThrowIfInvalidForGenreInconnu() {
            // Given
            Client client = Client.builder()
                    .lastname("Durand")
                    .firstname("Pierre")
                    .genre(GenreEnum.INCONNU)
                    .comptes(Collections.emptySet())
                    .build();

            // When
            ValidatorResult result = validator.validate(client);

            // Then
            assertThatThrownBy(result::throwIfInvalid)
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Le genre communiqué ne correspond pas aux valeurs acceptables");
        }

        @Test
        @DisplayName("Doit rejeter un client null")
        void shouldRejectNullClient() {
            // When & Then
            assertThatThrownBy(() -> validator.validate(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Impossible de valider un objet nul");
        }
    }
}

