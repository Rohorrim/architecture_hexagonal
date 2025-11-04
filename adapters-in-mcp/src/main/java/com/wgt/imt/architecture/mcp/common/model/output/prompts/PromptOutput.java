package com.wgt.imt.architecture.mcp.common.model.output.prompts;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Modèle de sortie décrivant un prompt MCP disponible — correspond à un élément de {@code prompts/list}.
 *
 * <h2>Qu'est-ce qu'un Prompt MCP ?</h2>
 * <p>Dans la spécification MCP, un <strong>prompt</strong> est un <em>template de conversation
 * réutilisable et paramétrable</em> exposé par le serveur. Il guide le LLM à travers un
 * workflow prédéfini — par exemple : récupérer des données via plusieurs Tool calls,
 * puis produire une analyse structurée.</p>
 *
 * <p>Différence clé avec les Tools :</p>
 * <ul>
 *   <li>Un <strong>Tool</strong> = action atomique (appel API, lecture BDD)</li>
 *   <li>Un <strong>Prompt</strong> = workflow complet guidant le LLM sur comment utiliser les tools</li>
 * </ul>
 *
 * <h2>Champs conformes à la spec MCP</h2>
 * <ul>
 *   <li>{@code name} — identifiant unique du prompt</li>
 *   <li>{@code description} — description du workflow réalisé par ce prompt</li>
 *   <li>{@code arguments} — liste des arguments attendus pour paramétrer le prompt</li>
 * </ul>
 *
 * @param name        Nom unique identifiant le prompt (ex : {@code "analyse-client"}).
 * @param description Description du workflow réalisé par ce prompt, lue et interprétée par le LLM.
 * @param arguments   Liste des arguments attendus pour paramétrer ce prompt.
 *
 *                    <p>Le LLM s'en sert pour savoir quels arguments fournir lors de l'appel à {@code prompts/get}.</p>
 * @see PromptArgument
 * @see PromptGetOutput
 * @see <a href="https://spec.modelcontextprotocol.io/specification/server/prompts/">MCP Prompts Specification</a>
 * @since 0.0.1
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record PromptOutput(String name, String description, List<PromptArgument> arguments) implements Serializable {

    @Serial
    private static final long serialVersionUID = 2941067598301476295L;

}

