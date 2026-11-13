package rateio.pagamento.dto;

import rateio.pagamento.domain.StatusPagamento;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PagamentoDetalhe(UUID id, UUID deId, UUID paraId, BigDecimal valor, StatusPagamento status, Instant data) {
}
