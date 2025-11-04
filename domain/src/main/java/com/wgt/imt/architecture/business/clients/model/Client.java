package com.wgt.imt.architecture.business.clients.model;

import com.wgt.imt.architecture.business.common.model.GenreEnum;
import com.wgt.imt.architecture.business.comptes.model.Compte;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

/**
 * Modèle métier représentant un client.
 * Contient les informations personnelles du client et ses comptes bancaires.
 *
 * @author Emmanuel WAGUET
 * @version 1.0
 */
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class Client {
    /**
     * Pattern de validation pour les noms : lettres et espaces uniquement, entre 1 et 250 caractères
     */
    private static final String NAME_PATTERN = "^[a-z-A-Z ]{1,250}$";

    /**
     * Identifiant unique du client, généré automatiquement
     */
    @Builder.Default
    @NotNull(message = "L'identifiant ne peut pas être nul")
    private final UUID identifier = UUID.randomUUID();

    /**
     * Nom de famille du client
     */
    @NotNull(message = "Le nom ne peut pas être nul")
    @Pattern(regexp = NAME_PATTERN, message = "Le nom n'est pas valide : il doit être composé uniquement de lettre et d'espace et doit faire entre 1 et 250 caractères")
    private final String lastname;

    /**
     * Prénom du client
     */
    @NotNull(message = "Le prenom ne peut pas être nul")
    @Pattern(regexp = NAME_PATTERN, message = "Le prenom n'est pas valide : il doit être composé uniquement de lettre et d'espace et doit faire entre 1 et 250 caractères")
    private final String firstname;

    /**
     * Genre du client (HOMME, FEMME, NON_COMMUNIQUER)
     */
    @NotNull(message = "Le genre ne peut pas être nul")
    private final GenreEnum genre;

    /**
     * Collection des comptes bancaires associés au client
     */
    @Builder.Default
    private final Collection<Compte> comptes = Collections.emptySet();
}
