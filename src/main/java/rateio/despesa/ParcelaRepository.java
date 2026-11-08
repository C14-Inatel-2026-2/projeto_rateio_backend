package rateio.despesa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import java.util.List;

public interface ParcelaRepository extends JpaRepository<ParcelaEntidade, UUID> {

    List<ParcelaEntidade> findByDespesaId(UUID despesaId);

    List<ParcelaEntidade> findByDespesaGrupoIdAndDespesaStatus(UUID grupoId, StatusDespesa status);
}
