package rateio.despesa.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ParticipanteRequest(
        UUID membroId,
        BigDecimal peso,
        BigDecimal valor
) {}
