package com.wgt.imt.architecture.rest.clients;

import com.wgt.imt.architecture.business.clients.ClientsServiceValidator;
import com.wgt.imt.architecture.business.clients.model.Client;
import com.wgt.imt.architecture.business.common.exception.ImtException;
import com.wgt.imt.architecture.business.common.exception.NotFoundException;
import com.wgt.imt.architecture.rest.clients.model.input.ClientInput;
import com.wgt.imt.architecture.rest.clients.model.input.ClientUpdateInput;
import com.wgt.imt.architecture.rest.clients.model.output.ClientOutput;
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
 * Contrôleur REST pour la gestion des clients.
 * Expose les endpoints HTTP pour les opérations CRUD sur les clients.
 * Point d'entrée : /api/imt/v1/clients
 *
 * @author Emmanuel WAGUET
 * @version 1.0
 */
@RestController
@AllArgsConstructor
@RequestMapping("api/imt/v1/clients")
public class ClientsController {
    /**
     * Service de validation et gestion des clients
     */
    private final ClientsServiceValidator service;

    /**
     * Récupère tous les clients.
     *
     * @return une collection de ClientOutput représentant tous les clients
     */
    @ResponseStatus(value = HttpStatus.OK)
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Collection<ClientOutput> getAll() {
        return Objects.requireNonNullElse(this.service.getAll(), Collections.<Client>emptySet()).stream()
                .map(ClientOutput::from)
                .collect(Collectors.toSet());
    }

    /**
     * Crée un nouveau client.
     *
     * @param client les informations du client à créer
     * @return le client créé avec son identifiant généré
     */
    @ResponseStatus(value = HttpStatus.CREATED)
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ClientOutput create(@RequestBody final ClientInput client) throws ImtException {
        return ClientOutput.from(
                this.service.create(
                        ClientInput.convert(client)
                )
        );
    }

    /**
     * Récupère un client spécifique par son identifiant.
     *
     * @param identifier l'identifiant du client au format String
     * @return le client correspondant
     * @throws NotFoundException si le client n'existe pas
     */
    @ResponseStatus(value = HttpStatus.OK)
    @GetMapping(value = "/{idClient}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ClientOutput getOne(@PathVariable("idClient") final String identifier) throws ImtException {
        return this.service.getOne(UUID.fromString(identifier))
                .map(ClientOutput::from)
                .orElseThrow(() -> new NotFoundException(String.format("Le client d'identifiant %s n'a pas été trouvé.", identifier)));
    }

    /**
     * Met à jour partiellement un client existant (PATCH).
     *
     * @param identifier l'identifiant du client au format String
     * @param client     les informations à mettre à jour
     * @throws NotFoundException si le client n'existe pas
     */
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @PatchMapping(value = "/{idClient}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void update(@PathVariable("idClient") final String identifier, @RequestBody final ClientUpdateInput client) throws ImtException {
        this.service.update(
                this.service.getOne(UUID.fromString(identifier))
                        .map(alreadySaved -> ClientUpdateInput.from(client, alreadySaved))
                        .orElseThrow(() -> new NotFoundException(String.format("Le client d'identifiant %s n'a pas été trouvé.", identifier)))
        );
    }

    /**
     * Supprime un client par son identifiant.
     *
     * @param identifier l'identifiant du client au format String
     */
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "/{idClient}")
    public void delete(@PathVariable("idClient") final String identifier) throws ImtException {
        this.service.delete(UUID.fromString(identifier));
    }
}
