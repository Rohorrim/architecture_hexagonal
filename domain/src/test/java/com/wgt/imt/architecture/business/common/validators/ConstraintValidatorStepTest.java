package com.wgt.imt.architecture.business.common.validators;

import com.wgt.imt.architecture.business.common.exception.BadRequestException;
import com.wgt.imt.architecture.business.common.model.ValidatorResult;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests unitaires pour la classe ConstraintValidatorStep.
 * Vérifie la validation des contraintes Jakarta Validation.
 *
 * @author Emmanuel WAGUET
 * @version 1.0
 */
@DisplayName("ConstraintValidatorStep - Tests unitaires")
class ConstraintValidatorStepTest {

    private ConstraintValidatorStep<TestObject> validator;

    @BeforeEach
    void setUp() {
        validator = new ConstraintValidatorStep<>();
    }

    @Nested
    @DisplayName("check() - Vérification des contraintes")
    class CheckTests {

        @Test
        @DisplayName("Doit accepter un objet valide")
        void shouldAcceptValidObject() throws Exception {
            // Given
            TestObject validObject = TestObject.builder()
                    .name("John")
                    .email("john@example.com")
                    .age(25)
                    .build();

            // When & Then - Pas d'exception levée
            validator.check(validObject);
        }

        @Test
        @DisplayName("Doit rejeter un objet avec un champ null annoté @NotNull")
        void shouldRejectObjectWithNullNotNullField() {
            // Given
            TestObject invalidObject = TestObject.builder()
                    .name(null) // Violation @NotNull
                    .email("john@example.com")
                    .age(25)
                    .build();

            // When & Then
            assertThatThrownBy(() -> validator.check(invalidObject))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Au moins une contrainte sur l'objet n'est pas respectée")
                    .hasMessageContaining("Le nom ne peut pas être nul");
        }

        @Test
        @DisplayName("Doit rejeter un objet avec un pattern invalide")
        void shouldRejectObjectWithInvalidPattern() {
            // Given
            TestObject invalidObject = TestObject.builder()
                    .name("John")
                    .email("invalid-email") // Violation @Pattern
                    .age(25)
                    .build();

            // When & Then
            assertThatThrownBy(() -> validator.check(invalidObject))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Au moins une contrainte sur l'objet n'est pas respectée")
                    .hasMessageContaining("L'email n'est pas valide");
        }

        @Test
        @DisplayName("Doit rejeter un objet avec une taille invalide")
        void shouldRejectObjectWithInvalidSize() {
            // Given
            TestObject invalidObject = TestObject.builder()
                    .name("Jo") // Violation @Size (min=3)
                    .email("john@example.com")
                    .age(25)
                    .build();

            // When & Then
            assertThatThrownBy(() -> validator.check(invalidObject))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Au moins une contrainte sur l'objet n'est pas respectée")
                    .hasMessageContaining("Le nom doit contenir entre 3 et 50 caractères");
        }

        @Test
        @DisplayName("Doit rejeter un objet avec plusieurs violations")
        void shouldRejectObjectWithMultipleViolations() {
            // Given
            TestObject invalidObject = TestObject.builder()
                    .name(null) // Violation @NotNull
                    .email("invalid") // Violation @Pattern
                    .age(25)
                    .build();

            // When & Then
            assertThatThrownBy(() -> validator.check(invalidObject))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Au moins une contrainte sur l'objet n'est pas respectée");
        }

        @Test
        @DisplayName("Doit accepter un objet avec toutes les contraintes respectées")
        void shouldAcceptObjectWithAllConstraintsSatisfied() throws Exception {
            // Given
            TestObject validObject = TestObject.builder()
                    .name("John Doe")
                    .email("john.doe@example.com")
                    .age(30)
                    .build();

            // When & Then - Pas d'exception levée
            validator.check(validObject);
        }

        @Test
        @DisplayName("Doit accepter un objet avec des valeurs limites valides")
        void shouldAcceptObjectWithBoundaryValues() throws Exception {
            // Given
            TestObject validObject = TestObject.builder()
                    .name("ABC") // Exactement 3 caractères (min)
                    .email("test@hotmail.com")
                    .age(0)
                    .build();

            // When & Then - Pas d'exception levée
            validator.check(validObject);
        }

        @Test
        @DisplayName("Doit rejeter un nom trop long")
        void shouldRejectNameTooLong() {
            // Given
            String longName = "A".repeat(51); // Plus de 50 caractères
            TestObject invalidObject = TestObject.builder()
                    .name(longName)
                    .email("john@example.com")
                    .age(25)
                    .build();

            // When & Then
            assertThatThrownBy(() -> validator.check(invalidObject))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Au moins une contrainte sur l'objet n'est pas respectée")
                    .hasMessageContaining("Le nom doit contenir entre 3 et 50 caractères");
        }
    }

    @Nested
    @DisplayName("validate() - Validation complète")
    class ValidateTests {

        @Test
        @DisplayName("Doit retourner un résultat valide pour un objet valide")
        void shouldReturnValidResultForValidObject() {
            // Given
            TestObject validObject = TestObject.builder()
                    .name("John")
                    .email("john@example.com")
                    .age(25)
                    .build();

            // When
            ValidatorResult result = validator.validate(validObject);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.isValid()).isTrue();
            assertThat(result.getExceptionToThrow()).isNull();
        }

        @Test
        @DisplayName("Doit retourner un résultat invalide pour un objet invalide")
        void shouldReturnInvalidResultForInvalidObject() {
            // Given
            TestObject invalidObject = TestObject.builder()
                    .name(null) // Violation
                    .email("john@example.com")
                    .age(25)
                    .build();

            // When
            ValidatorResult result = validator.validate(invalidObject);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.isValid()).isFalse();
            assertThat(result.getExceptionToThrow())
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Au moins une contrainte sur l'objet n'est pas respectée");
        }

        @Test
        @DisplayName("Doit lever une exception lors de throwIfInvalid pour un objet invalide")
        void shouldThrowExceptionWhenThrowIfInvalidForInvalidObject() {
            // Given
            TestObject invalidObject = TestObject.builder()
                    .name(null)
                    .email("john@example.com")
                    .age(25)
                    .build();

            // When
            ValidatorResult result = validator.validate(invalidObject);

            // Then
            assertThatThrownBy(result::throwIfInvalid)
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Au moins une contrainte sur l'objet n'est pas respectée");
        }

        @Test
        @DisplayName("Doit rejeter un objet null")
        void shouldRejectNullObject() {
            // When & Then
            assertThatThrownBy(() -> validator.validate(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Impossible de valider un objet nul");
        }
    }

    @Nested
    @DisplayName("Tests avec différents types d'objets")
    class DifferentObjectTypesTests {

        @Test
        @DisplayName("Doit valider un objet simple sans contraintes")
        void shouldValidateSimpleObjectWithoutConstraints() throws Exception {
            // Given
            ConstraintValidatorStep<SimpleObject> simpleValidator = new ConstraintValidatorStep<>();
            SimpleObject simpleObject = new SimpleObject("test");

            // When & Then - Pas d'exception levée
            simpleValidator.check(simpleObject);
        }

        @Test
        @DisplayName("Doit valider un objet avec contraintes complexes")
        void shouldValidateComplexObject() throws Exception {
            // Given
            TestObject validObject = TestObject.builder()
                    .name("Jane Smith")
                    .email("jane.smith@company.co.uk")
                    .age(45)
                    .build();

            // When & Then - Pas d'exception levée
            validator.check(validObject);
        }

        @Test
        @DisplayName("Doit gérer plusieurs types d'objets avec le même validateur générique")
        void shouldHandleMultipleObjectTypes() throws Exception {
            // Given
            TestObject testObject = TestObject.builder()
                    .name("Test")
                    .email("test@test.com")
                    .age(20)
                    .build();

            ConstraintValidatorStep<Object> genericValidator = new ConstraintValidatorStep<>();

            // When & Then - Pas d'exception levée
            genericValidator.check(testObject);
        }
    }

    @Nested
    @DisplayName("Tests d'intégration avec chaînage")
    class ChainTests {

        @Test
        @DisplayName("Doit chaîner avec un autre validateur")
        void shouldChainWithAnotherValidator() {
            // Given
            CustomValidator customValidator = new CustomValidator();
            validator.linkWith(customValidator);

            TestObject validObject = TestObject.builder()
                    .name("John")
                    .email("john@example.com")
                    .age(25)
                    .build();

            // When
            ValidatorResult result = validator.validate(validObject);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("Doit échouer sur le premier validateur si les contraintes ne sont pas respectées")
        void shouldFailOnFirstValidatorIfConstraintsNotMet() {
            // Given
            CustomValidator customValidator = new CustomValidator();
            validator.linkWith(customValidator);

            TestObject invalidObject = TestObject.builder()
                    .name(null) // Violation
                    .email("john@example.com")
                    .age(25)
                    .build();

            // When
            ValidatorResult result = validator.validate(invalidObject);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.isValid()).isFalse();
            assertThat(result.getExceptionToThrow()).isInstanceOf(BadRequestException.class);
        }

        @Test
        @DisplayName("Doit exécuter le second validateur si le premier passe")
        void shouldExecuteSecondValidatorIfFirstPasses() {
            // Given
            CustomValidator customValidator = new CustomValidator();
            validator.linkWith(customValidator);

            TestObject objectThatFailsCustomValidation = TestObject.builder()
                    .name("FAIL") // Déclenche la validation custom
                    .email("john@example.com")
                    .age(25)
                    .build();

            // When
            ValidatorResult result = validator.validate(objectThatFailsCustomValidation);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.isValid()).isFalse();
            assertThat(result.getExceptionToThrow())
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Custom validation failed");
        }
    }

    // ========== Classes de test ==========

    /**
     * Objet de test avec contraintes de validation.
     */
    @Getter
    @Builder
    @AllArgsConstructor
    static class TestObject {
        @NotNull(message = "Le nom ne peut pas être nul")
        @Size(min = 3, max = 50, message = "Le nom doit contenir entre 3 et 50 caractères")
        private String name;

        @NotNull(message = "L'email ne peut pas être nul")
        @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
                message = "L'email n'est pas valide")
        private String email;

        private Integer age;
    }

    /**
     * Objet simple sans contraintes.
     */
    @AllArgsConstructor
    static class SimpleObject {
        private String value;
    }

    /**
     * Validateur custom pour les tests de chaînage.
     */
    static class CustomValidator extends AbstractValidatorStep<TestObject> {
        @Override
        public void check(TestObject toValidate) throws com.wgt.imt.architecture.business.common.exception.ImtException {
            if ("FAIL".equals(toValidate.getName())) {
                throw new BadRequestException("Custom validation failed");
            }
        }
    }
}

