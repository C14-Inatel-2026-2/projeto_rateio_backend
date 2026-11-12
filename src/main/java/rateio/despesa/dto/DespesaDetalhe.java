package rateio.despesa.dto;

import rateio.despesa.domain.Despesa;
import rateio.despesa.domain.StatusDespesa;
import rateio.despesa.entity.DespesaEntidade;

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

    public static DespesaDetalhe de(DespesaEntidade despesa, List<Despesa.Parcela> parcelas) {
        return new DespesaDetalhe(
                despesa.getId(),
                despesa.getDescricao(),
                despesa.getValorTotal(),
                despesa.getPagador().getId(),
                despesa.getEstrategia(),
                despesa.getStatus(),
                despesa.getCriadaEm(),
                parcelas.stream().map(ParcelaDetalhe::de).toList());
    }
}
