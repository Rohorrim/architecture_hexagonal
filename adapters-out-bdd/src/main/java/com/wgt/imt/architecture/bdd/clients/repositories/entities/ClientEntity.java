package com.wgt.imt.architecture.bdd.clients.repositories.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Collection;

/**
 * Entité MongoDB représentant un client.
 * Stockée dans la collection "clients" de la base de données.
 * Contient les informations du client et ses comptes embarqués.
 *
 * @author Emmanuel WAGUET
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "clients")
public class ClientEntity {

    /**
     * Identifiant unique du client (clé primaire MongoDB)
     */
    @Id
    private String identifier;

    /**
     * Nom de famille du client
     */
    private String lastname;

    /**
     * Prénom du client
     */
    private String firstname;

    /**
     * Genre du client au format String
     */
    private String genre;

    /**
     * Collection des comptes bancaires du client (documents embarqués)
     */
    private Collection<CompteEntity> comptes;
}
