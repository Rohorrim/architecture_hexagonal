package com.wgt.imt.architecture.mcp.common.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.util.Map;

/**
 * Utilitaire de sérialisation/désérialisation JSON basé sur la bibliothèque <strong>Jackson</strong>.
 *
 * <h2>Rôle dans le système MCP</h2>
 * <p>Dans le flux d'un appel {@code tools/call} JSON-RPC 2.0, les arguments arrivent sous forme de
 * {@code Map<String, Object>} extraite du champ {@code params.arguments} de la
 * {@link com.wgt.imt.architecture.mcp.common.model.input.JsonRpcRequest}.
 * Cette classe se charge de convertir cette Map en objet Java fortement typé {@code <I>},
 * avant que la logique métier du {@link com.wgt.imt.architecture.mcp.tools.ToolHandler} ne s'exécute.</p>
 *
 * <h2>Patron de conception : Factory Method + Singleton-like</h2>
 * <p>La classe est <strong>non instanciable directement</strong> (constructeur privé).
 * On l'utilise via la méthode de fabrique statique {@link #withDefaultConf()},
 * ce qui garantit une configuration Jackson cohérente dans toute l'application.</p>
 *
 * <h2>Configuration Jackson choisie</h2>
 * <ul>
 *   <li>{@code FAIL_ON_UNKNOWN_PROPERTIES = false} — tolère les champs JSON inconnus :
 *       si le LLM envoie des champs supplémentaires, la désérialisation ne plante pas</li>
 *   <li>{@code FAIL_ON_EMPTY_BEANS = false} — tolère les classes sans champs (ex : arguments vides)</li>
 *   <li>{@code NON_NULL inclusion} — les champs Java {@code null} ne sont pas sérialisés en JSON,
 *       ce qui allège les réponses</li>
 * </ul>
 */
public class JsonUtils {

    /**
     * L'instance Jackson configurée.
     */
    private final ObjectMapper objectMapper;

    /**
     * Constructeur privé — utilise {@link #withDefaultConf()} pour créer une instance.
     */
    private JsonUtils() {
        this(new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL));
    }

    /**
     * Constructeur protégé permettant l'injection d'un {@link ObjectMapper} personnalisé.
     *
     * <p>Utilisé uniquement dans les <strong>tests unitaires</strong> pour injecter
     * un mock ou une configuration différente, sans modifier la configuration de production.</p>
     *
     * @param objectMapper L'instance Jackson à utiliser
     */
    protected JsonUtils(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Méthode de fabrique statique — crée une instance avec la configuration par défaut.
     *
     * <p>Usage typique dans le code de production :</p>
     * <pre>
     * {@code
     * MyArguments args = JsonUtils.withDefaultConf().read(MyArguments.class, jsonString);
     * }
     * </pre>
     *
     * @return Une nouvelle instance de {@link JsonUtils} avec la configuration standard
     */
    public static JsonUtils withDefaultConf() {
        return new JsonUtils();
    }

    /**
     * Convertit une {@link Map} en un objet Java du type spécifié.
     *
     * <p>Utilisé par {@link com.wgt.imt.architecture.mcp.tools.ToolHandler#apply(Map)}
     * pour convertir les arguments JSON (reçus comme {@code Map<String, Object>}) en objet
     * fortement typé {@code <I>}. Jackson est utilisé comme pivot : Map → objet typé.</p>
     *
     * <p>C'est l'approche conforme à la spec MCP où {@code arguments} est un objet JSON,
     * non une chaîne encodée.</p>
     *
     * @param <T>       Le type Java cible
     * @param valueType La classe Java cible (ex : {@code GetOneClientArguments.class})
     * @param map       La Map à convertir
     * @return L'objet Java de type {@code T}
     * @throws RuntimeException si la conversion échoue
     */
    public <T> T readFromMap(final Class<T> valueType, final Map<String, Object> map) {
        try {
            return this.objectMapper.convertValue(map, valueType);
        } catch (final IllegalArgumentException e) {
            throw new RuntimeException(String.format("Failed to convert Map into %s : %s", valueType.getSimpleName(), e.getMessage()), e);
        }
    }
}
