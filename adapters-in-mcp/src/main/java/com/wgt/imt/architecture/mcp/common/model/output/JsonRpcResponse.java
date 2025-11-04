package com.wgt.imt.architecture.mcp.common.model.output;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.io.Serial;
import java.io.Serializable;

/**
 * Modèle d'une réponse JSON-RPC 2.0.
 *
 * <h2>Règle d'exclusivité result / error</h2>
 * <p>La spec JSON-RPC 2.0 impose que <strong>exactement un</strong> des deux champs
 * soit présent dans chaque réponse :</p>
 * <ul>
 *   <li>Succès → {@code result} présent, {@code error} absent</li>
 *   <li>Erreur → {@code error} présent, {@code result} absent</li>
 * </ul>
 * <p>L'annotation {@code @JsonInclude(NON_NULL)} garantit que le champ absent
 * n'apparaît pas dans le JSON sérialisé.</p>
 *
 * @param jsonrpc Version du protocole. Toujours {@code "2.0"}.
 * @param id      Identifiant copié depuis la requête d'origine.
 *
 *                <p>Permet au client de corréler cette réponse à la requête qui l'a déclenchée.
 *                Il vaut {@code null} uniquement si la requête elle-même était une notification
 *                (pas d'id) ou si une erreur de parsing a empêché de lire l'id.</p>
 * @param result  Résultat de l'appel (présent uniquement en cas de succès).
 *
 *                <p>Sa structure dépend de la méthode appelée :
 *                liste pour {@code tools/list}, objet pour {@code tools/call}, etc.</p>
 * @param error   Détails de l'erreur (présent uniquement en cas d'échec).
 * @see JsonRpcError
 * @see <a href="https://www.jsonrpc.org/specification#response_object">JSON-RPC 2.0 Response Object</a>
 * @since 0.0.1
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record JsonRpcResponse(String jsonrpc, Object id, Object result, JsonRpcError error) implements Serializable {
    private static final String JSONRPC_VERSION = "2.0";

    @Serial
    private static final long serialVersionUID = 987654321012345678L;

    /**
     * Méthode de fabrique pour une réponse de succès.
     *
     * @param id     L'identifiant copié depuis la requête
     * @param result Le résultat de l'appel
     * @return Une réponse JSON-RPC 2.0 de succès
     */
    public static JsonRpcResponse success(final Object id, final Object result) {
        return JsonRpcResponse.builder()
                .jsonrpc(JSONRPC_VERSION)
                .id(id)
                .result(result)
                .build();
    }

    /**
     * Méthode de fabrique pour une réponse d'erreur.
     *
     * @param id    L'identifiant copié depuis la requête (peut être null)
     * @param error Les détails de l'erreur
     * @return Une réponse JSON-RPC 2.0 d'erreur
     */
    public static JsonRpcResponse error(final Object id, final JsonRpcError error) {
        return JsonRpcResponse.builder()
                .jsonrpc(JSONRPC_VERSION)
                .id(id)
                .error(error)
                .build();
    }
}

