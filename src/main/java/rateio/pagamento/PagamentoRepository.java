package rateio.pagamento;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PagamentoRepository extends JpaRepository<PagamentoEntidade, UUID> {

    List<PagamentoEntidade> findByGrupoId(UUID grupoId);

    Optional<PagamentoEntidade> findByIdAndGrupoId(UUID id, UUID grupoId);

    List<PagamentoEntidade> findByGrupoIdAndStatus(UUID grupoId, StatusPagamento status);
}
