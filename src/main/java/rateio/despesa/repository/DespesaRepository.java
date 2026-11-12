package rateio.despesa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rateio.despesa.entity.DespesaEntidade;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DespesaRepository extends JpaRepository<DespesaEntidade, UUID> {

    List<DespesaEntidade> findByGrupoId(UUID grupoId);

    Optional<DespesaEntidade> findByIdAndGrupoId(UUID id, UUID grupoId);

    boolean existsByGrupoId(UUID grupoId);
}
