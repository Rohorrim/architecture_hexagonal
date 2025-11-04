package com.wgt.imt.architecture.mcp.common.model.input;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * Modèle d'une requête JSON-RPC 2.0.
 *
 * <h2>Spécification JSON-RPC 2.0</h2>
 * <p>Une requête JSON-RPC 2.0 est un objet JSON avec quatre champs :</p>
 * <ul>
 *   <li>{@code jsonrpc} — toujours {@code "2.0"} (identifie la version du protocole)</li>
 *   <li>{@code id} — identifiant unique de la requête, retourné dans la réponse pour
 *       permettre la corrélation (absent pour les notifications)</li>
 *   <li>{@code method} — nom de la méthode à appeler (ex : {@code "tools/call"})</li>
 *   <li>{@code params} — paramètres de la méthode (objet JSON)</li>
 * </ul>
 *
 * <h2>Requête vs Notification</h2>
 * <ul>
 *   <li><strong>Requête</strong> : {@code id} présent → le client attend une réponse</li>
 *   <li><strong>Notification</strong> : {@code id} absent → pas de réponse attendue
 *       (ex : {@code initialized} dans le handshake MCP)</li>
 * </ul>
 *
 * @see <a href="https://www.jsonrpc.org/specification">Spécification JSON-RPC 2.0</a>
 * @since 0.0.1
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonRpcRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1234567890987654321L;

    /**
     * Version du protocole JSON-RPC. Doit toujours valoir {@code "2.0"}.
     *
     * <p>Ce champ permet au serveur de détecter si la requête respecte bien JSON-RPC 2.0.
     * Si absent ou différent de {@code "2.0"}, le serveur doit retourner l'erreur {@code -32600}
     * (Invalid Request).</p>
     */
    private String jsonrpc;

    /**
     * Identifiant de la requête, choisi par le client.
     *
     * <p>Peut être un entier, une chaîne, ou null (notification).
     * Le serveur le renvoie tel quel dans la réponse pour permettre
     * au client de corréler les réponses à leurs requêtes.
     * Particulièrement utile lors d'envois groupés (batch).</p>
     */
    private Object id;

    /**
     * Nom de la méthode à appeler.
     *
     * <p>Dans MCP, les méthodes correspondent aux primitives :
     * {@code "tools/list"}, {@code "tools/call"}, {@code "resources/list"},
     * {@code "resources/read"}, {@code "prompts/list"}, {@code "prompts/get"},
     * {@code "initialize"}, etc.</p>
     */
    private String method;

    /**
     * Paramètres de la méthode sous forme de paires clé/valeur.
     *
     * <p>La structure des paramètres dépend de la méthode appelée.
     * Pour {@code "tools/call"} par exemple :
     * {@code { "name": "GetOneClient", "arguments": { "identifier": "..." } }}</p>
     */
    private Map<String, Object> params;
}

