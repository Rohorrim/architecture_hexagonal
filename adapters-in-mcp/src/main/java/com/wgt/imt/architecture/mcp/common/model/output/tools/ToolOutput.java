package com.wgt.imt.architecture.mcp.common.model.output.tools;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.wgt.imt.architecture.mcp.JsonRpcController;
import com.wgt.imt.architecture.mcp.common.model.input.JsonRpcRequest;
import com.wgt.imt.architecture.mcp.common.utils.JsonSchemaGenerator;
import com.wgt.imt.architecture.mcp.tools.ToolHandler;
import lombok.Builder;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * Modèle de sortie décrivant un outil MCP disponible — correspond à un élément de {@code tools/list}.
 *
 * <h2>Rôle dans le protocole MCP</h2>
 * <p>Cette classe représente la <strong>carte d'identité</strong> d'un outil exposé à un LLM.
 * Le LLM l'utilise pour comprendre :</p>
 * <ul>
 *   <li>Comment s'appelle l'outil ({@code name})</li>
 *   <li>Ce qu'il fait ({@code description}) — <em>lu et interprété par le LLM lui-même</em></li>
 *   <li>Quels arguments il attend ({@code inputSchema}) — format JSON Schema</li>
 * </ul>
 *
 * <h2>Champ clé : inputSchema</h2>
 * <p>L'{@code inputSchema} est un <strong>JSON Schema</strong> (standard RFC) qui décrit
 * la structure des arguments attendus. Il est généré <em>dynamiquement</em> par introspection
 * de la classe Java des arguments via {@link JsonSchemaGenerator}.
 * Le LLM s'en sert pour construire des arguments valides lors d'un appel {@code tools/call}.</p>
 *
 * @param name        Nom unique identifiant l'outil.
 *
 *                    <p>C'est la valeur retournée par {@link ToolHandler#getName()}.
 *                    Elle sert également de clé de routage lors d'un appel {@code tools/call}.</p>
 * @param description Description en langage naturel de ce que fait l'outil.
 *
 *                    <p>Cette description est <strong>directement lue par le LLM</strong> pour décider
 *                    si cet outil est pertinent dans le contexte de la conversation.
 *                    Une description précise et orientée action améliore la qualité de l'agent.</p>
 * @param inputSchema JSON Schema décrivant la structure des arguments d'entrée attendus.
 *
 *                    <p>Généré automatiquement par {@link JsonSchemaGenerator}
 *                    à partir de la classe Java des arguments (retournée par
 *                    {@link ToolHandler#getInputType()}).
 *                    Conforme au standard <a href="https://json-schema.org/">JSON Schema</a>.</p>
 *
 *                    <p>Peut être absent ou vide si l'outil ne nécessite aucun argument.</p>
 * @see JsonRpcController#dispatch(JsonRpcRequest)
 * @see JsonSchemaGenerator
 * @see <a href="https://spec.modelcontextprotocol.io/specification/server/tools/#listing-tools">MCP tools/list</a>
 * @since 0.0.1
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ToolOutput(String name, String description, Map<String, Object> inputSchema) implements Serializable {
    @Serial
    private static final long serialVersionUID = -2865905156465069262L;

}
