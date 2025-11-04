package com.wgt.imt.architecture.mcp.tools.clients.input;

import com.wgt.imt.architecture.mcp.tools.clients.GetOneClientToolHandler;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/**
 * Arguments d'entrée pour l'outil MCP <strong>"GetOneClient"</strong>.
 *
 * <h2>Rôle de cette classe</h2>
 * <p>Cette classe est le <strong>contrat d'entrée fortement typé</strong> de l'outil {@code GetOneClient}.
 * Elle est instanciée automatiquement par {@link com.wgt.imt.architecture.mcp.common.utils.JsonUtils}
 * à partir de la chaîne JSON brute contenue dans le champ {@code arguments} de la requête {@code tools/call}.</p>
 *
 * <h2>Double rôle des annotations</h2>
 * <p>Les annotations portées par les champs de cette classe ont <strong>deux effets distincts</strong> :</p>
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
 * <p>Lors de l'exécution, après désérialisation JSON, les annotations Jakarta Validation
 * peuvent être utilisées pour valider l'objet instancié :</p>
 * <ul>
 *   <li>{@code @NotNull} → garantit que l'identifiant est présent dans les arguments</li>
 * </ul>
 *
 * <h2>inputSchema généré automatiquement</h2>
 * <pre>
 * {
 *   "type": "object",
 *   "properties": {
 *     "identifier": {
 *       "description": "Client identifier",
 *       "example": "550e8400-e29b-41d4-a716-446655440000",
 *       "type": "string",
 *       "format": "uuid"
 *     }
 *   },
 *   "required": ["identifier"]
 * }
 * </pre>
 *
 * @see GetOneClientToolHandler
 * @see com.wgt.imt.architecture.mcp.tools.ToolHandler
 * @see com.wgt.imt.architecture.mcp.common.utils.JsonSchemaGenerator
 * @since 0.0.1
 */
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class GetOneToolArguments implements Serializable {

    @Serial
    private static final long serialVersionUID = -5290573433852754521L;

    /**
     * Identifiant unique (UUID) du client à récupérer.
     *
     * <p><strong>Effet sur l'inputSchema :</strong> ce champ génère une propriété
     * {@code "type": "string", "format": "uuid"} dans l'{@code inputSchema},
     * et apparaît dans le tableau {@code "required"} grâce à {@code @NotNull}.</p>
     *
     * <p><strong>Pourquoi UUID plutôt que String ?</strong> L'utilisation de {@link UUID}
     * garantit le typage fort côté Java : une valeur non conforme au format UUID échouera
     * à la désérialisation Jackson avant même d'atteindre la logique métier.</p>
     */
    @Schema(example = "550e8400-e29b-41d4-a716-446655440000", description = "Client identifier")
    @NotNull(message = "identifier doit être présent dans les arguments")
    private UUID identifier;
}