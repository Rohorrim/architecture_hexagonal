package com.wgt.imt.architecture.business.common.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitaires pour la classe CollectionImtUtils.
 * Vérifie le bon fonctionnement des méthodes utilitaires sur les collections.
 *
 * @author Emmanuel WAGUET
 * @version 1.0
 */
@DisplayName("CollectionImtUtils - Tests unitaires")
class CollectionImtUtilsTest {

    @Nested
    @DisplayName("append(Collection, T) - Ajout d'un seul élément")
    class AppendSingleElementTests {

        @Test
        @DisplayName("Doit ajouter un élément à une collection non vide")
        void shouldAppendElementToNonEmptyCollection() {
            // Given
            Collection<String> source = Arrays.asList("a", "b", "c");
            String newElement = "d";

            // When
            Collection<String> result = CollectionImtUtils.append(source, newElement);

            // Then
            assertThat(result)
                    .isNotNull()
                    .hasSize(4)
                    .containsExactly("a", "b", "c", "d");
        }

        @Test
        @DisplayName("Doit ajouter un élément à une collection vide")
        void shouldAppendElementToEmptyCollection() {
            // Given
            Collection<String> source = Collections.emptyList();
            String newElement = "a";

            // When
            Collection<String> result = CollectionImtUtils.append(source, newElement);

            // Then
            assertThat(result)
                    .isNotNull()
                    .hasSize(1)
                    .containsExactly("a");
        }

        @Test
        @DisplayName("Doit ajouter un élément quand la source est null")
        void shouldAppendElementWhenSourceIsNull() {
            // Given
            Collection<String> source = null;
            String newElement = "a";

            // When
            Collection<String> result = CollectionImtUtils.append(source, newElement);

            // Then
            assertThat(result)
                    .isNotNull()
                    .hasSize(1)
                    .containsExactly("a");
        }

        @Test
        @DisplayName("Doit ajouter un élément null à une collection")
        void shouldAppendNullElement() {
            // Given
            Collection<String> source = Arrays.asList("a", "b");
            String newElement = null;

            // When
            Collection<String> result = CollectionImtUtils.append(source, newElement);

            // Then
            assertThat(result)
                    .isNotNull()
                    .hasSize(3)
                    .containsExactly("a", "b", null);
        }

        @Test
        @DisplayName("Ne doit pas modifier la collection source")
        void shouldNotModifySourceCollection() {
            // Given
            List<String> source = new ArrayList<>(Arrays.asList("a", "b"));
            int originalSize = source.size();
            String newElement = "c";

            // When
            Collection<String> result = CollectionImtUtils.append(source, newElement);

            // Then
            assertThat(source).hasSize(originalSize);
            assertThat(result).isNotSameAs(source);
        }

        @Test
        @DisplayName("Doit fonctionner avec différents types d'objets")
        void shouldWorkWithDifferentTypes() {
            // Given - Integers
            Collection<Integer> sourceInt = Arrays.asList(1, 2, 3);
            Integer newInt = 4;

            // When
            Collection<Integer> resultInt = CollectionImtUtils.append(sourceInt, newInt);

            // Then
            assertThat(resultInt).containsExactly(1, 2, 3, 4);
        }
    }

    @Nested
    @DisplayName("append(Collection, T...) - Ajout de plusieurs éléments (varargs)")
    class AppendVarargsTests {

        @Test
        @DisplayName("Doit ajouter plusieurs éléments à une collection non vide")
        void shouldAppendMultipleElementsToNonEmptyCollection() {
            // Given
            Collection<String> source = Arrays.asList("a", "b");
            String[] newElements = {"c", "d", "e"};

            // When
            Collection<String> result = CollectionImtUtils.append(source, newElements);

            // Then
            assertThat(result)
                    .isNotNull()
                    .hasSize(5)
                    .containsExactly("a", "b", "c", "d", "e");
        }

        @Test
        @DisplayName("Doit ajouter plusieurs éléments à une collection vide")
        void shouldAppendMultipleElementsToEmptyCollection() {
            // Given
            Collection<String> source = Collections.emptyList();
            String[] newElements = {"a", "b", "c"};

            // When
            Collection<String> result = CollectionImtUtils.append(source, newElements);

            // Then
            assertThat(result)
                    .isNotNull()
                    .hasSize(3)
                    .containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("Doit ajouter plusieurs éléments quand la source est null")
        void shouldAppendMultipleElementsWhenSourceIsNull() {
            // Given
            Collection<String> source = null;
            String[] newElements = {"a", "b"};

            // When
            Collection<String> result = CollectionImtUtils.append(source, newElements);

            // Then
            assertThat(result)
                    .isNotNull()
                    .hasSize(2)
                    .containsExactly("a", "b");
        }

        @Test
        @DisplayName("Doit gérer un tableau vide d'éléments")
        void shouldHandleEmptyVarargs() {
            // Given
            Collection<String> source = Arrays.asList("a", "b");
            String[] newElements = {};

            // When
            Collection<String> result = CollectionImtUtils.append(source, newElements);

            // Then
            assertThat(result)
                    .isNotNull()
                    .hasSize(2)
                    .containsExactly("a", "b");
        }

        @Test
        @DisplayName("Doit ajouter un seul élément via varargs")
        void shouldAppendSingleElementViaVarargs() {
            // Given
            Collection<String> source = Arrays.asList("a", "b");

            // When
            Collection<String> result = CollectionImtUtils.append(source, "c");

            // Then
            assertThat(result)
                    .isNotNull()
                    .hasSize(3)
                    .containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("Doit gérer des éléments null dans les varargs")
        void shouldHandleNullElementsInVarargs() {
            // Given
            Collection<String> source = Arrays.asList("a", "b");
            String[] newElements = {"c", null, "d"};

            // When
            Collection<String> result = CollectionImtUtils.append(source, newElements);

            // Then
            assertThat(result)
                    .isNotNull()
                    .hasSize(5)
                    .containsExactly("a", "b", "c", null, "d");
        }

        @Test
        @DisplayName("Ne doit pas modifier la collection source")
        void shouldNotModifySourceCollection() {
            // Given
            List<String> source = new ArrayList<>(Arrays.asList("a", "b"));
            int originalSize = source.size();
            String[] newElements = {"c", "d"};

            // When
            Collection<String> result = CollectionImtUtils.append(source, newElements);

            // Then
            assertThat(source).hasSize(originalSize);
            assertThat(result).isNotSameAs(source);
        }

        @Test
        @DisplayName("Doit fonctionner avec différents types d'objets")
        void shouldWorkWithDifferentTypes() {
            // Given
            Collection<Integer> sourceInt = Arrays.asList(1, 2);

            // When
            Collection<Integer> resultInt = CollectionImtUtils.append(sourceInt, 3, 4, 5);

            // Then
            assertThat(resultInt).containsExactly(1, 2, 3, 4, 5);
        }
    }

    @Nested
    @DisplayName("append(Collection, Collection) - Ajout d'une collection")
    class AppendCollectionTests {

        @Test
        @DisplayName("Doit ajouter une collection à une autre collection non vide")
        void shouldAppendCollectionToNonEmptyCollection() {
            // Given
            Collection<String> source = Arrays.asList("a", "b");
            Collection<String> toAdd = Arrays.asList("c", "d", "e");

            // When
            Collection<String> result = CollectionImtUtils.append(source, toAdd);

            // Then
            assertThat(result)
                    .isNotNull()
                    .hasSize(5)
                    .containsExactly("a", "b", "c", "d", "e");
        }

        @Test
        @DisplayName("Doit ajouter une collection à une collection vide")
        void shouldAppendCollectionToEmptyCollection() {
            // Given
            Collection<String> source = Collections.emptyList();
            Collection<String> toAdd = Arrays.asList("a", "b", "c");

            // When
            Collection<String> result = CollectionImtUtils.append(source, toAdd);

            // Then
            assertThat(result)
                    .isNotNull()
                    .hasSize(3)
                    .containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("Doit ajouter une collection quand la source est null")
        void shouldAppendCollectionWhenSourceIsNull() {
            // Given
            Collection<String> source = null;
            Collection<String> toAdd = Arrays.asList("a", "b");

            // When
            Collection<String> result = CollectionImtUtils.append(source, toAdd);

            // Then
            assertThat(result)
                    .isNotNull()
                    .hasSize(2)
                    .containsExactly("a", "b");
        }

        @Test
        @DisplayName("Doit gérer une collection vide à ajouter")
        void shouldHandleEmptyCollectionToAdd() {
            // Given
            Collection<String> source = Arrays.asList("a", "b");
            Collection<String> toAdd = Collections.emptyList();

            // When
            Collection<String> result = CollectionImtUtils.append(source, toAdd);

            // Then
            assertThat(result)
                    .isNotNull()
                    .hasSize(2)
                    .containsExactly("a", "b");
        }

        @Test
        @DisplayName("Doit gérer deux collections vides")
        void shouldHandleTwoEmptyCollections() {
            // Given
            Collection<String> source = Collections.emptyList();
            Collection<String> toAdd = Collections.emptyList();

            // When
            Collection<String> result = CollectionImtUtils.append(source, toAdd);

            // Then
            assertThat(result)
                    .isNotNull()
                    .isEmpty();
        }

        @Test
        @DisplayName("Doit gérer des éléments null dans la collection à ajouter")
        void shouldHandleNullElementsInCollectionToAdd() {
            // Given
            Collection<String> source = Arrays.asList("a", "b");
            Collection<String> toAdd = Arrays.asList("c", null, "d");

            // When
            Collection<String> result = CollectionImtUtils.append(source, toAdd);

            // Then
            assertThat(result)
                    .isNotNull()
                    .hasSize(5)
                    .containsExactly("a", "b", "c", null, "d");
        }

        @Test
        @DisplayName("Ne doit pas modifier la collection source")
        void shouldNotModifySourceCollection() {
            // Given
            List<String> source = new ArrayList<>(Arrays.asList("a", "b"));
            int originalSize = source.size();
            Collection<String> toAdd = Arrays.asList("c", "d");

            // When
            Collection<String> result = CollectionImtUtils.append(source, toAdd);

            // Then
            assertThat(source).hasSize(originalSize);
            assertThat(result).isNotSameAs(source);
        }

        @Test
        @DisplayName("Ne doit pas modifier la collection à ajouter")
        void shouldNotModifyCollectionToAdd() {
            // Given
            Collection<String> source = Arrays.asList("a", "b");
            List<String> toAdd = new ArrayList<>(Arrays.asList("c", "d"));
            int originalSize = toAdd.size();

            // When
            Collection<String> result = CollectionImtUtils.append(source, toAdd);

            // Then
            assertThat(toAdd).hasSize(originalSize);
            assertThat(result).isNotSameAs(toAdd);
        }

        @Test
        @DisplayName("Doit fonctionner avec différents types de collections")
        void shouldWorkWithDifferentCollectionTypes() {
            // Given - List et Set
            List<String> sourceList = Arrays.asList("a", "b");
            Set<String> sourceSet = new HashSet<>(Arrays.asList("c", "d"));

            // When
            Collection<String> result = CollectionImtUtils.append(sourceList, sourceSet);

            // Then
            assertThat(result)
                    .hasSize(4)
                    .contains("a", "b", "c", "d");
        }

        @Test
        @DisplayName("Doit fonctionner avec différents types d'objets")
        void shouldWorkWithDifferentTypes() {
            // Given
            Collection<Integer> sourceInt = Arrays.asList(1, 2);
            Collection<Integer> toAddInt = Arrays.asList(3, 4, 5);

            // When
            Collection<Integer> resultInt = CollectionImtUtils.append(sourceInt, toAddInt);

            // Then
            assertThat(resultInt).containsExactly(1, 2, 3, 4, 5);
        }
    }

    @Nested
    @DisplayName("Tests d'intégrité et de cohérence")
    class IntegrityTests {

        @Test
        @DisplayName("Doit retourner une nouvelle instance à chaque appel")
        void shouldReturnNewInstanceEachTime() {
            // Given
            Collection<String> source = Arrays.asList("a", "b");

            // When
            Collection<String> result1 = CollectionImtUtils.append(source, "c");
            Collection<String> result2 = CollectionImtUtils.append(source, "c");

            // Then
            assertThat(result1).isNotSameAs(result2);
            assertThat(result1).isEqualTo(result2);
        }

        @Test
        @DisplayName("Doit retourner une ArrayList modifiable")
        void shouldReturnModifiableArrayList() {
            // Given
            Collection<String> source = Arrays.asList("a", "b");

            // When
            Collection<String> result = CollectionImtUtils.append(source, "c");

            // Then
            assertThat(result).isInstanceOf(ArrayList.class);
            // Vérification que la liste est modifiable
            result.add("d");
            assertThat(result).hasSize(4);
        }

        @Test
        @DisplayName("Doit préserver l'ordre des éléments")
        void shouldPreserveElementOrder() {
            // Given
            Collection<String> source = Arrays.asList("z", "y", "x");

            // When
            Collection<String> result = CollectionImtUtils.append(source, "w");

            // Then
            assertThat(result).containsExactly("z", "y", "x", "w");
        }

        @Test
        @DisplayName("Doit gérer des collections avec des doublons")
        void shouldHandleCollectionsWithDuplicates() {
            // Given
            Collection<String> source = Arrays.asList("a", "b", "a");

            // When
            Collection<String> result = CollectionImtUtils.append(source, "a");

            // Then
            assertThat(result)
                    .hasSize(4)
                    .containsExactly("a", "b", "a", "a");
        }
    }
}

