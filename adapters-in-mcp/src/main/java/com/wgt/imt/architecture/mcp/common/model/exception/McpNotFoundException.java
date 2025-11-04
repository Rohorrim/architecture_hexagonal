package com.wgt.imt.architecture.mcp.common.model.exception;

import com.wgt.imt.architecture.business.common.exception.ImtException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.io.Serial;

/**
 * Exception levée lorsqu'une ressource ou un prompt MCP est introuvable.
 *
 * <h2>Cas d'usage</h2>
 * <ul>
 *   <li>{@code resources/read} — URI inconnue (ex : {@code reference://inexistant})</li>
 *   <li>{@code prompts/get} — nom de prompt inconnu (ex : {@code "inconnu"})</li>
 * </ul>
 *
 * <h2>Rôle dans le flux JSON-RPC 2.0</h2>
 * <p>Cette exception sert de <strong>filet de sécurité</strong> dans
 * {@link com.wgt.imt.architecture.mcp.McpControllerExceptionHandler}. En fonctionnement
 * normal, {@link com.wgt.imt.architecture.mcp.JsonRpcController} gère les éléments introuvables
 * directement en retournant un {@link com.wgt.imt.architecture.mcp.common.model.output.JsonRpcError}
 * ({@code -32601 Method not found}). HTTP retourne toujours <strong>200 OK</strong> —
 * les codes d'erreur applicatifs sont dans le corps JSON-RPC, pas dans l'en-tête HTTP.</p>
 *
 * <p>Le champ {@link #id} transporte l'identifiant de corrélation JSON-RPC afin que,
 * même quand cette exception remonte jusqu'au handler global, la réponse d'erreur
 * conserve le bon {@code id} de la requête d'origine.</p>
 *
 * <h2>Différence avec ToolException</h2>
 * <table border="1">
 *   <tr><th></th><th>{@link ImtException}</th><th>{@link McpNotFoundException}</th></tr>
 *   <tr><td><strong>Cause</strong></td><td>Erreur lors de l'exécution d'un outil</td><td>Élément inexistant (URI, nom)</td></tr>
 *   <tr><td><strong>Code JSON-RPC</strong></td><td>{@code -32000} Server error</td><td>{@code -32601} Method not found</td></tr>
 * </table>
 *
 * @see com.wgt.imt.architecture.mcp.McpControllerExceptionHandler
 * @since 0.0.1
 */
@Getter
@RequiredArgsConstructor
@ToString
public class McpNotFoundException extends ImtException {

    @Serial
    private static final long serialVersionUID = -1234567890123456789L;

    /**
     * Identifiant JSON-RPC de la requête d'origine.
     *
     * <p>Conservé dans l'exception pour que la réponse d'erreur puisse inclure
     * le bon {@code id} de corrélation, conformément à la spec JSON-RPC 2.0.</p>
     */
    private final Object id;

    /**
     * Type d'élément introuvable (ex : {@code "resource"}, {@code "prompt"}).
     * Permet au client de distinguer programmatiquement la nature de l'erreur.
     */
    private final String elementType;

    /**
     * Identifiant de l'élément introuvable (URI ou nom du prompt).
     */
    private final String identifier;

    /**
     * Message descriptif retourné dans la réponse d'erreur.
     */
    private final String message;
}

