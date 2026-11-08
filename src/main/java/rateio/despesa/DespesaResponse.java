package rateio.despesa;

import java.time.Instant;
import java.util.ArrayList;
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
        List<ParcelaResponse> parcelas = new ArrayList<>();
        for (ParcelaDetalhe parcela : detalhe.parcelas()) {
            parcelas.add(ParcelaResponse.de(parcela));
        }
        return new DespesaResponse(
                detalhe.id(),
                detalhe.descricao(),
                detalhe.valorTotal().toPlainString(),
                detalhe.pagadorId(),
                detalhe.estrategia(),
                detalhe.status(),
                detalhe.data(),
                parcelas);
    }
}