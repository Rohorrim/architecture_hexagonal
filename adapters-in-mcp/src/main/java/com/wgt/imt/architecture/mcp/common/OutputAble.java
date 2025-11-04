package com.wgt.imt.architecture.mcp.common;

/**
 * Interface marquant un handler MCP capable de produire son propre descripteur de sortie.
 *
 * <h2>Rôle dans l'architecture</h2>
 * <p>Les trois types de handlers MCP ({@link com.wgt.imt.architecture.mcp.tools.ToolHandler},
 * {@link com.wgt.imt.architecture.mcp.resources.ResourceHandler},
 * {@link com.wgt.imt.architecture.mcp.prompts.PromptHandler}) implémentent cette interface
 * pour permettre à {@link com.wgt.imt.architecture.mcp.JsonRpcController} de les traiter
 * de façon uniforme lors des méthodes de type liste
 * ({@code tools/list}, {@code resources/list}, {@code prompts/list}).</p>
 *
 * <h2>Patron : Polymorphisme par interface</h2>
 * <p>Sans cette interface, le contrôleur aurait besoin de trois méthodes distinctes pour
 * sérialiser les listes de handlers. Grâce à {@code OutputAble<T>}, une seule méthode
 * générique suffit :</p>
 * <pre>
 * private static &lt;I extends OutputAble&lt;?&gt;&gt; JsonRpcResponse handleResponseList(...)
 * </pre>
 *
 * @param <T> Le type du descripteur de sortie produit (ex : {@code ToolOutput}, {@code ResourceOutput})
 * @see com.wgt.imt.architecture.mcp.JsonRpcController
 * @since 0.0.1
 */
public interface OutputAble<T> {

    /**
     * Produit le descripteur de sortie de ce handler, tel qu'il sera retourné dans la réponse JSON-RPC.
     *
     * @return Le descripteur de sortie (nom, description, schéma, URI, etc.)
     */
    T toOutput();
}
