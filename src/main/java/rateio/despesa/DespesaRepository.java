package rateio.despesa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import java.util.List;
import java.util.Optional;

public interface DespesaRepository extends JpaRepository<DespesaEntidade, UUID> {

    List<DespesaEntidade> findByGrupoId(UUID grupoId);

    Optional<DespesaEntidade> findByIdAndGrupoId(UUID id, UUID grupoId);

    boolean existsByGrupoId(UUID grupoId);
}
