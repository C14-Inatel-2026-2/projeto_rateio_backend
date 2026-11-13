package rateio.membro.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rateio.membro.entity.Membro;

import java.util.Optional;
import java.util.UUID;

public interface MembroRepository extends JpaRepository<Membro, UUID> {

    boolean existsByEmail(String email);

    Optional<Membro> findByEmail(String email);
}
