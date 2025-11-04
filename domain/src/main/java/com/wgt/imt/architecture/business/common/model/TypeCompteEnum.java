package com.wgt.imt.architecture.business.common.model;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Énumération représentant les types de comptes bancaires possibles.
 * INCONNU est utilisé comme valeur par défaut pour les types non reconnus.
 *
 * @author Emmanuel WAGUET
 * @version 1.0
 */
public enum TypeCompteEnum {
    /**
     * Compte courant classique
     */
    COMPTE_COURANT,

    /**
     * Livret d'épargne A
     */
    LIVRET_A,

    /**
     * Livret de développement durable et solidaire
     */
    LDDS,

    /**
     * Type de compte inconnu ou invalide (non acceptable pour la création/modification)
     */
    INCONNU;

    /**
     * Chaîne de caractères listant les valeurs acceptables (exclut INCONNU)
     */
    public static final String ACCEPTABLE_VALUES = Set.of(COMPTE_COURANT, LIVRET_A, LDDS).stream().map(Enum::name).collect(Collectors.joining(", "));

    /**
     * Convertit une chaîne en TypeCompteEnum, retourne INCONNU si la valeur n'est pas reconnue.
     *
     * @param name le nom du type de compte
     * @return l'énumération correspondante ou INCONNU
     */
    public static TypeCompteEnum fromOrDefault(final String name) {
        return TypeCompteEnum.from(name).orElse(TypeCompteEnum.INCONNU);
    }

    /**
     * Convertit une chaîne en TypeCompteEnum de manière sécurisée.
     *
     * @param name le nom du type de compte
     * @return un Optional contenant l'énumération si valide, Optional.empty() sinon
     */
    public static Optional<TypeCompteEnum> from(final String name) {
        try {
            return Optional.ofNullable(name).map(TypeCompteEnum::valueOf);
        } catch (final IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
