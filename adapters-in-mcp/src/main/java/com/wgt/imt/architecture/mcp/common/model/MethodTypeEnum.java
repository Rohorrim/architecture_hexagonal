package com.wgt.imt.architecture.mcp.common.model;

import lombok.AllArgsConstructor;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Énumération des méthodes JSON-RPC 2.0 supportées par le serveur MCP.
 *
 * <h2>Rôle dans le routing</h2>
 * <p>Dans le protocole JSON-RPC 2.0, le champ {@code method} de la requête détermine
 * quelle action exécuter. Cette énumération centralise toutes les valeurs valides
 * et leur path JSON correspondant, utilisés par :</p>
 * <ul>
 *   <li>{@link com.wgt.imt.architecture.mcp.JsonRpcController#dispatch} — routing vers le bon handler</li>
 *   <li>{@link com.wgt.imt.architecture.mcp.common.validators.MethodValidatorStep} — validation de la méthode</li>
 * </ul>
 *
 * <h2>Correspondance avec la spec MCP</h2>
 * <pre>
 * Enum constant      Path JSON-RPC       Primitive MCP
 * ───────────────    ─────────────────   ──────────────
 * INITIALIZE         "initialize"        Handshake d'initialisation (réponse statique)
 * TOOLS_LIST         "tools/list"        Découverte des outils
 * TOOLS_CALL         "tools/call"        Exécution d'un outil
 * RESOURCES_LIST     "resources/list"    Découverte des ressources
 * RESOURCES_READ     "resources/read"    Lecture d'une ressource (resources/read)
 * PROMPTS_LIST       "prompts/list"      Découverte des prompts
 * PROMPTS_GET        "prompts/get"       Rendu d'un prompt
 * </pre>
 *
 * @see com.wgt.imt.architecture.mcp.JsonRpcController
 * @see com.wgt.imt.architecture.mcp.common.validators.MethodValidatorStep
 * @since 0.0.1
 */
@AllArgsConstructor
public enum MethodTypeEnum {

    /**
     * Handshake d'initialisation MCP ({@code initialize}).
     *
     * <p>Permet à un client de découvrir les capacités statiques du serveur.
     * Comme ce serveur ne repose pas sur SSE ni Streamable HTTP, il n'y a
     * pas de véritable session ouverte : la réponse est entièrement statique.</p>
     */
    INITIALIZE("initialize"),

    /**
     * Découverte de tous les outils disponibles ({@code tools/list}).
     */
    TOOLS_LIST("tools/list"),

    /**
     * Exécution d'un outil identifié par son nom ({@code tools/call}).
     */
    TOOLS_CALL("tools/call"),

    /**
     * Découverte de toutes les ressources disponibles ({@code resources/list}).
     */
    RESOURCES_LIST("resources/list"),

    /**
     * Lecture du contenu d'une ressource identifiée par son URI ({@code resources/read}).
     */
    RESOURCES_READ("resources/read"),

    /**
     * Découverte de tous les prompts disponibles ({@code prompts/list}).
     */
    PROMPTS_LIST("prompts/list"),

    /**
     * Rendu d'un prompt identifié par son nom avec ses arguments ({@code prompts/get}).
     */
    PROMPTS_GET("prompts/get");

    /**
     * La valeur du champ {@code method} dans la requête JSON-RPC 2.0.
     */
    private final String path;

    /**
     * Recherche la constante correspondant au {@code path} JSON-RPC fourni.
     *
     * <p>Utilisée dans {@link com.wgt.imt.architecture.mcp.JsonRpcController#dispatch}
     * et {@link com.wgt.imt.architecture.mcp.common.validators.MethodValidatorStep#check}
     * pour convertir la valeur brute du champ {@code method} en constante typée.</p>
     *
     * @param path La valeur du champ {@code method} (ex : {@code "tools/call"})
     * @return Un {@link Optional} contenant la constante correspondante,
     * ou {@link Optional#empty()} si la méthode est inconnue
     */
    public static Optional<MethodTypeEnum> fromPath(final String path) {
        return Stream.of(MethodTypeEnum.values())
                .filter(methodTypeEnum -> methodTypeEnum.path.equals(path))
                .findFirst();
    }
}
