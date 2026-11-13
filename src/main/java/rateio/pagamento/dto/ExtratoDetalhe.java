package rateio.pagamento.dto;

import rateio.pagamento.domain.TipoMovimentacao;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ExtratoDetalhe (TipoMovimentacao tipo,
                              UUID id,
                              UUID autorId,
                              String autorNome,
                              BigDecimal valor,
                              String status,
                              Instant data) {
}
