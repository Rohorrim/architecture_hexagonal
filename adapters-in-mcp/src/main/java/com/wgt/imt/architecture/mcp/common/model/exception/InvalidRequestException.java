package com.wgt.imt.architecture.mcp.common.model.exception;

import com.wgt.imt.architecture.business.common.exception.ImtException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.io.Serial;

/**
 * Exception levée lorsqu'une requête JSON-RPC 2.0 est structurellement invalide.
 *
 * <h2>Cas d'usage</h2>
 * <ul>
 *   <li>Champ {@code jsonrpc} absent ou différent de {@code "2.0"} → code {@code -32600}</li>
 *   <li>Champ {@code method} absent ou vide → code {@code -32600}</li>
 *   <li>Méthode inconnue → code {@code -32601}</li>
 *   <li>Paramètres requis manquants dans {@code params} → code {@code -32602}</li>
 * </ul>
 *
 * <h2>Cycle de vie</h2>
 * <p>Levée par {@link JsonVersionValidatorStep} ou {@link MethodValidatorStep},
 * elle est ensuite interceptée directement dans
 * {@link com.wgt.imt.architecture.mcp.JsonRpcController#dispatch} pour retourner
 * une réponse JSON-RPC avec le code d'erreur approprié.
 * Grâce au champ {@link #id}, la corrélation avec la requête d'origine est conservée
 * même en cas d'erreur de validation.</p>
 *
 * @see JsonVersionValidatorStep
 * @see MethodValidatorStep
 * @see com.wgt.imt.architecture.mcp.common.model.output.JsonRpcError
 * @since 0.0.1
 */
@Getter
@RequiredArgsConstructor
@ToString
public class InvalidRequestException extends ImtException {

    @Serial
    private static final long serialVersionUID = -6604417189957842847L;

    /**
     * Identifiant JSON-RPC de la requête invalide.
     *
     * <p>Conservé dans l'exception pour que la réponse d'erreur puisse inclure
     * le bon {@code id} de corrélation, conformément à la spec JSON-RPC 2.0.</p>
     */
    private final Object id;

    /**
     * Description de la raison de l'invalidité.
     *
     * <p>Ex : {@code "jsonrpc field must be \"2.0\""}, {@code "method field is required"}.</p>
     */
    private final String message;
}
