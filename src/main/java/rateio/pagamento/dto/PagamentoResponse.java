package rateio.pagamento.dto;

import rateio.pagamento.domain.StatusPagamento;

import java.time.Instant;
import java.util.UUID;

public record PagamentoResponse(
        UUID id,
        UUID deId,
        UUID paraId,
        String valor,
        StatusPagamento status,
        Instant data
) {

    public static PagamentoResponse de(PagamentoDetalhe detalhe) {
        return new PagamentoResponse(
                detalhe.id(),
                detalhe.deId(),
                detalhe.paraId(),
                detalhe.valor().toPlainString(),
                detalhe.status(),
                detalhe.data());
    }
}