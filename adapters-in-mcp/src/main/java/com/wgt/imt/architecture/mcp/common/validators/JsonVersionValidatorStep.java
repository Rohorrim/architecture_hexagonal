package com.wgt.imt.architecture.mcp.common.validators;

import com.wgt.imt.architecture.business.common.exception.ImtException;
import com.wgt.imt.architecture.business.common.validators.AbstractValidatorStep;
import com.wgt.imt.architecture.mcp.common.model.exception.InvalidRequestException;
import com.wgt.imt.architecture.mcp.common.model.input.JsonRpcRequest;
import lombok.AllArgsConstructor;

/**
 * Étape de validation JSON-RPC 2.0 : vérifie que le champ {@code jsonrpc} vaut {@code "2.0"}.
 *
 * <h2>Patron : Chain of Responsibility</h2>
 * <p>Cette classe s'inscrit dans la chaîne de validation initialisée dans
 * {@link com.wgt.imt.architecture.mcp.JsonRpcController#dispatch}.
 * Elle est la <strong>première étape</strong>, chaînée avec {@link MethodValidatorStep} :</p>
 * <pre>
 * JsonVersionValidatorStep → MethodValidatorStep
 * </pre>
 *
 * <h2>Règle vérifiée</h2>
 * <p>La spec JSON-RPC 2.0 impose que toute requête contienne exactement
 * {@code "jsonrpc": "2.0"}. Si ce champ est absent ou a une valeur différente,
 * une {@link InvalidRequestException} est levée avec le code {@code -32600}.</p>
 *
 * @see MethodValidatorStep
 * @see InvalidRequestException
 * @see <a href="https://www.jsonrpc.org/specification#request_object">JSON-RPC 2.0 Request Object</a>
 * @since 0.0.1
 */
@AllArgsConstructor
public class JsonVersionValidatorStep extends AbstractValidatorStep<JsonRpcRequest> {

    /**
     * Vérifie que le champ {@code jsonrpc} de la requête vaut exactement {@code "2.0"}.
     *
     * @param request La requête JSON-RPC à valider
     * @throws InvalidRequestException si {@code jsonrpc} est absent ou différent de {@code "2.0"}
     */
    @Override
    public void check(final JsonRpcRequest request) throws ImtException {
        if (!"2.0".equals(request.getJsonrpc())) {
            throw new InvalidRequestException(request.getId(), "jsonrpc field must be \"2.0\"");
        }
    }
}
