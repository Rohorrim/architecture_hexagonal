package com.wgt.imt.architecture.mcp.common.model.output;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serial;
import java.io.Serializable;

/**
 * Résultat de la méthode {@code initialize} du protocole MCP.
 *
 * <h2>Rôle</h2>
 * <p>Retourné en réponse à une requête JSON-RPC {@code initialize}, ce POJO déclare
 * au client les capacités statiques du serveur MCP.
 * Comme ce serveur ne repose pas sur SSE ni Streamable HTTP, il n'y a pas de
 * véritable établissement de session : la réponse est entièrement statique.</p>
 *
 * <h2>Structure MCP</h2>
 * <pre>
 * {
 *   "protocolVersion": "2024-11-05",
 *   "capabilities": {
 *     "tools":     { "listChanged": false },
 *     "resources": { "subscribe": false, "listChanged": false },
 *     "prompts":   { "listChanged": false }
 *   },
 *   "serverInfo": {
 *     "name":    "IMT Architecture Hexagonale",
 *     "version": "0.0.1"
 *   }
 * }
 * </pre>
 *
 * @param protocolVersion Version du protocole MCP négociée (ex : {@code "2024-11-05"}).
 * @param capabilities    Capacités déclarées par le serveur.
 * @param serverInfo      Informations d'identification du serveur.
 * @see <a href="https://spec.modelcontextprotocol.io/specification/basic/lifecycle/">MCP Lifecycle — initialize</a>
 * @since 0.0.1
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record InitializeResult(
        String protocolVersion,
        Capabilities capabilities,
        ServerInfo serverInfo
) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1122334455667788990L;

    /**
     * Construit une réponse {@code initialize} avec les valeurs par défaut du serveur.
     *
     * @return Un {@link InitializeResult} prêt à être sérialisé en JSON-RPC 2.0
     */
    public static InitializeResult defaultResult() {
        return new InitializeResult(
                "2024-11-05",
                Capabilities.defaults(),
                new ServerInfo("IMT Architecture Hexagonale", "0.0.1")
        );
    }

    /**
     * Capacités déclarées par le serveur MCP.
     *
     * @param tools     Capacités liées aux outils.
     * @param resources Capacités liées aux ressources.
     * @param prompts   Capacités liées aux prompts.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Capabilities(
            ToolsCapability tools,
            ResourcesCapability resources,
            PromptsCapability prompts
    ) implements Serializable {

        @Serial
        private static final long serialVersionUID = 1122334455667788991L;

        /**
         * Retourne les capacités par défaut : pas de notifications de changement de liste,
         * pas de souscription aux ressources.
         */
        public static Capabilities defaults() {
            return new Capabilities(
                    new ToolsCapability(false),
                    new ResourcesCapability(false, false),
                    new PromptsCapability(false)
            );
        }
    }

    /**
     * Capacités liées aux outils ({@code tools/list}, {@code tools/call}).
     *
     * @param listChanged Indique si le serveur peut notifier un changement de liste d'outils.
     *                    Toujours {@code false} ici (pas de SSE).
     */
    public record ToolsCapability(boolean listChanged) implements Serializable {
        @Serial
        private static final long serialVersionUID = 1122334455667788992L;
    }

    /**
     * Capacités liées aux ressources ({@code resources/list}, {@code resources/read}).
     *
     * @param subscribe   Indique si le serveur supporte la souscription aux ressources.
     *                    Toujours {@code false} ici (pas de SSE).
     * @param listChanged Indique si le serveur peut notifier un changement de liste de ressources.
     *                    Toujours {@code false} ici (pas de SSE).
     */
    public record ResourcesCapability(boolean subscribe, boolean listChanged) implements Serializable {
        @Serial
        private static final long serialVersionUID = 1122334455667788993L;
    }

    /**
     * Capacités liées aux prompts ({@code prompts/list}, {@code prompts/get}).
     *
     * @param listChanged Indique si le serveur peut notifier un changement de liste de prompts.
     *                    Toujours {@code false} ici (pas de SSE).
     */
    public record PromptsCapability(boolean listChanged) implements Serializable {
        @Serial
        private static final long serialVersionUID = 1122334455667788994L;
    }

    /**
     * Informations d'identification du serveur MCP.
     *
     * @param name    Nom lisible du serveur.
     * @param version Version du serveur.
     */
    public record ServerInfo(String name, String version) implements Serializable {
        @Serial
        private static final long serialVersionUID = 1122334455667788995L;
    }
}

