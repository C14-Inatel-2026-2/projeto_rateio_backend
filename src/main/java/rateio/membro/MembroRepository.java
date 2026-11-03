package rateio.membro;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MembroRepository extends JpaRepository<Membro, UUID> {

    boolean existsByEmail(String email);

    Optional<Membro> findByEmail(String email);
}
