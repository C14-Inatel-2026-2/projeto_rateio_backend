package rateio.grupo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface GrupoMembroRepository extends JpaRepository<GrupoMembroEntidade, UUID> {

    boolean existsByGrupoIdAndMembroId(UUID grupoId, UUID membroId);

    List<GrupoMembroEntidade> findByMembroId(UUID membroId);

    List<GrupoMembroEntidade> findByGrupoId(UUID grupoId);
}