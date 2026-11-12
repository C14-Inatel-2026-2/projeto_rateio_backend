package rateio.despesa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rateio.despesa.domain.StatusDespesa;
import rateio.despesa.entity.ParcelaEntidade;

import java.util.List;
import java.util.UUID;

public interface ParcelaRepository extends JpaRepository<ParcelaEntidade, UUID> {

    List<ParcelaEntidade> findByDespesaId(UUID despesaId);

    List<ParcelaEntidade> findByDespesaGrupoIdAndDespesaStatus(UUID grupoId, StatusDespesa status);
}
