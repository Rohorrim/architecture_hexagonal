package com.wgt.imt.architecture.business.common.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.*;
import java.util.stream.Stream;

/**
 * Classe utilitaire pour les opérations sur les collections.
 * Fournit des méthodes pour ajouter des éléments à des collections de manière immutable.
 *
 * @author Emmanuel WAGUET
 * @version 1.0
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CollectionImtUtils {

    /**
     * Ajoute un élément à une collection et retourne une nouvelle collection.
     *
     * @param <T>     le type des éléments de la collection
     * @param source  la collection source (peut être null)
     * @param element l'élément à ajouter
     * @return une nouvelle collection contenant tous les éléments de la source plus le nouvel élément
     */
    public static <T> Collection<T> append(final Collection<T> source, final T element) {
        final List<T> result = new ArrayList<>(Objects.requireNonNullElse(source, Collections.emptyList()));
        result.add(element);
        return result;
    }

    /**
     * Ajoute plusieurs éléments (varargs) à une collection et retourne une nouvelle collection.
     *
     * @param <T>      le type des éléments de la collection
     * @param source   la collection source (peut être null)
     * @param elements les éléments à ajouter
     * @return une nouvelle collection contenant tous les éléments de la source plus les nouveaux éléments
     */
    public static <T> Collection<T> append(final Collection<T> source, final T... elements) {
        final List<T> result = new ArrayList<>(Objects.requireNonNullElse(source, Collections.emptyList()));
        Stream.of(elements).forEach(result::add);
        return result;
    }

    /**
     * Ajoute une collection d'éléments à une collection source et retourne une nouvelle collection.
     *
     * @param <T>      le type des éléments de la collection
     * @param source   la collection source (peut être null)
     * @param elements la collection d'éléments à ajouter
     * @return une nouvelle collection contenant tous les éléments des deux collections
     */
    public static <T> Collection<T> append(final Collection<T> source, final Collection<T> elements) {
        final List<T> result = new ArrayList<>(Objects.requireNonNullElse(source, Collections.emptyList()));
        result.addAll(elements);
        return result;
    }
}
