package rateio.despesa;

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
}