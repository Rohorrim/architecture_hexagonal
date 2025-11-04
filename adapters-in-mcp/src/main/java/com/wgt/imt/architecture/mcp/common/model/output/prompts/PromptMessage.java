package com.wgt.imt.architecture.mcp.common.model.output.prompts;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.io.Serial;
import java.io.Serializable;

/**
 * Un message dans un prompt MCP rendu — élément de {@link PromptGetOutput#messages()}.
 *
 * <h2>Rôle</h2>
 * <p>Représente un tour de conversation dans le prompt. Le champ {@code role} indique
 * qui "parle" dans ce message :</p>
 * <ul>
 *   <li>{@code "user"} — instruction donnée au LLM (ce que l'utilisateur/système demande)</li>
 *   <li>{@code "assistant"} — exemple de réponse attendue (few-shot prompting)</li>
 * </ul>
 *
 * @param role    Rôle du message : {@code "user"} ou {@code "assistant"}.
 *
 *                <p>Dans la plupart des prompts MCP simples, tous les messages sont de rôle {@code "user"}.</p>
 * @param content Contenu textuel du message, injecté directement dans le contexte de conversation du LLM.
 * @see PromptGetOutput
 * @since 0.0.1
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record PromptMessage(String role, String content) implements Serializable {

    @Serial
    private static final long serialVersionUID = 857861477461921440L;

}

