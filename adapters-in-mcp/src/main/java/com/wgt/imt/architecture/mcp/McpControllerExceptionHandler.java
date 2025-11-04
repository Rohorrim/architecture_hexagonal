package com.wgt.imt.architecture.mcp;

import com.wgt.imt.architecture.business.common.exception.ImtException;
import com.wgt.imt.architecture.mcp.common.model.exception.InvalidRequestException;
import com.wgt.imt.architecture.mcp.common.model.exception.McpNotFoundException;
import com.wgt.imt.architecture.mcp.common.model.output.JsonRpcError;
import com.wgt.imt.architecture.mcp.common.model.output.JsonRpcResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Gestionnaire global d'exceptions pour le contrôleur JSON-RPC 2.0 MCP.
 *
 * <h2>Règle fondamentale JSON-RPC 2.0</h2>
 * <p>Contrairement à REST, le protocole JSON-RPC 2.0 impose que HTTP retourne
 * <strong>toujours 200 OK</strong>. Le code HTTP ne signale que les erreurs de
 * transport (connexion perdue, etc.). Les erreurs applicatives sont encodées
 * <em>dans le corps</em> de la réponse via le champ {@code error} de
 * {@link JsonRpcResponse}.</p>
 *
 * <h2>Rôle réel de ce handler</h2>
 * <p>Le {@link com.wgt.imt.architecture.mcp.JsonRpcController} intercepte
 * lui-même la quasi-totalité des exceptions dans son bloc {@code try/catch}.
 * Ce handler est un <strong>filet de sécurité</strong> pour les exceptions
 * qui surviennent <em>avant</em> l'entrée dans la méthode du contrôleur,
 * notamment :</p>
 * <ul>
 *   <li>{@link HttpMessageNotReadableException} — Spring ne parvient pas à
 *       désérialiser le corps JSON (JSON malformé) → erreur {@code -32700}</li>
 *   <li>{@link ImtException} / {@link McpNotFoundException} — sécurité si une
 *       exception remonte malgré le catch du contrôleur</li>
 *   <li>{@link Exception} — toute autre erreur imprévue → erreur {@code -32603}</li>
 * </ul>
 *
 * <h2>Champ {@code id} dans les réponses d'erreur</h2>
 * <p>Quand l'exception est levée avant que la requête ait pu être parsée
 * (ex : JSON invalide), l'identifiant de corrélation {@code id} est inconnu.
 * La spec JSON-RPC 2.0 prescrit dans ce cas d'utiliser {@code null}.</p>
 *
 * <h2>Correspondance codes d'erreur</h2>
 * <table border="1">
 *   <tr><th>Exception</th><th>Code JSON-RPC</th><th>HTTP</th></tr>
 *   <tr><td>{@link HttpMessageNotReadableException}</td><td>{@code -32700} Parse error</td><td>200</td></tr>
 *   <tr><td>{@link ImtException}</td><td>{@code -32000} Server error</td><td>200</td></tr>
 *   <tr><td>{@link McpNotFoundException}</td><td>{@code -32601} Method not found</td><td>200</td></tr>
 *   <tr><td>{@link Exception}</td><td>{@code -32603} Internal error</td><td>200</td></tr>
 * </table>
 *
 * @see JsonRpcError
 * @see JsonRpcResponse
 * @since 0.0.1
 */
@Slf4j
@RestControllerAdvice(basePackages = "com.wgt.imt.architecture.mcp")
public class McpControllerExceptionHandler {

    /**
     * Gère les erreurs de désérialisation JSON ({@code -32700 Parse error}).
     *
     * <p>Levée par Spring <em>avant</em> d'entrer dans la méthode du contrôleur,
     * quand le corps de la requête n'est pas du JSON valide. L'identifiant
     * {@code id} est forcément {@code null} — la requête n'ayant pas pu être lue.</p>
     *
     * @param exception L'exception de désérialisation
     * @return HTTP 200 avec une erreur JSON-RPC {@code -32700}
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<JsonRpcResponse> handleParseError(final HttpMessageNotReadableException exception) {
        log.warn("JSON parse error: {}", exception.getMessage());
        return ResponseEntity.ok(JsonRpcResponse.error(null, JsonRpcError.parseError(exception.getMessage())));
    }

    /**
     * Filet de sécurité pour les {@link InvalidRequestException} ({@code -32600 Invalid Request}).
     *
     * <p>Normalement interceptée directement dans
     * {@link com.wgt.imt.architecture.mcp.JsonRpcController#dispatch} via la chaîne de validation.
     * Ce handler ne devrait pas être atteint en fonctionnement nominal.</p>
     *
     * <p>L'identifiant de corrélation {@code id} est préservé depuis la requête d'origine
     * via {@link InvalidRequestException#getId()}.</p>
     *
     * @param exception La {@link InvalidRequestException} interceptée
     * @return HTTP 200 avec une erreur JSON-RPC {@code -32600}
     */
    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<JsonRpcResponse> handleInvalidRequestException(final InvalidRequestException exception) {
        log.warn("InvalidRequestException intercepted by global handler: {}", exception.getMessage());
        return ResponseEntity.ok(JsonRpcResponse.error(exception.getId(), JsonRpcError.invalidRequest(exception.getMessage())));
    }

    /**
     * Filet de sécurité pour les {@link McpNotFoundException} ({@code -32601 Method not found}).
     *
     * <p>Normalement interceptée directement dans
     * {@link com.wgt.imt.architecture.mcp.JsonRpcController#dispatch}.
     * Ce handler ne devrait pas être atteint en fonctionnement nominal.</p>
     *
     * <p>L'identifiant de corrélation {@code id} est préservé depuis la requête d'origine
     * via {@link McpNotFoundException#getId()}.</p>
     *
     * @param exception La {@link McpNotFoundException} interceptée
     * @return HTTP 200 avec une erreur JSON-RPC {@code -32601}
     */
    @ExceptionHandler(McpNotFoundException.class)
    public ResponseEntity<JsonRpcResponse> handleNotFoundException(final McpNotFoundException exception) {
        log.warn("McpNotFoundException intercepted by global handler: {}", exception.getMessage());
        return ResponseEntity.ok(JsonRpcResponse.error(exception.getId(), JsonRpcError.methodNotFound(exception.getMessage())));
    }

    /**
     * Filet de sécurité pour les {@link ImtException} ({@code -32000 Server error}).
     *
     * <p>Normalement interceptée directement dans
     * {@link com.wgt.imt.architecture.mcp.JsonRpcController#dispatch}.
     * Ce handler ne devrait pas être atteint en fonctionnement nominal.</p>
     *
     * <p>L'identifiant est {@code null} ici car {@link ImtException} est une exception
     * du domaine métier sans notion d'identifiant JSON-RPC. Conformément à la spec
     * JSON-RPC 2.0 (§5), {@code null} est la valeur prescrite quand l'id ne peut
     * pas être déterminé.</p>
     *
     * @param exception La {@link ImtException} interceptée
     * @return HTTP 200 avec une erreur JSON-RPC {@code -32000}
     */
    @ExceptionHandler(ImtException.class)
    public ResponseEntity<JsonRpcResponse> handleImtException(final ImtException exception) {
        log.warn("ImtException intercepted by global handler: {}", exception.getMessage());
        return ResponseEntity.ok(JsonRpcResponse.error(null, JsonRpcError.internalError(exception.getMessage())));
    }


    /**
     * Filet de sécurité ultime pour toute exception non prévue ({@code -32603 Internal error}).
     *
     * <p>Normalement interceptée par le {@code catch (Exception e)} du contrôleur.
     * Ce handler ne devrait pas être atteint en fonctionnement nominal.</p>
     *
     * <p>L'identifiant est {@code null} car une exception générique ne porte aucune
     * information sur la requête JSON-RPC d'origine. Conformément à la spec
     * JSON-RPC 2.0 (§5), {@code null} est la valeur prescrite quand l'id ne peut
     * pas être déterminé.</p>
     *
     * @param exception L'exception imprévue
     * @return HTTP 200 avec une erreur JSON-RPC {@code -32603}
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<JsonRpcResponse> handleGenericException(final Exception exception) {
        log.error("Unexpected exception intercepted by global handler", exception);
        return ResponseEntity.ok(JsonRpcResponse.error(null, JsonRpcError.internalError(exception.getMessage())));
    }
}
