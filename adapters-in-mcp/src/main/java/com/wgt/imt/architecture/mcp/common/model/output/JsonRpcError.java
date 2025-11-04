package com.wgt.imt.architecture.mcp.common.model.output;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.io.Serial;
import java.io.Serializable;

/**
 * Modèle d'une erreur JSON-RPC 2.0.
 *
 * <h2>Codes d'erreur standardisés</h2>
 * <p>Contrairement à REST qui réutilise les codes HTTP (400, 404, 500...),
 * JSON-RPC 2.0 définit ses propres codes d'erreur négatifs :</p>
 * <table border="1">
 *   <tr><th>Code</th><th>Constante</th><th>Signification</th></tr>
 *   <tr><td>{@code -32700}</td><td>{@link #PARSE_ERROR}</td><td>JSON invalide — impossible de parser la requête</td></tr>
 *   <tr><td>{@code -32600}</td><td>{@link #INVALID_REQUEST}</td><td>La requête n'est pas un objet JSON-RPC 2.0 valide</td></tr>
 *   <tr><td>{@code -32601}</td><td>{@link #METHOD_NOT_FOUND}</td><td>La méthode demandée n'existe pas</td></tr>
 *   <tr><td>{@code -32602}</td><td>{@link #INVALID_PARAMS}</td><td>Les paramètres sont invalides</td></tr>
 *   <tr><td>{@code -32603}</td><td>{@link #INTERNAL_ERROR}</td><td>Erreur interne du serveur</td></tr>
 *   <tr><td>{@code -32000} à {@code -32099}</td><td>—</td><td>Erreurs applicatives personnalisées</td></tr>
 * </table>
 *
 * <p>Les codes positifs et les codes non réservés ({@code > -32000}) sont libres
 * d'utilisation par l'application.</p>
 *
 * @param code    Code numérique identifiant le type d'erreur.
 *
 *                <p>Utiliser les constantes définies dans cette classe pour respecter la spec.
 *                Les valeurs entre {@code -32099} et {@code -32000} sont réservées aux erreurs
 *                serveur personnalisées.</p>
 * @param message Message court et lisible décrivant l'erreur.
 *
 *                <p>Doit rester concis (une phrase). Pour plus de détails, utiliser {@link #data}.</p>
 * @param data    Informations additionnelles sur l'erreur (optionnel).
 *
 *                <p>Peut contenir n'importe quelle valeur JSON : un objet, une chaîne, etc.
 *                Par exemple : la stack trace en développement, ou les détails de validation.</p>
 * @see JsonRpcResponse
 * @see <a href="https://www.jsonrpc.org/specification#error_object">JSON-RPC 2.0 Error Object</a>
 * @since 0.0.1
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record JsonRpcError(int code, String message, Object data) implements Serializable {

    /**
     * Erreur de parsing JSON : la requête reçue n'est pas du JSON valide.
     */
    public static final int PARSE_ERROR = -32700;

    // =========================================================================
    // Codes d'erreur standard JSON-RPC 2.0
    // =========================================================================
    /**
     * Requête invalide : structure JSON-RPC incorrecte (champs manquants, etc.).
     */
    public static final int INVALID_REQUEST = -32600;
    /**
     * Méthode inconnue : la méthode demandée n'est pas enregistrée sur ce serveur.
     */
    public static final int METHOD_NOT_FOUND = -32601;
    /**
     * Paramètres invalides : les params ne correspondent pas à ce qu'attend la méthode.
     */
    public static final int INVALID_PARAMS = -32602;
    /**
     * Erreur interne serveur : erreur inattendue lors du traitement.
     */
    public static final int INTERNAL_ERROR = -32603;
    @Serial
    private static final long serialVersionUID = -1122334455667788990L;

    // =========================================================================
    // Champs de l'objet erreur
    // =========================================================================

    /**
     * Méthode de fabrique : erreur de parsing JSON.
     *
     * <p>Utilisée lorsque Spring ne peut pas désérialiser le corps de la requête
     * (JSON malformé). Le champ {@code id} sera {@code null} car la requête
     * n'a pas pu être parsée.</p>
     */
    public static JsonRpcError parseError(final String detail) {
        return JsonRpcError.builder()
                .code(PARSE_ERROR)
                .message("Parse error: " + detail)
                .build();
    }

    /**
     * Méthode de fabrique : méthode non trouvée.
     */
    public static JsonRpcError methodNotFound(final String methodName) {
        return JsonRpcError.builder()
                .code(METHOD_NOT_FOUND)
                .message("Method not found: " + methodName)
                .build();
    }

    /**
     * Méthode de fabrique : requête invalide.
     */
    public static JsonRpcError invalidRequest(final String detail) {
        return JsonRpcError.builder()
                .code(INVALID_REQUEST)
                .message("Invalid Request: " + detail)
                .build();
    }

    /**
     * Méthode de fabrique : paramètres invalides.
     */
    public static JsonRpcError invalidParams(final String detail) {
        return JsonRpcError.builder()
                .code(INVALID_PARAMS)
                .message("Invalid params: " + detail)
                .build();
    }

    /**
     * Méthode de fabrique : erreur interne serveur.
     */
    public static JsonRpcError internalError(final String detail) {
        return JsonRpcError.builder()
                .code(INTERNAL_ERROR)
                .message("Internal error: " + detail)
                .build();
    }

    /**
     * Méthode de fabrique : erreur applicative métier ({@code -32000 Server error}).
     *
     * <p>À utiliser pour les erreurs <em>métier</em> remontées par le domain
     * (ex : client introuvable, règle métier violée).
     * Distinct de {@link #internalError} ({@code -32603}) qui est réservé aux
     * erreurs <em>techniques</em> imprévues.</p>
     */
    public static JsonRpcError serverError(final String detail) {
        return JsonRpcError.builder()
                .code(-32000)
                .message("Server error: " + detail)
                .build();
    }
}

