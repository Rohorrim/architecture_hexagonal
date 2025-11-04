package com.wgt.imt.architecture.mcp.tools.comptes;

import com.wgt.imt.architecture.business.common.exception.ImtException;
import com.wgt.imt.architecture.business.common.validators.ConstraintValidatorStep;
import com.wgt.imt.architecture.business.comptes.ComptesService;
import com.wgt.imt.architecture.mcp.tools.ToolHandler;
import com.wgt.imt.architecture.mcp.tools.comptes.input.GetAllComptesToolArguments;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Implémentation concrète du {@link ToolHandler} pour l'outil MCP <strong>"GetAllComptes"</strong>.
 *
 * <h2>Rôle de cette classe dans l'architecture MCP</h2>
 * <p>Cette classe est <strong>l'adaptateur</strong> entre le protocole MCP et le service métier
 * {@link ComptesService}. Elle matérialise le pattern
 * <em>Hexagonal Architecture (Ports & Adapters)</em> :</p>
 * <ul>
 *   <li>Le <strong>port</strong> est l'interface {@link ToolHandler} — contrat défini par la couche MCP</li>
 *   <li>L'<strong>adaptateur</strong> est cette classe — implémentation concrète qui appelle le domaine</li>
 *   <li>Le <strong>domaine</strong> est {@link ComptesService} — logique métier pure, ignorante de MCP</li>
 * </ul>
 *
 * <h2>Différence avec GetAllClients</h2>
 * <p>Contrairement à {@link com.wgt.imt.architecture.mcp.tools.clients.GetAllClientToolHandler}
 * qui n'a aucun argument, cet outil requiert un {@code clientIdentifier} obligatoire.
 * Les comptes sont systématiquement filtrés par client dans ce modèle bancaire.</p>
 *
 * @see ToolHandler
 * @see GetAllComptesToolArguments
 * @see ComptesService
 * @since 0.0.1
 */
@Service
@AllArgsConstructor
public class GetAllComptesToolHandler implements ToolHandler<GetAllComptesToolArguments> {

    /**
     * Nom de l'outil tel qu'exposé dans {@code tools/list} et attendu dans {@code tools/call}.
     */
    private static final String TOOL_NAME = "GetAllComptes";

    /**
     * Description en langage naturel, lue et interprétée par le LLM.
     */
    private static final String TOOL_DESCRIPTION = "Permet de récupérer tous les comptes d'un client";

    /**
     * Service métier du domaine — injecté par Spring via le constructeur ({@code @AllArgsConstructor}).
     */
    private final ComptesService comptesService;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return GetAllComptesToolHandler.TOOL_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getToolDescription() {
        return GetAllComptesToolHandler.TOOL_DESCRIPTION;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<GetAllComptesToolArguments> getInputType() {
        return GetAllComptesToolArguments.class;
    }

    /**
     * Délègue au service métier pour récupérer tous les comptes d'un client.
     *
     * @param arguments Les arguments contenant l'UUID du client propriétaire des comptes
     * @return La collection des comptes du client, retournée par
     * {@link ComptesService#getAllFilteredByClient(java.util.UUID)}
     */
    @Override
    public Object apply(final GetAllComptesToolArguments arguments) throws ImtException {
        new ConstraintValidatorStep<>().validate(arguments).throwIfInvalid();
        return this.comptesService.getAllFilteredByClient(arguments.getClientIdentifier());
    }
}

