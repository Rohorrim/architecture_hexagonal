package com.wgt.imt.architecture.mcp.tools.comptes.input;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/**
 * Arguments d'entrée pour l'outil MCP <strong>"GetAllComptes"</strong>.
 *
 * <h2>Rôle de cette classe</h2>
 * <p>Contrairement à l'outil {@code GetAllClients} qui n'a aucun argument,
 * {@code GetAllComptes} nécessite obligatoirement l'identifiant du client propriétaire
 * des comptes à récupérer. Les comptes sont toujours filtrés par client dans ce modèle
 * bancaire — il n'existe pas de liste globale de tous les comptes indépendamment des clients.</p>
 *
 * <h2>Double rôle des annotations</h2>
 *
 * <h3>1. Génération de l'inputSchema (tools/list)</h3>
 * <p>{@link com.wgt.imt.architecture.mcp.common.utils.JsonSchemaGenerator} introspecte cette classe
 * par réflexion pour produire l'{@code inputSchema} exposé lors de la découverte {@code tools/list} :</p>
 * <ul>
 *   <li>{@code @NotNull} → le champ apparaît dans la liste {@code "required"} du JSON Schema</li>
 *   <li>{@code @Schema(description = "...")} → alimente le champ {@code "description"} du JSON Schema</li>
 *   <li>{@code @Schema(example = "...")} → alimente le champ {@code "example"} du JSON Schema</li>
 *   <li>Le type {@code UUID} → traduit en {@code "type": "string", "format": "uuid"}</li>
 * </ul>
 *
 * <h3>2. Validation Bean (tools/call)</h3>
 * <ul>
 *   <li>{@code @NotNull} → garantit que l'identifiant du client est présent dans les arguments</li>
 * </ul>
 *
 * @see com.wgt.imt.architecture.mcp.tools.comptes.GetAllComptesToolHandler
 * @see com.wgt.imt.architecture.mcp.tools.ToolHandler
 * @see com.wgt.imt.architecture.mcp.common.utils.JsonSchemaGenerator
 * @since 0.0.1
 */
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class GetAllComptesToolArguments implements Serializable {

    @Serial
    private static final long serialVersionUID = 7612043985301476291L;

    /**
     * Identifiant unique (UUID) du client dont on veut récupérer les comptes.
     *
     * <p><strong>Effet sur l'inputSchema :</strong> ce champ génère une propriété
     * {@code "type": "string", "format": "uuid"} dans l'{@code inputSchema},
     * et apparaît dans le tableau {@code "required"} grâce à {@code @NotNull}.</p>
     */
    @Schema(example = "550e8400-e29b-41d4-a716-446655440000", description = "Identifiant du client propriétaire des comptes")
    @NotNull(message = "clientIdentifier doit être présent dans les arguments")
    private UUID clientIdentifier;
}

