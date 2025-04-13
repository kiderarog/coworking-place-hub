package io.neif.coworkingplacehub.repositories;

import io.neif.coworkingplacehub.models.Coworking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CoworkingRepository extends JpaRepository<Coworking, UUID> {
}
