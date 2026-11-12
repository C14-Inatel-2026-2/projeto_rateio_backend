package rateio.pagamento;

import java.time.Instant;
import java.util.UUID;

public record ExtratoResponse(
        TipoMovimentacao tipo,
        UUID id,
        UUID autorId,
        String autorNome,
        String valor,
        String status,
        Instant data
) {

    public static ExtratoResponse de(ExtratoDetalhe detalhe) {
        return new ExtratoResponse(
                detalhe.tipo(),
                detalhe.id(),
                detalhe.autorId(),
                detalhe.autorNome(),
                detalhe.valor().toPlainString(),
                detalhe.status(),
                detalhe.data());
    }
}
