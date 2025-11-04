package com.wgt.imt.architecture.mcp;

import com.wgt.imt.architecture.business.common.exception.ImtException;
import com.wgt.imt.architecture.mcp.common.model.MethodTypeEnum;
import com.wgt.imt.architecture.mcp.common.model.input.JsonRpcRequest;
import com.wgt.imt.architecture.mcp.common.model.output.InitializeResult;
import com.wgt.imt.architecture.mcp.common.model.output.JsonRpcError;
import com.wgt.imt.architecture.mcp.common.model.output.JsonRpcResponse;
import com.wgt.imt.architecture.mcp.common.utils.RequestUtils;
import com.wgt.imt.architecture.mcp.common.utils.ResponseUtils;
import com.wgt.imt.architecture.mcp.common.validators.JsonVersionValidatorStep;
import com.wgt.imt.architecture.mcp.common.validators.MethodValidatorStep;
import com.wgt.imt.architecture.mcp.prompts.PromptHandler;
import com.wgt.imt.architecture.mcp.resources.ResourceHandler;
import com.wgt.imt.architecture.mcp.tools.ToolHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Contrôleur JSON-RPC 2.0 pour le protocole MCP.
 *
 * <h2>Principe JSON-RPC 2.0 — un seul endpoint, routing par {@code method}</h2>
 * <p>Ce contrôleur n'expose qu'<strong>un seul endpoint</strong> : {@code POST /imt/v1/mcp}.</p>
 * <p>Le routing est effectué via le champ {@code method} du corps JSON-RPC, et non via l'URL.
 * Cela est conforme à la <a href="https://www.jsonrpc.org/specification">spécification JSON-RPC 2.0</a>
 * et au protocole MCP officiel.</p>
 *
 * <pre>
 * Action           Endpoint unique JSON-RPC 2.0
 * ─────────────    ─────────────────────────────────────
 * tools/list    →  POST /mcp  { "method": "tools/list"     }
 * tools/call    →  POST /mcp  { "method": "tools/call"     }
 * resources/list→  POST /mcp  { "method": "resources/list" }
 * resources/read→  POST /mcp  { "method": "resources/read" }
 * prompts/list  →  POST /mcp  { "method": "prompts/list"   }
 * prompts/get   →  POST /mcp  { "method": "prompts/get"    }
 * </pre>
 *
 * <h2>Codes d'erreur JSON-RPC 2.0</h2>
 * <p>Contrairement à REST qui utilise les codes HTTP, JSON-RPC définit ses propres codes
 * dans le corps de la réponse (voir {@link JsonRpcError}) :</p>
 * <ul>
 *   <li>{@code -32700} Parse error</li>
 *   <li>{@code -32600} Invalid Request</li>
 *   <li>{@code -32601} Method not found</li>
 *   <li>{@code -32602} Invalid params</li>
 *   <li>{@code -32603} Internal error</li>
 * </ul>
 *
 * @see JsonRpcRequest
 * @see JsonRpcResponse
 * @see JsonRpcError
 * @see <a href="https://www.jsonrpc.org/specification">Spécification JSON-RPC 2.0</a>
 * @since 0.0.1
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/imt/v1/mcp")
public class JsonRpcController {
    private final Collection<ToolHandler<?>> toolHandlers;
    private final Collection<ResourceHandler> resourceHandlers;
    private final Collection<PromptHandler> promptHandlers;

    /**
     * Point d'entrée unique du protocole JSON-RPC 2.0.
     *
     * <p>Toutes les actions MCP transitent par cet endpoint.
     * Le champ {@code method} de la requête détermine quelle action exécuter.</p>
     *
     * <p>Méthodes supportées :</p>
     * <ul>
     *   <li>{@code initialize}      — handshake : retourne les capacités statiques du serveur</li>
     *   <li>{@code tools/list}      — découverte des outils</li>
     *   <li>{@code tools/call}      — exécution d'un outil</li>
     *   <li>{@code resources/list}  — découverte des resources</li>
     *   <li>{@code resources/read}  — lecture d'une resource</li>
     *   <li>{@code prompts/list}    — découverte des prompts</li>
     *   <li>{@code prompts/get}     — rendu d'un prompt</li>
     * </ul>
     *
     * <p>Note : HTTP retourne toujours <strong>200 OK</strong>, même en cas d'erreur.
     * C'est la spec JSON-RPC : le code HTTP signale uniquement les erreurs de transport,
     * pas les erreurs applicatives (qui sont dans le champ {@code error} du corps).</p>
     * <h2>Chaîne de validation</h2>
     * <p>Avant le routing, deux étapes de validation sont exécutées en séquence
     * (patron <em>Chain of Responsibility</em>) :</p>
     * <ol>
     *   <li>{@link JsonVersionValidatorStep} — vérifie que {@code jsonrpc == "2.0"}</li>
     *   <li>{@link MethodValidatorStep} — vérifie que {@code method} est connue
     *       et que les {@code params} requis sont présents</li>
     * </ol>
     * <p>Si une étape échoue, une {@link com.wgt.imt.architecture.mcp.common.model.exception.InvalidRequestException}
     * est levée et interceptée localement pour retourner l'erreur JSON-RPC appropriée
     * ({@code -32600} ou {@code -32602}) en conservant le {@code id} de corrélation.</p>
     *
     * @param request La requête JSON-RPC 2.0
     * @return Une réponse JSON-RPC 2.0 (succès ou erreur), toujours avec HTTP 200
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public JsonRpcResponse dispatch(@RequestBody final JsonRpcRequest request) throws ImtException {
        //Validation des paramètres de l'appel
        new JsonVersionValidatorStep()
                .linkWith(new MethodValidatorStep())
                .validate(request)
                .throwIfInvalid();

        //Récupération des attributs
        final Object id = request.getId();
        final Map<String, Object> params = request.getParams();

        //Traitement de la demande
        try {
            return MethodTypeEnum.fromPath(request.getMethod())
                    .map(method -> switch (method) {
                        case MethodTypeEnum.INITIALIZE -> JsonRpcResponse.success(id, InitializeResult.defaultResult());
                        case MethodTypeEnum.TOOLS_LIST -> ResponseUtils.handleResponseList(request, this.toolHandlers);
                        case MethodTypeEnum.RESOURCES_LIST ->
                                ResponseUtils.handleResponseList(request, this.resourceHandlers);
                        case MethodTypeEnum.PROMPTS_LIST ->
                                ResponseUtils.handleResponseList(request, this.promptHandlers);
                        case MethodTypeEnum.TOOLS_CALL -> this.handleToolsCall(id, params);
                        case MethodTypeEnum.RESOURCES_READ -> this.handleResourcesRead(id, params);
                        case MethodTypeEnum.PROMPTS_GET -> this.handlePromptsGet(id, params);
                    })
                    .orElse(JsonRpcResponse.error(request.getId(), JsonRpcError.methodNotFound(request.getMethod())));
        } catch (final Exception e) {
            log.error("Unexpected error dispatching method {}", request.getMethod(), e);
            return JsonRpcResponse.error(request.getId(), JsonRpcError.internalError(e.getMessage()));
        }
    }

    /**
     * Gère un appel {@code tools/call} : recherche le {@link ToolHandler} par son nom
     * et délègue l'exécution.
     *
     * @param id     L'identifiant JSON-RPC de corrélation
     * @param params Les paramètres de la requête ({@code name} et {@code arguments})
     * @return Réponse succès avec le résultat de l'outil,
     * ou erreur {@code -32601} si l'outil est inconnu,
     * ou erreur {@code -32000} si l'exécution lève une {@link ImtException} (erreur métier)
     */
    private JsonRpcResponse handleToolsCall(final Object id, final Map<String, Object> params) {
        final String name = RequestUtils.extractName(params);
        final Map<String, Object> arguments = RequestUtils.extractObjectArguments(params);
        return Optional.ofNullable(this.toolHandlers).stream()
                .flatMap(Collection::stream)
                .filter(handler -> handler.getName().equals(name))
                .findFirst()
                .map(handler -> {
                    try {
                        return JsonRpcResponse.success(id, handler.apply(arguments));
                    } catch (final ImtException e) {
                        return JsonRpcResponse.error(id, JsonRpcError.serverError(e.getMessage()));
                    }
                })
                .orElseGet(() -> JsonRpcResponse.error(id, JsonRpcError.methodNotFound("Unknown tool: " + name)));

    }

    /**
     * Gère un appel {@code resources/read} : recherche le {@link ResourceHandler} par son URI
     * et retourne son contenu.
     *
     * @param id     L'identifiant JSON-RPC de corrélation
     * @param params Les paramètres de la requête ({@code uri})
     * @return Réponse succès avec le contenu de la ressource,
     * ou erreur {@code -32602} si l'URI est inconnue
     */
    private JsonRpcResponse handleResourcesRead(final Object id, final Map<String, Object> params) {
        final String uri = RequestUtils.extractURI(params);
        return Optional.ofNullable(this.resourceHandlers).stream()
                .flatMap(Collection::stream)
                .filter(handler -> handler.getResourceUri().equals(uri))
                .findFirst()
                .map(handler -> JsonRpcResponse.success(id, handler.apply()))
                .orElseGet(() -> JsonRpcResponse.error(id, JsonRpcError.invalidParams("Unknown resource URI: " + uri)));
    }

    /**
     * Gère un appel {@code prompts/get} : recherche le {@link PromptHandler} par son nom
     * et retourne le prompt rendu avec les arguments fournis.
     *
     * @param id     L'identifiant JSON-RPC de corrélation
     * @param params Les paramètres de la requête ({@code name} et {@code arguments})
     * @return Réponse succès avec le prompt rendu,
     * ou erreur {@code -32601} si le prompt est inconnu
     */
    private JsonRpcResponse handlePromptsGet(final Object id, final Map<String, Object> params) {
        final String name = RequestUtils.extractName(params);
        final Map<String, String> arguments = RequestUtils.extractStringArguments(params);
        return Optional.ofNullable(this.promptHandlers).stream()
                .flatMap(Collection::stream)
                .filter(handler -> handler.getName().equals(name))
                .findFirst()
                .map(handler -> JsonRpcResponse.success(id, handler.apply(arguments)))
                .orElseGet(() -> JsonRpcResponse.error(id, JsonRpcError.methodNotFound("Unknown prompt: " + name)));
    }
}