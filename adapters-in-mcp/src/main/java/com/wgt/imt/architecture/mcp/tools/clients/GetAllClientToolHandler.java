package com.wgt.imt.architecture.mcp.tools.clients;

import com.wgt.imt.architecture.business.clients.ClientsService;
import com.wgt.imt.architecture.business.common.exception.ImtException;
import com.wgt.imt.architecture.business.common.validators.ConstraintValidatorStep;
import com.wgt.imt.architecture.mcp.common.model.input.tools.EmptyToolArguments;
import com.wgt.imt.architecture.mcp.tools.ToolHandler;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Implémentation concrète du {@link ToolHandler} pour l'outil MCP <strong>"GetAllClients"</strong>.
 *
 * <h2>Rôle de cette classe dans l'architecture MCP</h2>
 * <p>Cette classe est <strong>l'adaptateur</strong> entre le protocole MCP et le service métier
 * {@link ClientsService}. Elle matérialise le pattern
 * <em>Hexagonal Architecture (Ports & Adapters)</em> :</p>
 * <ul>
 *   <li>Le <strong>port</strong> est l'interface {@link ToolHandler} — contrat défini par la couche MCP</li>
 *   <li>L'<strong>adaptateur</strong> est cette classe — implémentation concrète qui appelle le domaine</li>
 *   <li>Le <strong>domaine</strong> est {@link ClientsService} — logique métier pure, ignorante de MCP</li>
 * </ul>
 *
 * <h2>Outil sans argument</h2>
 * <p>Cet outil est un exemple d'outil <em>sans argument</em>. Le type générique {@code <I>}
 * de {@link ToolHandler} est instancié avec {@link EmptyToolArguments}, une classe vide
 * dont le seul rôle est de satisfaire le contrat générique de l'interface.
 * Le handler l'ignore complètement dans la méthode {@link #apply}.</p>
 *
 * <h2>Découverte automatique par Spring</h2>
 * <p>L'annotation {@code @Service} fait de cette classe un bean Spring. Le contrôleur
 * {@link com.wgt.imt.architecture.mcp.JsonRpcController} reçoit automatiquement <em>tous</em> les
 * beans {@link ToolHandler} dans sa collection, sans configuration explicite.</p>
 *
 * <h2>Exemple d'appel via l'API MCP</h2>
 * <pre>
 * POST /api/imt/v1/mcp/tools/call
 * Content-Type: application/json
 *
 * {
 *   "name": "GetAllClients",
 *   "arguments": "{}"
 * }
 * </pre>
 *
 * @see ToolHandler
 * @see EmptyToolArguments
 * @see ClientsService
 * @since 0.0.1
 */
@Service
@AllArgsConstructor
public class GetAllClientToolHandler implements ToolHandler<EmptyToolArguments> {

    /**
     * Nom de l'outil tel qu'exposé dans {@code tools/list} et attendu dans {@code tools/call}.
     */
    private static final String TOOL_NAME = "GetAllClients";

    /**
     * Description en langage naturel, lue et interprétée par le LLM.
     */
    private static final String TOOL_DESCRIPTION = "Permet de récupérer tous les clients";

    /**
     * Service métier du domaine — injecté par Spring via le constructeur ({@code @AllArgsConstructor}).
     */
    private final ClientsService clientsService;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return GetAllClientToolHandler.TOOL_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getToolDescription() {
        return GetAllClientToolHandler.TOOL_DESCRIPTION;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<EmptyToolArguments> getInputType() {
        return EmptyToolArguments.class;
    }

    /**
     * Délègue au service métier pour récupérer tous les clients.
     *
     * <p>Le paramètre {@code arguments} est ignoré car cet outil ne nécessite aucun argument.
     * La classe {@link EmptyToolArguments} est utilisée uniquement pour satisfaire le
     * contrat générique de {@link ToolHandler}.</p>
     *
     * @param arguments Ignoré ({@link EmptyToolArguments} — outil sans argument)
     * @return La liste de tous les clients retournée par {@link ClientsService#getAll()}
     */
    @Override
    public Object apply(final EmptyToolArguments arguments) throws ImtException {
        new ConstraintValidatorStep<>().validate(arguments).throwIfInvalid();
        return this.clientsService.getAll();
    }
}
