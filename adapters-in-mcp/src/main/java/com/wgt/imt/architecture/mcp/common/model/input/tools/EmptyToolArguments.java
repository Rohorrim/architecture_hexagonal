package com.wgt.imt.architecture.mcp.common.model.input.tools;

import com.wgt.imt.architecture.mcp.tools.clients.GetAllClientToolHandler;

import java.io.Serial;
import java.io.Serializable;

/**
 * Arguments d'entrée vides — utilisés par les outils MCP ne nécessitant aucun argument.
 *
 * <h2>Pourquoi cette classe existe-t-elle ?</h2>
 * <p>L'interface {@link com.wgt.imt.architecture.mcp.tools.ToolHandler} est <strong>générique</strong>
 * ({@code ToolHandler<I>}) et requiert toujours un type d'arguments {@code I}.
 * Pour les outils sans argument (ex : {@code GetAllClients}), on ne peut pas utiliser
 * {@code ToolHandler<Void>} car cela compliquerait la désérialisation JSON.
 * Cette classe sert de <strong>type nominal vide</strong> satisfaisant ce contrat.</p>
 *
 * <h2>Patron : Marker Class</h2>
 * <p>C'est un exemple du patron <em>Marker Class</em> (ou <em>Null Object</em> appliqué aux types) :
 * une classe sans état ni comportement, dont le seul rôle est de représenter
 * <em>"absence d'arguments"</em> de façon explicite et typée.</p>
 *
 * <h2>Effet sur l'inputSchema généré</h2>
 * <p>Quand {@link com.wgt.imt.architecture.mcp.common.utils.JsonSchemaGenerator} introspecte
 * cette classe, elle ne trouve aucun champ pertinent et produit un schéma minimal :</p>
 * <pre>
 * {
 *   "type": "object"
 * }
 * </pre>
 * <p>Ce schéma indique au LLM que l'outil accepte un objet JSON vide {@code {}} ou pas
 * d'arguments du tout.</p>
 *
 * <h2>Placement dans common/model/input</h2>
 * <p>Cette classe est délibérément placée dans le package {@code common} (et non dans un
 * package spécifique à un outil) car elle est <strong>partagée</strong> par tous les outils
 * sans argument, quels qu'ils soient.</p>
 *
 * @see com.wgt.imt.architecture.mcp.tools.ToolHandler
 * @see GetAllClientToolHandler
 * @see com.wgt.imt.architecture.mcp.common.utils.JsonSchemaGenerator
 * @since 0.0.1
 */
public class EmptyToolArguments implements Serializable {

    @Serial
    private static final long serialVersionUID = 4979456123399876834L;
}