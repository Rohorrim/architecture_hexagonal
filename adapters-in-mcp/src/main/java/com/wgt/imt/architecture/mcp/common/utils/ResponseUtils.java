package com.wgt.imt.architecture.mcp.common.utils;

import com.wgt.imt.architecture.mcp.common.OutputAble;
import com.wgt.imt.architecture.mcp.common.model.input.JsonRpcRequest;
import com.wgt.imt.architecture.mcp.common.model.output.JsonRpcResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Utilitaires de construction des réponses JSON-RPC 2.0.
 *
 * <p>Regroupe les helpers statiques partagés entre les différents composants
 * du contrôleur MCP pour la sérialisation et l'assemblage des réponses.</p>
 *
 * @since 0.0.1
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ResponseUtils {

    /**
     * Sérialise une collection de handlers en réponse JSON-RPC de succès
     * pour les méthodes de type liste ({@code tools/list}, {@code resources/list}, {@code prompts/list}).
     *
     * <p>Chaque élément de la collection implémente {@link OutputAble} et produit
     * son propre descripteur de sortie via {@link OutputAble#toOutput()}.</p>
     *
     * @param <I>     Type des handlers, doit implémenter {@link OutputAble}
     * @param request La requête JSON-RPC d'origine (pour récupérer l'identifiant de corrélation)
     * @param output  La collection de handlers à sérialiser
     * @return Une réponse JSON-RPC 2.0 de succès contenant l'ensemble des descripteurs
     */
    public static <I extends OutputAble<?>> JsonRpcResponse handleResponseList(final JsonRpcRequest request, final Collection<I> output) {
        return JsonRpcResponse.success(request.getId(), Optional.ofNullable(output).stream()
                .flatMap(Collection::stream)
                .map(OutputAble::toOutput)
                .collect(Collectors.toSet()));
    }
}

