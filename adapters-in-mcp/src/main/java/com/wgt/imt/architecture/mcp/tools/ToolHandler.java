package com.wgt.imt.architecture.mcp.tools;

import com.wgt.imt.architecture.business.common.exception.ImtException;
import com.wgt.imt.architecture.mcp.common.OutputAble;
import com.wgt.imt.architecture.mcp.common.model.output.tools.ToolOutput;
import com.wgt.imt.architecture.mcp.common.utils.JsonSchemaGenerator;
import com.wgt.imt.architecture.mcp.common.utils.JsonUtils;

import java.util.Map;
import java.util.Optional;

/**
 * Interface générique définissant le contrat d'un <strong>outil MCP</strong> (Model Context Protocol Tool).
 *
 * <h2>Contexte MCP</h2>
 * <p>Dans la spécification MCP, un <em>tool</em> est une <strong>capacité d'action</strong> exposée par un serveur
 * MCP à destination d'un agent conversationnel (LLM). Concrètement, un outil peut :</p>
 * <ul>
 *   <li>Lire des données (ex : récupérer un client en base)</li>
 *   <li>Déclencher une action métier (ex : créer un compte)</li>
 *   <li>Appeler un service externe</li>
 * </ul>
 *
 * <h2>Patron de conception : Interface générique + Strategy</h2>
 * <p>Cette interface utilise un <strong>paramètre de type générique {@code <I>}</strong> qui représente
 * la classe des arguments d'entrée spécifiques à chaque outil. Ce patron permet :</p>
 * <ol>
 *   <li><strong>Typage fort</strong> — chaque outil déclare explicitement le type de ses arguments</li>
 *   <li><strong>Extensibilité</strong> — ajouter un nouvel outil = créer une nouvelle classe qui implémente cette interface</li>
 *   <li><strong>Découverte automatique</strong> — Spring collecte toutes les implémentations via l'injection de collection</li>
 * </ol>
 *
 * <h2>Méthode par défaut (default method)</h2>
 * <p>La méthode {@link #apply(Map)} est une <strong>méthode par défaut</strong> (Java 8+)
 * qui agit comme un <em>template method</em> : elle gère la conversion de la {@code Map} JSON
 * en objet typé {@code <I>}, puis délègue à {@link #apply(Object)}.
 * Les implémentations n'ont qu'à se concentrer sur la logique métier.</p>
 *
 * @param <I> Le type Java des arguments d'entrée de l'outil (ex : {@code GetOneClientArguments})
 * @see <a href="https://spec.modelcontextprotocol.io/specification/server/tools/">Spécification MCP — Tools</a>
 * @see com.wgt.imt.architecture.mcp.JsonRpcController
 */
public interface ToolHandler<I> extends OutputAble<ToolOutput> {

    /**
     * Point d'entrée principal appelé par le contrôleur MCP.
     *
     * <p>Cette <strong>méthode par défaut</strong> effectue deux opérations :</p>
     * <ol>
     *   <li><strong>Conversion</strong> — sérialise la {@code Map<String, Object>} en JSON via
     *       {@link JsonUtils}, puis désérialise vers le type cible {@code <I>}.
     *       Ce double passage par JSON est le prix de la conformité à la spec MCP
     *       (qui mandate un objet JSON pour {@code arguments}, pas une chaîne).</li>
     *   <li><strong>Délégation</strong> — appelle {@link #apply(Object)} avec l'objet typé</li>
     * </ol>
     *
     * <p>Si {@code arguments} est {@code null} (outil sans paramètre), le {@link Optional} absorbe
     * le cas sans lever de NullPointerException.</p>
     *
     * @param arguments Map des arguments JSON désérialisés (peut être {@code null})
     * @return Le résultat de l'exécution de l'outil
     * @throws ImtException En cas d'erreur métier lors du traitement
     */
    default Object apply(final Map<String, Object> arguments) throws ImtException {
        return this.apply(Optional.ofNullable(arguments)
                .map(data -> JsonUtils.withDefaultConf().readFromMap(this.getInputType(), data))
                .orElse(null));
    }

    /**
     * Construit le {@link ToolOutput} décrivant cette tool (pour {@code tools/list}).
     *
     * <p>Méthode par défaut : évite la duplication dans chaque implémentation.</p>
     *
     * @return Le descripteur du tool
     */
    default ToolOutput toOutput() {
        return ToolOutput.builder()
                .name(this.getName())
                .description(this.getToolDescription())
                .inputSchema(JsonSchemaGenerator.generateSchema(this.getInputType()))
                .build();
    }

    /**
     * Retourne le <strong>nom unique</strong> de l'outil.
     *
     * <p>Ce nom correspond au champ {@code name} dans la réponse {@code tools/list} de MCP.
     * Il est également utilisé pour router les appels : le contrôleur compare ce nom
     * avec le champ {@code name} de la requête {@code tools/call} pour identifier le bon handler.</p>
     *
     * <p><strong>Convention :</strong> utiliser PascalCase (ex : {@code "GetOneClient"}).</p>
     *
     * @return Le nom de l'outil (ex : {@code "GetAllClients"})
     */
    String getName();

    /**
     * Retourne la <strong>description lisible</strong> de l'outil.
     *
     * <p>Cette description est exposée via {@code tools/list} et consommée par le LLM pour
     * <em>comprendre ce que fait l'outil</em> et décider s'il doit l'appeler.
     * Une bonne description est donc cruciale pour la qualité des réponses de l'agent.</p>
     *
     * @return Une description claire et concise de l'action réalisée par l'outil
     */
    String getToolDescription();

    /**
     * Retourne la <strong>classe Java des arguments d'entrée</strong> de l'outil.
     *
     * <p>Cette méthode a deux usages :</p>
     * <ol>
     *   <li><strong>Conversion</strong> — utilisée par la méthode par défaut
     *       {@link #apply(Map)} pour convertir la Map JSON en objet typé via Jackson</li>
     *   <li><strong>Génération du schéma JSON</strong> — le contrôleur introspecte cette classe
     *       via {@link com.wgt.imt.architecture.mcp.common.utils.JsonSchemaGenerator}
     *       pour produire l'{@code inputSchema} exposé dans {@code tools/list}</li>
     * </ol>
     *
     * @return La classe {@code Class<I>} des arguments d'entrée
     */
    Class<I> getInputType();

    /**
     * Implémente la <strong>logique métier</strong> de l'outil.
     *
     * <p>C'est la seule méthode que les classes concrètes <em>doivent obligatoirement</em> implémenter.
     * À ce stade, {@code arguments} est déjà désérialisé et typé — aucune manipulation JSON n'est nécessaire.</p>
     *
     * @param arguments Objet typé contenant les arguments de l'appel (peut être {@code null} si l'outil n'en a pas)
     * @return Le résultat de l'outil, sérialisé automatiquement en JSON dans la réponse HTTP
     * @throws ImtException En cas d'erreur métier (ex : ressource introuvable, règle métier violée)
     */
    Object apply(final I arguments) throws ImtException;
}
