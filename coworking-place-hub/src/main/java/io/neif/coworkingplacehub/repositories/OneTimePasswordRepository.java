package io.neif.coworkingplacehub.repositories;

import io.neif.coworkingplacehub.models.OneTimePassword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OneTimePasswordRepository extends JpaRepository<OneTimePassword, String> {

    Optional<OneTimePassword> findByOtpCode(Integer otpCode);

    Optional<OneTimePassword> findByEmail(String email);

    void deleteByEmail(String email);

    void deleteAllByExpAtBefore(LocalDateTime expAtBefore);
}

