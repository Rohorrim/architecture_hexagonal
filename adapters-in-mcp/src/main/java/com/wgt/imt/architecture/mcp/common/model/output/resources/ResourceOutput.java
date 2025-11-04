package com.wgt.imt.architecture.mcp.common.model.output.resources;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.wgt.imt.architecture.mcp.resources.ResourceHandler;
import lombok.Builder;

import java.io.Serial;
import java.io.Serializable;

/**
 * Modèle de sortie décrivant une ressource MCP disponible — correspond à un élément de {@code resources/list}.
 *
 * <h2>Qu'est-ce qu'une Resource MCP ?</h2>
 * <p>Dans la spécification MCP, une <strong>resource</strong> est une donnée en lecture seule
 * exposée par le serveur à destination du LLM. Contrairement aux {@code tools}, les resources
 * ne déclenchent pas d'action : elles fournissent du <em>contexte</em> que le LLM peut consulter
 * avant de prendre une décision ou d'appeler un outil.</p>
 *
 * <p>Exemples typiques : données de référence (énumérations), documentation métier, configuration.</p>
 *
 * <h2>Champs conformes à la spec MCP</h2>
 * <ul>
 *   <li>{@code uri} — identifiant unique de la ressource (ex : {@code reference://types-compte})</li>
 *   <li>{@code name} — nom lisible par le LLM</li>
 *   <li>{@code description} — description de ce que contient la ressource</li>
 *   <li>{@code mimeType} — format du contenu (ex : {@code application/json}, {@code text/plain})</li>
 * </ul>
 *
 * @param uri         URI unique identifiant la ressource.
 *
 *                    <p>Convention de nommage : {@code scheme://path} (ex : {@code reference://types-compte}).
 *                    Le schéma est libre mais doit être cohérent dans le projet.</p>
 * @param name        Nom lisible de la ressource, interprété par le LLM.
 * @param description Description du contenu de la ressource, aidant le LLM à décider si elle est pertinente.
 * @param mimeType    Type MIME du contenu retourné par {@code resources/read}.
 *                    (ex : {@code application/json}, {@code text/plain}, {@code text/markdown})
 * @see ResourceHandler
 * @see <a href="https://spec.modelcontextprotocol.io/specification/server/resources/">MCP Resources Specification</a>
 * @since 0.0.1
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ResourceOutput(String uri, String name, String description, String mimeType) implements Serializable {

    @Serial
    private static final long serialVersionUID = 5312047698301476291L;

}

