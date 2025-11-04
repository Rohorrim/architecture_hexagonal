package com.wgt.imt.architecture.mcp.common.validators;

import com.wgt.imt.architecture.business.common.exception.ImtException;
import com.wgt.imt.architecture.business.common.validators.AbstractValidatorStep;
import com.wgt.imt.architecture.mcp.common.model.MethodTypeEnum;
import com.wgt.imt.architecture.mcp.common.model.exception.InvalidRequestException;
import com.wgt.imt.architecture.mcp.common.model.input.JsonRpcRequest;
import lombok.AllArgsConstructor;

import java.util.Map;

/**
 * Étape de validation JSON-RPC 2.0 : vérifie que le champ {@code method} est valide
 * et que les {@code params} requis sont présents.
 *
 * <h2>Patron : Chain of Responsibility</h2>
 * <p>Cette classe est la <strong>deuxième étape</strong> de la chaîne de validation,
 * exécutée après {@link JsonVersionValidatorStep}. Elle effectue deux contrôles :</p>
 * <ol>
 *   <li><strong>Méthode</strong> — le champ {@code method} n'est pas vide et correspond
 *       à une valeur connue de {@link MethodTypeEnum}</li>
 *   <li><strong>Paramètres</strong> — les {@code params} requis par la méthode sont présents</li>
 * </ol>
 *
 * <h2>Paramètres obligatoires par méthode</h2>
 * <table border="1">
 *   <tr><th>Méthode</th><th>Paramètre requis</th><th>Erreur si absent</th></tr>
 *   <tr><td>{@code tools/call}</td><td>{@code params.name}</td><td>{@code -32602} Invalid params</td></tr>
 *   <tr><td>{@code resources/read}</td><td>{@code params.uri}</td><td>{@code -32602} Invalid params</td></tr>
 *   <tr><td>{@code prompts/get}</td><td>{@code params.name}</td><td>{@code -32602} Invalid params</td></tr>
 *   <tr><td>{@code tools/list}, {@code resources/list}, {@code prompts/list}</td><td>Aucun</td><td>—</td></tr>
 * </table>
 *
 * @see JsonVersionValidatorStep
 * @see MethodTypeEnum
 * @see InvalidRequestException
 * @since 0.0.1
 */
@AllArgsConstructor
public class MethodValidatorStep extends AbstractValidatorStep<JsonRpcRequest> {

    /**
     * Vérifie la méthode et les paramètres de la requête.
     *
     * @param request La requête JSON-RPC à valider
     * @throws InvalidRequestException si {@code method} est absent, inconnu,
     *                                 ou si les {@code params} requis sont manquants
     */
    @Override
    public void check(final JsonRpcRequest request) throws ImtException {
        if (request.getMethod() == null || request.getMethod().isBlank()) {
            throw new InvalidRequestException(request.getId(), "method field is required");
        }

        final MethodTypeEnum method = MethodTypeEnum.fromPath(request.getMethod())
                .orElseThrow(() -> new InvalidRequestException(request.getId(), "method not found: " + request.getMethod()));

        switch (method) {
            case MethodTypeEnum.TOOLS_CALL -> this.validToolsParam(request.getId(), request.getParams());
            case MethodTypeEnum.RESOURCES_READ -> this.validResourcesParams(request.getId(), request.getParams());
            case MethodTypeEnum.PROMPTS_GET -> this.validPromptsParams(request.getId(), request.getParams());
        }
    }

    /**
     * Vérifie que {@code params.name} est présent pour un appel {@code tools/call}.
     *
     * @param id     L'identifiant JSON-RPC de corrélation
     * @param params Les paramètres de la requête
     * @throws InvalidRequestException si {@code params} est null ou ne contient pas la clé {@code name}
     */
    private void validToolsParam(final Object id, final Map<String, Object> params) throws InvalidRequestException {
        if (params == null || !params.containsKey("name")) {
            throw new InvalidRequestException(id, "params.name is required for tools/call");
        }
    }

    /**
     * Vérifie que {@code params.uri} est présent pour un appel {@code resources/read}.
     *
     * @param id     L'identifiant JSON-RPC de corrélation
     * @param params Les paramètres de la requête
     * @throws InvalidRequestException si {@code params} est null ou ne contient pas la clé {@code uri}
     */
    private void validResourcesParams(final Object id, final Map<String, Object> params) throws InvalidRequestException {
        if (params == null || !params.containsKey("uri")) {
            throw new InvalidRequestException(id, "params.uri is required for resources/read");
        }
    }

    /**
     * Vérifie que {@code params.name} est présent pour un appel {@code prompts/get}.
     *
     * @param id     L'identifiant JSON-RPC de corrélation
     * @param params Les paramètres de la requête
     * @throws InvalidRequestException si {@code params} est null ou ne contient pas la clé {@code name}
     */
    private void validPromptsParams(final Object id, final Map<String, Object> params) throws InvalidRequestException {
        if (params == null || !params.containsKey("name")) {
            throw new InvalidRequestException(id, "params.name is required for prompts/get");
        }
    }
}
