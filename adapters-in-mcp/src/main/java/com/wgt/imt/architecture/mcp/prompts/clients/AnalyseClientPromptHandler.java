package com.wgt.imt.architecture.mcp.prompts.clients;

import com.wgt.imt.architecture.mcp.common.model.output.prompts.PromptArgument;
import com.wgt.imt.architecture.mcp.common.model.output.prompts.PromptGetOutput;
import com.wgt.imt.architecture.mcp.common.model.output.prompts.PromptMessage;
import com.wgt.imt.architecture.mcp.prompts.PromptHandler;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Prompt MCP guidant le LLM à travers une <strong>analyse financière complète d'un client</strong>.
 *
 * <h2>Cas d'usage pédagogique</h2>
 * <p>Ce prompt illustre la différence fondamentale entre un Tool et un Prompt MCP :</p>
 * <ul>
 *   <li>Un <strong>Tool</strong> réalise une action atomique : {@code GetOneClient} retourne un client.</li>
 *   <li>Un <strong>Prompt</strong> orchestre un <em>workflow complet</em> : il indique au LLM
 *       <em>comment enchaîner plusieurs Tools</em> pour atteindre un objectif de haut niveau.</li>
 * </ul>
 *
 * <h2>Workflow orchestré</h2>
 * <p>Le prompt guide le LLM à travers les étapes suivantes :</p>
 * <ol>
 *   <li>Appel de {@code GetOneClient} pour récupérer l'identité du client</li>
 *   <li>Appel de {@code GetAllComptes} pour récupérer ses comptes</li>
 *   <li>Analyse des soldes et détection des comptes en débit (solde &lt; 0)</li>
 *   <li>Production d'un rapport structuré avec recommandations</li>
 * </ol>
 *
 * <h2>Argument requis</h2>
 * <ul>
 *   <li>{@code clientIdentifier} — UUID du client à analyser</li>
 * </ul>
 *
 * @see PromptHandler
 * @since 0.0.1
 */
@Service
public class AnalyseClientPromptHandler implements PromptHandler {

    private static final String PROMPT_NAME = "analyse-client";
    private static final String PROMPT_DESCRIPTION = "Génère un workflow guidé pour analyser la situation financière complète d'un client : récupération des données, analyse des comptes, détection des soldes négatifs et recommandations";
    private static final String ARG_CLIENT_ID = "clientIdentifier";

    @Override
    public String getName() {
        return PROMPT_NAME;
    }

    @Override
    public String getPromptDescription() {
        return PROMPT_DESCRIPTION;
    }

    @Override
    public List<PromptArgument> getArguments() {
        return List.of(
                PromptArgument.builder()
                        .name(ARG_CLIENT_ID)
                        .description("Identifiant UUID du client dont on veut analyser la situation financière")
                        .required(true)
                        .build()
        );
    }

    /**
     * Génère le prompt rendu avec l'identifiant du client injecté.
     *
     * <p>Le message produit guide le LLM étape par étape, en lui indiquant précisément
     * quels Tools appeler et dans quel ordre. Le LLM reçoit ce texte comme instruction
     * et l'exécute de manière autonome.</p>
     *
     * @param arguments Doit contenir la clé {@code "clientIdentifier"} avec un UUID valide
     * @return Le prompt rendu sous forme de {@link PromptGetOutput}
     */
    @Override
    public PromptGetOutput apply(final Map<String, String> arguments) {
        final String clientId = Optional.ofNullable(arguments)
                .map(args -> args.get(ARG_CLIENT_ID))
                .orElse("<clientIdentifier non fourni>");

        final String promptText = """
                Tu es un conseiller bancaire expert. Ta mission est d'analyser la situation financière du client identifié par l'UUID : %s
                
                Suis les étapes suivantes dans l'ordre :
                
                Étape 1 — Récupération du profil client
                Appelle l'outil GetOneClient avec l'argument : identifier = %s
                Note l'identité du client (nom, prénom, genre).
                
                Étape 2 — Récupération des comptes
                Appelle l'outil GetAllComptes avec l'argument : clientIdentifier = %s
                Liste tous les comptes disponibles avec leur type et leur solde.
                
                Étape 3 — Analyse financière
                Pour chaque compte :
                - Identifie le type (COMPTE_COURANT, LIVRET_A, LDDS)
                - Note le solde
                - Signale les comptes avec un solde strictement négatif (risque de frais d'agios)
                
                Étape 4 — Rapport structuré
                Produis un rapport avec :
                - Identité complète du client
                - Nombre total de comptes et répartition par type
                - Solde total consolidé (somme de tous les soldes)
                - Liste des comptes en débit (solde < 0) avec le montant du dépassement
                - Recommandations : régularisation des comptes en débit, diversification des épargnes si aucun LIVRET_A ou LDDS
                """.formatted(clientId, clientId, clientId);

        return PromptGetOutput.builder()
                .name(PROMPT_NAME)
                .messages(List.of(
                        PromptMessage.builder()
                                .role("user")
                                .content(promptText)
                                .build()
                ))
                .build();
    }
}

