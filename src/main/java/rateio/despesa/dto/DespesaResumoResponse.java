package rateio.despesa.dto;

import rateio.despesa.domain.StatusDespesa;

import java.time.Instant;
import java.util.UUID;

public record DespesaResumoResponse(
        UUID id,
        String descricao,
        String valorTotal,
        UUID pagadorId,
        StatusDespesa status,
        Instant data
) {

    public static DespesaResumoResponse de(DespesaResumo resumo) {
        return new DespesaResumoResponse(
                resumo.id(),
                resumo.descricao(),
                resumo.valorTotal().toPlainString(),
                resumo.pagadorId(),
                resumo.status(),
                resumo.data());
    }
}
