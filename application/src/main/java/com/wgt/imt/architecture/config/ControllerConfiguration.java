package com.wgt.imt.architecture.config;

import com.wgt.imt.architecture.business.clients.ClientStorageProvider;
import com.wgt.imt.architecture.business.clients.ClientsServiceValidator;
import com.wgt.imt.architecture.business.comptes.ComptesServiceValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration Spring pour instancier les services métier du domain.
 * C'est ici que l'on assemble les composants de l'architecture hexagonale :
 * - Le module domain reste pur (sans Spring)
 * - Cette configuration injecte les implémentations des ports (interfaces)
 * - Les adapters-out implémentent les ports
 * - Les adapters-in utilisent les services métier
 *
 * @author Emmanuel WAGUET
 * @version 1.0
 */
@Configuration
public class ControllerConfiguration {

    /**
     * Crée le bean du service de gestion des clients avec validation.
     * Injecte l'implémentation du ClientStorageProvider (fournie par adapters-out-bdd).
     *
     * @param clientStorageProvider implémentation du port de stockage des clients
     * @return le service métier de gestion des clients
     */
    @Bean
    public ClientsServiceValidator clientsServiceValidator(final ClientStorageProvider clientStorageProvider) {
        return new ClientsServiceValidator(clientStorageProvider);
    }

    /**
     * Crée le bean du service de gestion des comptes avec validation.
     * Injecte le port de stockage nécessaire.
     *
     * @param clientStorageProvider   implémentation du port de stockage des clients
     * @return le service métier de gestion des comptes
     */
    @Bean
    public ComptesServiceValidator comptesServiceValidator(final ClientStorageProvider clientStorageProvider) {
        return new ComptesServiceValidator(clientStorageProvider);
    }
}
