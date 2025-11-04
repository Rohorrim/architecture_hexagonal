package com.wgt.imt.architecture;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Classe principale de l'application Architecture.
 * Point d'entrée de l'application Spring Boot.
 *
 * @author Emmanuel WAGUET
 * @version 1.0
 */
@SpringBootApplication
@EnableAsync
public class ArchitectureApplication {

    /**
     * Méthode principale qui lance l'application Spring Boot.
     *
     * @param args arguments de ligne de commande passés à l'application
     */
    public static void main(String[] args) {
        SpringApplication.run(ArchitectureApplication.class, args);
    }

}
