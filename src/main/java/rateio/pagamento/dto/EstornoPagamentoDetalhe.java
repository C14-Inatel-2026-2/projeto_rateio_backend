package rateio.pagamento.dto;

import rateio.pagamento.domain.StatusPagamento;

import java.util.UUID;

public record EstornoPagamentoDetalhe(UUID id, StatusPagamento status) {
}
