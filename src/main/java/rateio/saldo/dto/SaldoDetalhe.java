package rateio.saldo.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record SaldoDetalhe(UUID membroId, String nome, BigDecimal saldo) {
}
