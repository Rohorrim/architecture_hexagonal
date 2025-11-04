package com.wgt.imt.architecture.bdd.common.model.mappers;

/**
 * Classe abstraite pour les mappers entre objets métier et entités de base de données.
 * Définit le contrat pour la conversion bidirectionnelle.
 *
 * @param <T> le type de l'objet métier
 * @param <E> le type de l'entité de base de données
 * @author Emmanuel WAGUET
 * @version 1.0
 */
public abstract class AbstractBddMapper<T, E> {
    /**
     * Convertit une entité de base de données en objet métier.
     *
     * @param input l'entité à convertir
     * @return l'objet métier correspondant
     */
    public abstract T from(final E input);

    /**
     * Convertit un objet métier en entité de base de données.
     *
     * @param object l'objet métier à convertir
     * @return l'entité correspondante
     */
    public abstract E to(final T object);
}
