package com.wgt.imt.architecture.mcp.tools.clients;

import com.wgt.imt.architecture.business.clients.ClientsService;
import com.wgt.imt.architecture.business.common.exception.ImtException;
import com.wgt.imt.architecture.business.common.validators.ConstraintValidatorStep;
import com.wgt.imt.architecture.mcp.tools.ToolHandler;
import com.wgt.imt.architecture.mcp.tools.clients.input.GetOneToolArguments;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Implémentation concrète du {@link ToolHandler} pour l'outil MCP <strong>"GetOneClient"</strong>.
 *
 * <h2>Rôle de cette classe dans l'architecture MCP</h2>
 * <p>Contrairement à {@link GetAllClientToolHandler}, cet outil illustre le cas d'un
 * {@link ToolHandler} <strong>avec argument</strong> : il requiert l'identifiant UUID du client.
 * Le LLM doit construire cet argument en se basant sur l'{@code inputSchema} retourné par
 * {@code tools/list} — qui est généré automatiquement depuis {@link GetOneToolArguments}
 * par {@link com.wgt.imt.architecture.mcp.common.utils.JsonSchemaGenerator}.</p>
 *
 * <h2>Flux complet d'un appel</h2>
 * <pre>
 * 1. LLM appelle POST /tools/call avec :
 *    { "name": "GetOneClient", "arguments": "{\"identifier\": \"550e8400-...\"}" }
 *
 * 2. JsonRpcController route vers ce handler (name = "GetOneClient")
 *
 * 3. ToolHandler.handle(String) désérialise le JSON en GetOneToolParameters
 *    → { identifier: UUID("550e8400-...") }
 *
 * 4. handle(GetOneToolParameters, context) appelle clientsService.getOne(identifier)
 *
 * 5. Le résultat est enveloppé dans ActionOutput et retourné au LLM
 * </pre>
 *
 * @see ToolHandler
 * @see GetOneToolArguments
 * @see ClientsService
 * @since 0.0.1
 */
@Service
@AllArgsConstructor
public class GetOneClientToolHandler implements ToolHandler<GetOneToolArguments> {

    /**
     * Nom de l'outil tel qu'exposé dans {@code tools/list} et attendu dans {@code tools/call}.
     */
    private static final String TOOL_NAME = "GetOneClient";

    /**
     * Description en langage naturel, lue et interprétée par le LLM.
     */
    private static final String TOOL_DESCRIPTION = "Permet de récupérer les informations d'un client en particulier";

    /**
     * Service métier du domaine — injecté par Spring via le constructeur ({@code @AllArgsConstructor}).
     */
    private final ClientsService clientsService;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return GetOneClientToolHandler.TOOL_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getToolDescription() {
        return GetOneClientToolHandler.TOOL_DESCRIPTION;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<GetOneToolArguments> getInputType() {
        return GetOneToolArguments.class;
    }

    /**
     * Délègue au service métier pour récupérer un client par son identifiant.
     *
     * @param arguments Les arguments typés contenant l'UUID du client à récupérer
     * @return Le client correspondant à l'identifiant, retourné par {@link ClientsService#getOne}
     */
    @Override
    public Object apply(final GetOneToolArguments arguments) throws ImtException {
        new ConstraintValidatorStep<>().validate(arguments).throwIfInvalid();
        return this.clientsService.getOne(arguments.getIdentifier());
    }
}
