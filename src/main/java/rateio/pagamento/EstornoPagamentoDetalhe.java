package rateio.pagamento;

import java.util.UUID;

public record EstornoPagamentoDetalhe(UUID id, StatusPagamento status) {
}
