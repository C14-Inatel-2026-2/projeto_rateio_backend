package rateio.despesa;

import java.util.UUID;

public record ParcelaResponse(UUID membroId, String valor) {

    public static ParcelaResponse de(ParcelaDetalhe parcela) {
        return new ParcelaResponse(parcela.membroId(), parcela.valor().toPlainString());
    }
}