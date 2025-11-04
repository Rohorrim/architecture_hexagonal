package com.wgt.imt.architecture.mcp.common.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utilitaires de manipulation des requêtes JSON-RPC 2.0.
 *
 * <p>Regroupe les helpers statiques partagés entre les différents composants
 * du contrôleur MCP pour l'extraction et la conversion des paramètres.</p>
 *
 * @since 0.0.1
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RequestUtils {

    /**
     * Clé JSON du paramètre {@code name}, utilisée par {@code tools/call} et {@code prompts/get}.
     */
    private static final String NAME_ATTRIBUTE = "name";

    /**
     * Clé JSON du paramètre {@code uri}, utilisée par {@code resources/read}.
     */
    private static final String URI_ATTRIBUTE = "uri";

    /**
     * Clé JSON du paramètre {@code arguments}, utilisée par {@code tools/call} et {@code prompts/get}.
     */
    private static final String ARGUMENTS_ATTRIBUTE = "arguments";

    /**
     * Extrait le paramètre {@code name} depuis les {@code params} d'une requête JSON-RPC.
     *
     * <p>Utilisé par les méthodes {@code tools/call} et {@code prompts/get} pour identifier
     * l'outil ou le prompt cible par son nom.</p>
     *
     * @param params Les paramètres de la requête JSON-RPC
     * @return La valeur du champ {@code name}, ou {@code null} si absent
     */
    public static String extractName(final Map<String, Object> params) {
        return (String) params.get(NAME_ATTRIBUTE);
    }

    /**
     * Extrait le paramètre {@code uri} depuis les {@code params} d'une requête JSON-RPC.
     *
     * <p>Utilisé par la méthode {@code resources/read} pour identifier la ressource
     * cible par son URI (ex : {@code reference://types-compte}).</p>
     *
     * @param params Les paramètres de la requête JSON-RPC
     * @return La valeur du champ {@code uri}, ou {@code null} si absent
     */
    public static String extractURI(final Map<String, Object> params) {
        return (String) params.get(URI_ATTRIBUTE);
    }

    /**
     * Extrait les arguments d'un appel {@code tools/call} depuis les {@code params}.
     *
     * <p>Utilise le pattern matching {@code instanceof Map<?,?>} pour éviter tout
     * unchecked cast générique. Les clés sont converties en {@link String} via
     * {@link String#valueOf}, les valeurs restent {@link Object}.</p>
     *
     * @param params Les paramètres de la requête JSON-RPC
     * @return La map des arguments typée {@code Map<String, Object>}, ou une map vide
     */
    public static Map<String, Object> extractObjectArguments(final Map<String, Object> params) {
        return params.get("arguments") instanceof Map<?, ?> raw
                ? raw.entrySet().stream()
                .collect(Collectors.toUnmodifiableMap(e -> String.valueOf(e.getKey()), Map.Entry::getValue))
                : Map.of();
    }

    /**
     * Extrait les arguments d'un appel {@code prompts/get} depuis les {@code params}.
     *
     * <p>Utilise le pattern matching {@code instanceof Map<?,?>} pour éviter tout
     * unchecked cast générique. Seules les entrées dont la valeur est une {@link String}
     * sont conservées — les autres sont silencieusement ignorées.</p>
     *
     * @param params Les paramètres de la requête JSON-RPC
     * @return La map des arguments typée {@code Map<String, String>}, ou une map vide
     */
    public static Map<String, String> extractStringArguments(final Map<String, Object> params) {
        return params.get(ARGUMENTS_ATTRIBUTE) instanceof Map<?, ?> raw
                ? raw.entrySet().stream()
                .filter(e -> e.getValue() instanceof String)
                .collect(Collectors.toUnmodifiableMap(e -> String.valueOf(e.getKey()), e -> (String) e.getValue()))
                : Map.of();
    }
}
