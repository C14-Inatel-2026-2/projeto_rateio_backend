package rateio.pagamento.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rateio.pagamento.domain.StatusPagamento;
import rateio.pagamento.entity.PagamentoEntidade;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PagamentoRepository extends JpaRepository<PagamentoEntidade, UUID> {

    List<PagamentoEntidade> findByGrupoId(UUID grupoId);

    Optional<PagamentoEntidade> findByIdAndGrupoId(UUID id, UUID grupoId);

    List<PagamentoEntidade> findByGrupoIdAndStatus(UUID grupoId, StatusPagamento status);
}
