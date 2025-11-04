package com.wgt.imt.architecture.business.common.model;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Énumération représentant les genres possibles pour un client.
 * INCONNU est utilisé comme valeur par défaut pour les genres non reconnus.
 *
 * @author Emmanuel WAGUET
 * @version 1.0
 */
public enum GenreEnum {
    /**
     * Genre masculin
     */
    HOMME,

    /**
     * Genre féminin
     */
    FEMME,

    /**
     * Genre non communiqué par choix
     */
    NON_COMMUNIQUER,

    /**
     * Genre inconnu ou invalide (non acceptable pour la création/modification)
     */
    INCONNU;

    /**
     * Chaîne de caractères listant les valeurs acceptables (exclut INCONNU)
     */
    public static final String ACCEPTABLE_VALUES = Set.of(HOMME, FEMME, NON_COMMUNIQUER).stream().map(Enum::name).collect(Collectors.joining(", "));

    /**
     * Convertit une chaîne en GenreEnum, retourne INCONNU si la valeur n'est pas reconnue.
     *
     * @param name le nom du genre
     * @return l'énumération correspondante ou INCONNU
     */
    public static GenreEnum fromOrDefault(final String name) {
        return GenreEnum.from(name).orElse(GenreEnum.INCONNU);
    }

    /**
     * Convertit une chaîne en GenreEnum de manière sécurisée.
     *
     * @param name le nom du genre
     * @return un Optional contenant l'énumération si valide, Optional.empty() sinon
     */
    public static Optional<GenreEnum> from(final String name) {
        try {
            return Optional.ofNullable(name)
                    .map(GenreEnum::valueOf);
        } catch (final IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}