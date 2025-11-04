package com.wgt.imt.architecture.mcp.tools.comptes.input;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/**
 * Arguments d'entrée pour l'outil MCP <strong>"GetOneCompte"</strong>.
 *
 * <h2>Rôle de cette classe</h2>
 * <p>Cet outil nécessite <strong>deux identifiants</strong> car un compte bancaire
 * est toujours rattaché à un client dans ce modèle : on ne peut pas accéder à un compte
 * sans connaître son propriétaire. Le LLM doit donc fournir les deux UUID.</p>
 *
 * <h2>Double rôle des annotations</h2>
 *
 * <h3>1. Génération de l'inputSchema (tools/list)</h3>
 * <p>{@link com.wgt.imt.architecture.mcp.common.utils.JsonSchemaGenerator} introspecte cette classe
 * par réflexion pour produire l'{@code inputSchema} exposé lors de la découverte {@code tools/list} :</p>
 * <ul>
 *   <li>{@code @NotNull} → les champs apparaissent dans la liste {@code "required"} du JSON Schema</li>
 *   <li>{@code @Schema(description = "...")} → alimente le champ {@code "description"} du JSON Schema</li>
 *   <li>{@code @Schema(example = "...")} → alimente le champ {@code "example"} du JSON Schema</li>
 *   <li>Le type {@code UUID} → traduit en {@code "type": "string", "format": "uuid"}</li>
 * </ul>
 *
 * <h3>2. Validation Bean (tools/call)</h3>
 * <ul>
 *   <li>{@code @NotNull} → garantit que les deux identifiants sont présents dans les arguments</li>
 * </ul>
 *
 * @see com.wgt.imt.architecture.mcp.tools.comptes.GetOneCompteToolHandler
 * @see com.wgt.imt.architecture.mcp.tools.ToolHandler
 * @see com.wgt.imt.architecture.mcp.common.utils.JsonSchemaGenerator
 * @since 0.0.1
 */
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class GetOneCompteToolArguments implements Serializable {

    @Serial
    private static final long serialVersionUID = -3148692074518903762L;

    /**
     * Identifiant unique (UUID) du client propriétaire du compte.
     *
     * <p>Requis car les comptes sont toujours filtrés par client dans ce modèle métier.</p>
     */
    @Schema(example = "550e8400-e29b-41d4-a716-446655440000", description = "Identifiant du client propriétaire du compte")
    @NotNull(message = "clientIdentifier doit être présent dans les arguments")
    private UUID clientIdentifier;

    /**
     * Identifiant unique (UUID) du compte à récupérer.
     */
    @Schema(example = "660f9511-f30c-52e5-b827-557766551111", description = "Identifiant du compte à récupérer")
    @NotNull(message = "identifier doit être présent dans les arguments")
    private UUID identifier;
}

