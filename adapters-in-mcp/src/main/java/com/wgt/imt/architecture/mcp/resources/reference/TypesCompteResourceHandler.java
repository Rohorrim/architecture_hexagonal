package com.wgt.imt.architecture.mcp.resources.reference;

import com.wgt.imt.architecture.business.common.model.TypeCompteEnum;
import com.wgt.imt.architecture.mcp.common.model.output.resources.ResourceContent;
import com.wgt.imt.architecture.mcp.resources.ResourceHandler;
import org.springframework.stereotype.Service;

/**
 * Resource MCP exposant les <strong>types de compte bancaire disponibles</strong>.
 *
 * <h2>Cas d'usage pédagogique</h2>
 * <p>Cette resource illustre pourquoi les Resources MCP sont nécessaires en complément
 * des Tools. Quand un LLM veut créer un compte (outil {@code CreateCompte}),
 * il doit connaître les valeurs acceptées pour le champ {@code type}.
 * Il ne peut pas les deviner — il doit les <em>lire</em> depuis cette resource.</p>
 *
 * <p>Workflow typique du LLM :</p>
 * <ol>
 *   <li>Appelle {@code resources/read} avec URI {@code reference://types-compte}</li>
 *   <li>Lit les valeurs valides : {@code COMPTE_COURANT}, {@code LIVRET_A}, {@code LDDS}</li>
 *   <li>Utilise l'une de ces valeurs dans les arguments de l'outil {@code CreateCompte}</li>
 * </ol>
 *
 * <h2>Contenu retourné</h2>
 * <p>Tableau JSON des types valides (la valeur {@code INCONNU} est exclue car
 * elle n'est pas acceptable lors de la création/modification d'un compte) :</p>
 * <pre>
 * [
 *   { "value": "COMPTE_COURANT", "description": "Compte courant classique" },
 *   { "value": "LIVRET_A",       "description": "Livret d'épargne A" },
 *   { "value": "LDDS",           "description": "Livret de développement durable et solidaire" }
 * ]
 * </pre>
 *
 * @see ResourceHandler
 * @see TypeCompteEnum
 * @since 0.0.1
 */
@Service
public class TypesCompteResourceHandler implements ResourceHandler {

    private static final String URI = "reference://types-compte";
    private static final String NAME = "Types de compte bancaire disponibles";
    private static final String DESCRIPTION = "Liste des types de compte valides à utiliser lors de la création ou modification d'un compte bancaire";
    private static final String MIME_TYPE = "application/json";

    /**
     * Contenu JSON statique généré une seule fois — les types de compte ne changent pas à l'exécution.
     */
    private static final String CONTENT = """
            [
              { "value": "COMPTE_COURANT", "description": "Compte courant classique" },
              { "value": "LIVRET_A",       "description": "Livret d'épargne A" },
              { "value": "LDDS",           "description": "Livret de développement durable et solidaire" }
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
     * Retourne la liste des types de compte valides au format JSON.
     *
     * <p>Le contenu est <strong>statique</strong> car les types de compte sont des constantes
     * métier définies dans {@link TypeCompteEnum} et ne varient pas à l'exécution.
     * La valeur {@code INCONNU} est volontairement exclue car elle représente une erreur
     * de parsing, non un type créable.</p>
     *
     * @return Le contenu de la resource avec les types valides
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

