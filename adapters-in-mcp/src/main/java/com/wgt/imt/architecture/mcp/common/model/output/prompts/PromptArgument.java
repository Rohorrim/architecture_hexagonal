package com.wgt.imt.architecture.mcp.common.model.output.prompts;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.io.Serial;
import java.io.Serializable;

/**
 * Définition d'un argument attendu par un prompt MCP.
 *
 * <p>Utilisé dans {@link PromptOutput#arguments()} pour décrire au LLM
 * quels paramètres fournir lors d'un appel {@code prompts/get}.</p>
 *
 * @param name        Nom de l'argument (ex : {@code "clientIdentifier"}).
 * @param description Description de l'argument, aidant le LLM à comprendre ce qu'il doit fournir.
 * @param required    Indique si cet argument est obligatoire pour l'exécution du prompt.
 * @see PromptOutput
 * @since 0.0.1
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record PromptArgument(String name, String description, boolean required) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1234567890123456789L;

}

