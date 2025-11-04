package com.wgt.imt.architecture.bdd.clients.repositories;

import com.wgt.imt.architecture.bdd.clients.repositories.entities.ClientEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Repository Spring Data MongoDB pour les entités Client.
 * Fournit les opérations CRUD standard héritées de MongoRepository.
 *
 * @author Emmanuel WAGUET
 * @version 1.0
 */
public interface ClientRepository extends MongoRepository<ClientEntity, String> {
}
