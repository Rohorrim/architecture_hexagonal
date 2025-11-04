package com.wgt.imt.architecture.rest.comptes;

import com.wgt.imt.architecture.business.common.exception.ImtException;
import com.wgt.imt.architecture.business.comptes.ComptesServiceValidator;
import com.wgt.imt.architecture.business.comptes.model.Compte;
import com.wgt.imt.architecture.rest.comptes.model.input.CompteInput;
import com.wgt.imt.architecture.rest.comptes.model.input.CompteUpdateInput;
import com.wgt.imt.architecture.rest.comptes.model.output.ComptesOutput;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Contrôleur REST pour la gestion des comptes bancaires.
 * Expose les endpoints HTTP pour les opérations CRUD sur les comptes d'un client.
 * Point d'entrée : /api/imt/v1/clients/{idClient}/comptes
 *
 * @author Emmanuel WAGUET
 * @version 1.0
 */
@RestController
@AllArgsConstructor
@RequestMapping("api/imt/v1/clients/{idClient}/comptes")
public class ComptesController {
    /**
     * Service de validation et gestion des comptes
     */
    private final ComptesServiceValidator service;

    /**
     * Récupère tous les comptes d'un client.
     *
     * @param clientIdentifier l'identifiant du client au format String
     * @return une collection de ComptesOutput représentant tous les comptes du client
     */
    @ResponseStatus(value = HttpStatus.OK)
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Collection<ComptesOutput> getAll(@PathVariable("idClient") final String clientIdentifier) {
        return Objects.requireNonNullElse(this.service.getAllFilteredByClient(UUID.fromString(clientIdentifier)), Collections.<Compte>emptySet()).stream()
                .map(ComptesOutput::from)
                .collect(Collectors.toSet());
    }

    /**
     * Crée un nouveau compte pour un client.
     *
     * @param clientIdentifier l'identifiant du client au format String
     * @param compte           les informations du compte à créer
     * @return le compte créé avec son identifiant généré
     */
    @ResponseStatus(value = HttpStatus.CREATED)
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ComptesOutput create(@PathVariable("idClient") final String clientIdentifier, @RequestBody final CompteInput compte) throws ImtException {
        return ComptesOutput.from(
                this.service.create(
                        UUID.fromString(clientIdentifier),
                        CompteInput.convert(compte)
                )
        );
    }

    /**
     * Récupère un compte spécifique d'un client.
     *
     * @param clientIdentifier l'identifiant du client au format String
     * @param identifier       l'identifiant du compte au format String
     * @return le compte correspondant
     * @throws RuntimeException si le compte n'existe pas pour ce client
     */
    @ResponseStatus(value = HttpStatus.OK)
    @GetMapping(value = "/{idCompte}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ComptesOutput getOne(@PathVariable("idClient") final String clientIdentifier, @PathVariable("idCompte") final String identifier) {
        return this.service.getOneFilteredByClient(UUID.fromString(clientIdentifier), UUID.fromString(identifier))
                .map(ComptesOutput::from)
                .orElseThrow(() -> new RuntimeException(String.format("Le compte d'identifiant %s n'a pas été trouvé pour le client %s.", identifier, clientIdentifier)));
    }

    /**
     * Met à jour partiellement un compte existant (PATCH).
     *
     * @param clientIdentifier l'identifiant du client au format String
     * @param identifier       l'identifiant du compte au format String
     * @param compte           les informations à mettre à jour
     * @throws RuntimeException si le compte n'existe pas pour ce client
     */
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @PatchMapping(value = "/{idCompte}")
    public void update(@PathVariable("idClient") final String clientIdentifier, @PathVariable("idCompte") final String identifier, @RequestBody final CompteUpdateInput compte) throws ImtException {
        final UUID formattedClientIdentifier = UUID.fromString(clientIdentifier);
        this.service.update(
                formattedClientIdentifier,
                this.service.getOneFilteredByClient(formattedClientIdentifier, UUID.fromString(identifier))
                        .map(alreadySaved -> CompteUpdateInput.from(compte, alreadySaved))
                        .orElseThrow(() -> new RuntimeException(String.format("Le compte d'identifiant %s n'a pas été trouvé pour le client %s.", identifier, clientIdentifier)))
        );

    }

    /**
     * Supprime un compte d'un client.
     *
     * @param clientIdentifier l'identifiant du client au format String
     * @param identifier       l'identifiant du compte au format String
     */
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "/{idCompte}")
    public void delete(@PathVariable("idClient") final String clientIdentifier, @PathVariable("idCompte") final String identifier) throws ImtException {
        this.service.delete(UUID.fromString(clientIdentifier), UUID.fromString(identifier));
    }
}
