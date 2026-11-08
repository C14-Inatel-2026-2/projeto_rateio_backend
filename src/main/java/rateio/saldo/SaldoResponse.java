package rateio.saldo;

import java.util.UUID;

public record SaldoResponse(UUID membroId, String nome, String saldo) {

    public static SaldoResponse de(SaldoDetalhe detalhe) {
        return new SaldoResponse(detalhe.membroId(), detalhe.nome(), detalhe.saldo().toPlainString());
    }
}