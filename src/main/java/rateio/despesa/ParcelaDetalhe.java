package rateio.despesa;

import java.util.UUID;
import java.math.BigDecimal;

public record ParcelaDetalhe(UUID membroId, BigDecimal valor) {
}
