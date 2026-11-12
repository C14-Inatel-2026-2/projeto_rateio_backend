package rateio.despesa.dto;

import rateio.despesa.domain.StatusDespesa;
import rateio.despesa.entity.DespesaEntidade;

import java.util.UUID;

public record EstornoDetalhe(UUID id, StatusDespesa status) {

    public static EstornoDetalhe de(DespesaEntidade despesa) {
        return new EstornoDetalhe(despesa.getId(), despesa.getStatus());
    }
}
