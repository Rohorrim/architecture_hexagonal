package com.wgt.imt.architecture.mcp.resources;

import com.wgt.imt.architecture.mcp.common.OutputAble;
import com.wgt.imt.architecture.mcp.common.model.output.resources.ResourceContent;
import com.wgt.imt.architecture.mcp.common.model.output.resources.ResourceOutput;

/**
 * Interface définissant le contrat d'une <strong>ressource MCP</strong> (Model Context Protocol Resource).
 *
 * <h2>Contexte MCP — Resources vs Tools</h2>
 * <p>Les resources sont la deuxième primitive de MCP, complémentaire aux Tools :</p>
 * <table border="1">
 *   <tr><th></th><th>Tool</th><th>Resource</th></tr>
 *   <tr><td><strong>Nature</strong></td><td>Action (verbe)</td><td>Donnée (nom)</td></tr>
 *   <tr><td><strong>Interaction</strong></td><td>Exécution avec effets de bord possibles</td><td>Lecture seule</td></tr>
 *   <tr><td><strong>Usage</strong></td><td>Appeler un service, modifier des données</td><td>Enrichir le contexte du LLM</td></tr>
 *   <tr><td><strong>Analogie</strong></td><td>Fonction / API REST POST</td><td>Fichier / API REST GET</td></tr>
 * </table>
 *
 * <h2>Patron de conception</h2>
 * <p>Même patron que {@link com.wgt.imt.architecture.mcp.tools.ToolHandler} :
 * chaque implémentation est un bean Spring {@code @Service} découvert automatiquement
 * par le contrôleur via l'injection de collection.</p>
 *
 * <h2>Cas d'usage dans ce projet</h2>
 * <ul>
 *   <li>Données de référence (énumérations) : types de compte, genres — le LLM les consulte
 *       avant de créer un client ou un compte pour connaître les valeurs valides</li>
 * </ul>
 *
 * @see com.wgt.imt.architecture.mcp.JsonRpcController
 * @see <a href="https://spec.modelcontextprotocol.io/specification/server/resources/">MCP Resources Specification</a>
 */
public interface ResourceHandler extends OutputAble<ResourceOutput> {

    /**
     * Construit le {@link ResourceOutput} décrivant cette ressource (pour {@code resources/list}).
     *
     * <p>Méthode par défaut : évite la duplication dans chaque implémentation.</p>
     *
     * @return Le descripteur de la ressource
     */
    default ResourceOutput toOutput() {
        return ResourceOutput.builder()
                .uri(this.getResourceUri())
                .name(this.getResourceName())
                .description(this.getResourceDescription())
                .mimeType(this.getMimeType())
                .build();
    }

    /**
     * Retourne l'URI unique identifiant cette ressource.
     *
     * <p>Convention : {@code scheme://path} (ex : {@code reference://types-compte}).
     * Le LLM utilise cet URI pour appeler {@code resources/read}.</p>
     *
     * @return L'URI de la ressource
     */
    String getResourceUri();

    /**
     * Retourne le nom lisible de la ressource.
     *
     * @return Le nom de la ressource
     */
    String getResourceName();

    /**
     * Retourne la description de la ressource.
     *
     * <p>Lue par le LLM pour décider si cette ressource est pertinente pour sa tâche.</p>
     *
     * @return La description de la ressource
     */
    String getResourceDescription();

    /**
     * Retourne le type MIME du contenu produit par {@link #apply()}.
     *
     * @return Le type MIME (ex : {@code "application/json"}, {@code "text/plain"})
     */
    String getMimeType();

    /**
     * Lit et retourne le contenu de la ressource (pour {@code resources/read}).
     *
     * @return Le contenu de la ressource encapsulé dans un {@link ResourceContent}
     */
    ResourceContent apply();
}

