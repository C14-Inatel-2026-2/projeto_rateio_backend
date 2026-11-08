package rateio.quitacao;

import java.math.BigDecimal;
import java.util.UUID;

public record QuitacaoDetalhe (UUID deId, String deNome, UUID paraId, String paraNome, BigDecimal valor) {
}
