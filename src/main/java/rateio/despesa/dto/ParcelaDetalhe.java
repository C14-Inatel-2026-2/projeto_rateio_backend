package rateio.despesa.dto;

import rateio.despesa.domain.Despesa;

import java.math.BigDecimal;
import java.util.UUID;

public record ParcelaDetalhe(UUID membroId, BigDecimal valor) {

    public static ParcelaDetalhe de(Despesa.Parcela parcela) {
        return new ParcelaDetalhe(parcela.membroId(), parcela.valor());
    }
}
