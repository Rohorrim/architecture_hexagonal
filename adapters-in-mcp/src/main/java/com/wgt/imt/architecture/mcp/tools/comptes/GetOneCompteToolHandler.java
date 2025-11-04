package com.wgt.imt.architecture.mcp.tools.comptes;

import com.wgt.imt.architecture.business.common.exception.ImtException;
import com.wgt.imt.architecture.business.common.validators.ConstraintValidatorStep;
import com.wgt.imt.architecture.business.comptes.ComptesService;
import com.wgt.imt.architecture.mcp.tools.ToolHandler;
import com.wgt.imt.architecture.mcp.tools.comptes.input.GetOneCompteToolArguments;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Implémentation concrète du {@link ToolHandler} pour l'outil MCP <strong>"GetOneCompte"</strong>.
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
 * <h2>Outil à deux arguments</h2>
 * <p>Cet outil illustre un cas plus complexe que {@link GetOneCompteToolArguments} :
 * deux UUID sont nécessaires car un compte ne peut pas être identifié globalement — il
 * est toujours rattaché à un client. Le LLM doit avoir préalablement connaissance du
 * {@code clientIdentifier} (par exemple via un appel à {@code GetAllClients} ou
 * {@code GetOneClient}) avant de pouvoir appeler cet outil.</p>
 *
 * <h2>Flux complet d'un appel</h2>
 * <pre>
 * 1. LLM appelle POST /tools/call avec :
 *    {
 *      "name": "GetOneCompte",
 *      "arguments": "{
 *        \"clientIdentifier\": \"550e8400-...\",
 *        \"identifier\": \"660f9511-...\"
 *      }"
 *    }
 *
 * 2. JsonRpcController route vers ce handler (name = "GetOneCompte")
 *
 * 3. ToolHandler.handle(String) désérialise le JSON en GetOneCompteToolArguments
 *    → { clientIdentifier: UUID("550e8400-..."), identifier: UUID("660f9511-...") }
 *
 * 4. handle(GetOneCompteToolArguments, context) appelle
 *    comptesService.getOneFilteredByClient(clientIdentifier, identifier)
 *
 * 5. Le résultat (Optional<Compte>) est enveloppé dans ActionOutput et retourné au LLM
 * </pre>
 *
 * @see ToolHandler
 * @see GetOneCompteToolArguments
 * @see ComptesService
 * @since 0.0.1
 */
@Service
@AllArgsConstructor
public class GetOneCompteToolHandler implements ToolHandler<GetOneCompteToolArguments> {

    /**
     * Nom de l'outil tel qu'exposé dans {@code tools/list} et attendu dans {@code tools/call}.
     */
    private static final String TOOL_NAME = "GetOneCompte";

    /**
     * Description en langage naturel, lue et interprétée par le LLM.
     */
    private static final String TOOL_DESCRIPTION = "Permet de récupérer les informations d'un compte en particulier pour un client donné";

    /**
     * Service métier du domaine — injecté par Spring via le constructeur ({@code @AllArgsConstructor}).
     */
    private final ComptesService comptesService;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return GetOneCompteToolHandler.TOOL_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getToolDescription() {
        return GetOneCompteToolHandler.TOOL_DESCRIPTION;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<GetOneCompteToolArguments> getInputType() {
        return GetOneCompteToolArguments.class;
    }

    /**
     * Délègue au service métier pour récupérer un compte spécifique d'un client.
     *
     * @param arguments Les arguments contenant l'UUID du client et l'UUID du compte
     * @return Un {@link java.util.Optional} contenant le compte s'il existe, retourné par
     * {@link ComptesService#getOneFilteredByClient(java.util.UUID, java.util.UUID)}
     */
    @Override
    public Object apply(final GetOneCompteToolArguments arguments) throws ImtException {
        new ConstraintValidatorStep<>().validate(arguments).throwIfInvalid();
        return this.comptesService.getOneFilteredByClient(
                arguments.getClientIdentifier(),
                arguments.getIdentifier()
        );
    }
}

