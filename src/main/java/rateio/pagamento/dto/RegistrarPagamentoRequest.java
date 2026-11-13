package rateio.pagamento.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record RegistrarPagamentoRequest(UUID paraId, BigDecimal valor) {
}