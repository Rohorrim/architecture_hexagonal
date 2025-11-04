package com.wgt.imt.architecture.mcp.common.model.output.resources;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.io.Serial;
import java.io.Serializable;

/**
 * Contenu d'une ressource MCP retourné par {@code resources/read}.
 *
 * <h2>Rôle</h2>
 * <p>Quand un LLM veut lire le contenu d'une ressource, il appelle {@code resources/read}
 * avec l'URI de la ressource. Cette classe encapsule le contenu retourné.</p>
 *
 * <h2>Champ text vs blob</h2>
 * <p>La spec MCP distingue deux types de contenu :</p>
 * <ul>
 *   <li>{@code text} — contenu textuel (JSON, Markdown, texte brut) — utilisé dans ce projet</li>
 *   <li>{@code blob} — contenu binaire encodé en Base64 (images, PDF...) — non utilisé ici</li>
 * </ul>
 *
 * @param uri      URI de la ressource lue, répété dans la réponse pour permettre au LLM de la corréler.
 * @param mimeType Type MIME du contenu (doit correspondre à celui déclaré dans {@link ResourceOutput#mimeType()}).
 * @param text     Contenu textuel de la ressource (JSON, Markdown, texte brut, etc.).
 *
 *                 <p>C'est cette valeur que le LLM va lire et interpréter pour enrichir son contexte.</p>
 * @see ResourceOutput
 * @see <a href="https://spec.modelcontextprotocol.io/specification/server/resources/#reading-resources">MCP resources/read</a>
 * @since 0.0.1
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ResourceContent(String uri, String mimeType, String text) implements Serializable {

    @Serial
    private static final long serialVersionUID = -8134592074518903762L;

}

