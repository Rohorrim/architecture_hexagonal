package com.wgt.imt.architecture.mcp.common.model.output.prompts;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Résultat d'un appel {@code prompts/get} — le prompt rendu avec ses arguments injectés.
 *
 * <h2>Structure</h2>
 * <p>Contient la liste des {@link PromptMessage} que le LLM doit traiter en séquence.
 * Chaque message a un rôle ({@code user} ou {@code assistant}) et un contenu textuel.</p>
 *
 * <h2>Exemple de réponse</h2>
 * <pre>
 * {
 *   "name": "analyse-client",
 *   "messages": [
 *     {
 *       "role": "user",
 *       "content": "Tu es un conseiller bancaire. Analyse le client 550e8400-...
 *                   Commence par appeler GetOneClient, puis GetAllComptes..."
 *     }
 *   ]
 * }
 * </pre>
 *
 * @param name     Nom du prompt rendu (pour corrélation avec la requête).
 * @param messages Séquence de messages composant le prompt rendu.
 *
 *                 <p>Le LLM les traite dans l'ordre pour exécuter le workflow guidé.</p>
 * @see PromptMessage
 * @see <a href="https://spec.modelcontextprotocol.io/specification/server/prompts/#getting-a-prompt">MCP prompts/get</a>
 * @since 0.0.1
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record PromptGetOutput(String name, List<PromptMessage> messages) implements Serializable {

    @Serial
    private static final long serialVersionUID = -7654321098765432109L;

}

