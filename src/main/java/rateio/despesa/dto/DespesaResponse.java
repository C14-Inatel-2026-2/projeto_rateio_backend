package rateio.despesa.dto;

import rateio.despesa.domain.Despesa;
import rateio.despesa.domain.StatusDespesa;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record DespesaResponse(
        UUID id,
        String descricao,
        String valorTotal,
        UUID pagadorId,
        Despesa.EstrategiaDivisao estrategia,
        StatusDespesa status,
        Instant data,
        List<ParcelaResponse> parcelas
) {

    public static DespesaResponse de(DespesaDetalhe detalhe) {
        return new DespesaResponse(
                detalhe.id(),
                detalhe.descricao(),
                detalhe.valorTotal().toPlainString(),
                detalhe.pagadorId(),
                detalhe.estrategia(),
                detalhe.status(),
                detalhe.data(),
                detalhe.parcelas().stream().map(ParcelaResponse::de).toList());
    }
}
