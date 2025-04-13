package io.neif.coworkingplacehub.repositories;

import io.neif.coworkingplacehub.models.Client;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClientRepository extends MongoRepository<Client, UUID> {

    Optional<Client> findByUsername(String username);

    Optional<Client> findByEmail(String email);

}
