package io.neif.coworkingplacehub.repositories;

import io.neif.coworkingplacehub.models.Coworking;
import io.neif.coworkingplacehub.models.Spot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpotRepository extends JpaRepository<Spot, UUID> {

    boolean existsByCoworkingIdAndActiveBookingIsTrue(UUID coworkingId);

    Optional<Spot> findFirstByCoworkingIdAndActiveBookingIsFalse(UUID coworkingId);

    Optional<Spot> findByClientId(UUID clientId);

    List<Spot> findSpotsByCoworkingAndActiveBookingIsTrue(Coworking coworking);

}