package rateio.despesa.dto;

import rateio.despesa.domain.StatusDespesa;
import rateio.despesa.entity.DespesaEntidade;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record DespesaResumo(
        UUID id,
        String descricao,
        BigDecimal valorTotal,
        UUID pagadorId,
        StatusDespesa status,
        Instant data
) {

    public static DespesaResumo de(DespesaEntidade despesa) {
        return new DespesaResumo(
                despesa.getId(),
                despesa.getDescricao(),
                despesa.getValorTotal(),
                despesa.getPagador().getId(),
                despesa.getStatus(),
                despesa.getCriadaEm());
    }
}
