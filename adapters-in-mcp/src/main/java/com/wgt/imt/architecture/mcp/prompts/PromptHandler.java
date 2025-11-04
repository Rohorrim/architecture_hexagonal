package com.wgt.imt.architecture.mcp.prompts;

import com.wgt.imt.architecture.mcp.common.OutputAble;
import com.wgt.imt.architecture.mcp.common.model.output.prompts.PromptArgument;
import com.wgt.imt.architecture.mcp.common.model.output.prompts.PromptGetOutput;
import com.wgt.imt.architecture.mcp.common.model.output.prompts.PromptOutput;

import java.util.List;
import java.util.Map;

/**
 * Interface définissant le contrat d'un <strong>prompt MCP</strong> (Model Context Protocol Prompt).
 *
 * <h2>Contexte MCP — Prompts</h2>
 * <p>Les prompts sont la troisième primitive de MCP. Ils permettent d'exposer des
 * <strong>templates de conversation réutilisables</strong> qui guident le LLM à travers
 * un workflow complexe impliquant potentiellement plusieurs Tool calls.</p>
 *
 * <h2>Différence avec les Tools</h2>
 * <pre>
 * Tool  : action atomique  → "Donne-moi le client X"
 * Prompt: workflow guidé   → "Analyse la situation financière du client X :
 *                              1. Appelle GetOneClient(X)
 *                              2. Appelle GetAllComptes(X)
 *                              3. Analyse les soldes
 *                              4. Produis un rapport structuré"
 * </pre>
 *
 * <h2>Patron de conception</h2>
 * <p>Même patron que {@link com.wgt.imt.architecture.mcp.tools.ToolHandler} :
 * chaque implémentation est un bean Spring {@code @Service} découvert automatiquement.</p>
 *
 * @see com.wgt.imt.architecture.mcp.JsonRpcController
 * @see <a href="https://spec.modelcontextprotocol.io/specification/server/prompts/">MCP Prompts Specification</a>
 */
public interface PromptHandler extends OutputAble<PromptOutput> {

    /**
     * Construit le {@link PromptOutput} décrivant ce prompt (pour {@code prompts/list}).
     *
     * <p>Méthode par défaut pour éviter la duplication dans chaque implémentation.</p>
     *
     * @return Le descripteur du prompt
     */
    default PromptOutput toOutput() {
        return PromptOutput.builder()
                .name(this.getName())
                .description(this.getPromptDescription())
                .arguments(this.getArguments())
                .build();
    }

    /**
     * Retourne le nom unique identifiant ce prompt.
     *
     * <p>Ce nom est utilisé par le LLM pour appeler {@code prompts/get}.</p>
     *
     * @return Le nom du prompt (ex : {@code "analyse-client"})
     */
    String getName();

    /**
     * Retourne la description du workflow réalisé par ce prompt.
     *
     * <p>Lue par le LLM pour décider si ce prompt est adapté à sa tâche.</p>
     *
     * @return La description du prompt
     */
    String getPromptDescription();

    /**
     * Retourne la liste des arguments attendus pour paramétrer ce prompt.
     *
     * <p>Ces arguments sont exposés dans {@code prompts/list} pour informer le LLM
     * de ce qu'il doit fournir lors d'un appel {@code prompts/get}.</p>
     *
     * @return La liste des {@link PromptArgument}
     */
    List<PromptArgument> getArguments();

    /**
     * Génère le prompt rendu avec les arguments fournis (pour {@code prompts/get}).
     *
     * @param arguments Map des arguments (clé = nom de l'argument, valeur = valeur fournie par le LLM)
     * @return Le prompt rendu sous forme de {@link PromptGetOutput} contenant les messages
     */
    PromptGetOutput apply(final Map<String, String> arguments);
}

