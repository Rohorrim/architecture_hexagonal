package com.wgt.imt.architecture.business.comptes.model;

import com.wgt.imt.architecture.business.common.model.TypeCompteEnum;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

/**
 * Modèle métier représentant un compte bancaire.
 * Contient les informations du compte incluant le type, le nom et le solde.
 *
 * @author Emmanuel WAGUET
 * @version 1.0
 */
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class Compte {
    /**
     * Pattern de validation pour le nom du compte : lettres, chiffres, underscore et tiret, entre 1 et 250 caractères
     */
    private static final String NAME_PATTERN = "^[a-z-A-Z1-9_-]{1,250}$";

    /**
     * Identifiant unique du compte, généré automatiquement
     */
    @Builder.Default
    @NotNull(message = "L'identifiant ne peut pas être nul")
    private final UUID identifier = UUID.randomUUID();

    /**
     * Identifiant du client propriétaire du compte
     */
    @Builder.Default
    @NotNull(message = "L'identifiant du client ne peut pas être nul")
    private final UUID clientIdentifier = UUID.randomUUID();

    /**
     * Nom du compte
     */
    @NotNull(message = "Le nom ne peut pas être nul")
    @Pattern(regexp = NAME_PATTERN, message = "Le nom n'est pas valide : il doit être composé de lettre, de chiffre, d'underscore ou de tiré et doit faire entre 1 et 250 caractères")
    private final String name;

    /**
     * Type de compte (COMPTE_COURANT, LIVRET_A, LDDS)
     */
    @NotNull(message = "Le type ne peut pas être nul")
    private final TypeCompteEnum type;

    /**
     * Solde du compte, par défaut à 0
     */
    @Builder.Default
    private final Double solde = 0D;

    /**
     * Historique des mouvements effectués sur le compte.
     */
    @Builder.Default
    private final Collection<Mouvement> historique = Collections.emptySet();
}
