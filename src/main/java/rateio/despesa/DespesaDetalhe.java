package rateio.despesa;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record DespesaDetalhe(
        UUID id,
        String descricao,
        BigDecimal valorTotal,
        UUID pagadorId,
        Despesa.EstrategiaDivisao estrategia,
        StatusDespesa status,
        Instant data,
        List<ParcelaDetalhe> parcelas
) {


}
