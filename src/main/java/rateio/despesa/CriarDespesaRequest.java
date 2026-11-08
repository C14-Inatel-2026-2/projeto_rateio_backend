package rateio.despesa;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CriarDespesaRequest(
        String descricao,
        BigDecimal valorTotal,
        UUID pagadorId,
        Despesa.EstrategiaDivisao estrategia,
        List<ParticipanteRequest> participantes
) {
}