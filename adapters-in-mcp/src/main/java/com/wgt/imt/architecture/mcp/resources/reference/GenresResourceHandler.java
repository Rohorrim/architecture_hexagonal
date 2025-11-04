package com.wgt.imt.architecture.mcp.resources.reference;

import com.wgt.imt.architecture.business.common.model.GenreEnum;
import com.wgt.imt.architecture.mcp.common.model.output.resources.ResourceContent;
import com.wgt.imt.architecture.mcp.resources.ResourceHandler;
import org.springframework.stereotype.Service;

/**
 * Resource MCP exposant les <strong>genres disponibles</strong> pour la création d'un client.
 *
 * <h2>Cas d'usage pédagogique</h2>
 * <p>Même principe que {@link TypesCompteResourceHandler} : le LLM doit consulter
 * cette resource avant d'appeler un outil de création de client pour connaître
 * les valeurs acceptées pour le champ {@code genre}.</p>
 *
 * <p>Workflow typique du LLM :</p>
 * <ol>
 *   <li>Appelle {@code resources/read} avec URI {@code reference://genres}</li>
 *   <li>Lit les valeurs valides : {@code HOMME}, {@code FEMME}, {@code NON_COMMUNIQUER}</li>
 *   <li>Utilise l'une de ces valeurs dans les arguments de l'outil {@code CreateClient}</li>
 * </ol>
 *
 * <h2>Contenu retourné</h2>
 * <pre>
 * [
 *   { "value": "HOMME",            "description": "Genre masculin" },
 *   { "value": "FEMME",            "description": "Genre féminin" },
 *   { "value": "NON_COMMUNIQUER",  "description": "Genre non communiqué par choix" }
 * ]
 * </pre>
 *
 * @see ResourceHandler
 * @see GenreEnum
 * @since 0.0.1
 */
@Service
public class GenresResourceHandler implements ResourceHandler {

    private static final String URI = "reference://genres";
    private static final String NAME = "Genres disponibles pour un client";
    private static final String DESCRIPTION = "Liste des genres valides à utiliser lors de la création ou modification d'un client";
    private static final String MIME_TYPE = "application/json";

    private static final String CONTENT = """
            [
              { "value": "HOMME",           "description": "Genre masculin" },
              { "value": "FEMME",           "description": "Genre féminin" },
              { "value": "NON_COMMUNIQUER", "description": "Genre non communiqué par choix" }
            ]
            """;

    @Override
    public String getResourceUri() {
        return URI;
    }

    @Override
    public String getResourceName() {
        return NAME;
    }

    @Override
    public String getResourceDescription() {
        return DESCRIPTION;
    }

    @Override
    public String getMimeType() {
        return MIME_TYPE;
    }

    /**
     * Retourne la liste des genres valides au format JSON.
     *
     * <p>La valeur {@code INCONNU} de {@link GenreEnum} est exclue pour la même raison
     * que dans {@link TypesCompteResourceHandler} : elle représente une erreur de parsing.</p>
     *
     * @return Le contenu de la resource avec les genres valides
     */
    @Override
    public ResourceContent apply() {
        return ResourceContent.builder()
                .uri(URI)
                .mimeType(MIME_TYPE)
                .text(CONTENT)
                .build();
    }
}

