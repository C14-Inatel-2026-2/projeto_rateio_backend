package rateio.despesa;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DivisaoServiceTest {

    @Test
    public void testarDivisaoCemPorTres() {
        DivisaoService service = new DivisaoService();

        BigDecimal valorTotal = new BigDecimal("100.00");
        List<UUID> membros = List.of(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID()
        );

        List<Despesa.Parcela> resultado = service.dividirIgual(valorTotal, membros);

        assertEquals(3, resultado.size());
        assertEquals(membros.get(0), resultado.get(0).membroId());
        assertEquals(new BigDecimal("33.34"), resultado.get(0).valor());
        assertEquals(membros.get(1), resultado.get(1).membroId());
        assertEquals(new BigDecimal("33.33"), resultado.get(1).valor());
        assertEquals(membros.get(2), resultado.get(2).membroId());
        assertEquals(new BigDecimal("33.33"), resultado.get(2).valor());

        BigDecimal somaDasParcelas = resultado.stream()
                .map(Despesa.Parcela::valor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        assertEquals(valorTotal, somaDasParcelas);
    }
}