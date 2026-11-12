package rateio.despesa.service;

import org.junit.jupiter.api.Test;
import rateio.despesa.domain.Despesa;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DivisaoServiceTest {

    private final DivisaoService servico = new DivisaoService();

    @Test
    void testarDivisaoCemPorTres() {
        BigDecimal valorTotal = new BigDecimal("100.00");
        List<UUID> membros = List.of(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID()
        );

        List<Despesa.Parcela> resultado = servico.dividirIgual(valorTotal, membros);

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

    @Test
    void rejeitaValorNuloNaDivisaoPorPeso() {
        Map<UUID, BigDecimal> pesos = new LinkedHashMap<>();
        pesos.put(UUID.randomUUID(), new BigDecimal("1"));

        assertThrows(IllegalArgumentException.class,
                () -> servico.dividirPorPeso(null, pesos));
    }

    @Test
    void rejeitaValorZeroNaDivisaoPorPeso() {
        Map<UUID, BigDecimal> pesos = new LinkedHashMap<>();
        pesos.put(UUID.randomUUID(), new BigDecimal("1"));

        assertThrows(IllegalArgumentException.class,
                () -> servico.dividirPorPeso(BigDecimal.ZERO, pesos));
    }

    @Test
    void rejeitaMapaNuloNaDivisaoPorPeso() {
        assertThrows(IllegalArgumentException.class,
                () -> servico.dividirPorPeso(new BigDecimal("10.00"), null));
    }

    @Test
    void rejeitaMapaVazioNaDivisaoPorPeso() {
        assertThrows(IllegalArgumentException.class,
                () -> servico.dividirPorPeso(new BigDecimal("10.00"), new LinkedHashMap<>()));
    }

    @Test
    void rejeitaPesoNulo() {
        Map<UUID, BigDecimal> pesos = new LinkedHashMap<>();
        pesos.put(UUID.randomUUID(), null);

        assertThrows(IllegalArgumentException.class,
                () -> servico.dividirPorPeso(new BigDecimal("10.00"), pesos));
    }

    @Test
    void rejeitaPesoZero() {
        Map<UUID, BigDecimal> pesos = new LinkedHashMap<>();
        pesos.put(UUID.randomUUID(), BigDecimal.ZERO);

        assertThrows(IllegalArgumentException.class,
                () -> servico.dividirPorPeso(new BigDecimal("10.00"), pesos));
    }

    @Test
    void rejeitaPesoNegativo() {
        Map<UUID, BigDecimal> pesos = new LinkedHashMap<>();
        pesos.put(UUID.randomUUID(), new BigDecimal("-1"));

        assertThrows(IllegalArgumentException.class,
                () -> servico.dividirPorPeso(new BigDecimal("10.00"), pesos));
    }

    @Test
    void dividePorPesoUmEDois() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        Map<UUID, BigDecimal> pesos = new LinkedHashMap<>();
        pesos.put(a, new BigDecimal("1"));
        pesos.put(b, new BigDecimal("2"));

        List<Despesa.Parcela> parcelas = servico.dividirPorPeso(new BigDecimal("100.00"), pesos);

        assertEquals(a, parcelas.get(0).membroId());
        assertEquals(new BigDecimal("33.33"), parcelas.get(0).valor());
        assertEquals(b, parcelas.get(1).membroId());
        assertEquals(new BigDecimal("66.67"), parcelas.get(1).valor());
    }

    @Test
    void sobraVaiParaQuemAparecePrimeiroNoEmpate() {
        Map<UUID, BigDecimal> pesos = new LinkedHashMap<>();
        pesos.put(UUID.randomUUID(), new BigDecimal("1"));
        pesos.put(UUID.randomUUID(), new BigDecimal("1"));
        pesos.put(UUID.randomUUID(), new BigDecimal("1"));

        List<Despesa.Parcela> parcelas = servico.dividirPorPeso(new BigDecimal("10.00"), pesos);

        assertEquals(new BigDecimal("3.34"), parcelas.get(0).valor());
        assertEquals(new BigDecimal("3.33"), parcelas.get(1).valor());
        assertEquals(new BigDecimal("3.33"), parcelas.get(2).valor());
    }

    @Test
    void sobraVaiParaMaiorResto() {
        Map<UUID, BigDecimal> pesos = new LinkedHashMap<>();
        pesos.put(UUID.randomUUID(), new BigDecimal("1"));
        pesos.put(UUID.randomUUID(), new BigDecimal("2"));
        pesos.put(UUID.randomUUID(), new BigDecimal("3"));

        List<Despesa.Parcela> parcelas = servico.dividirPorPeso(new BigDecimal("100.00"), pesos);

        assertEquals(new BigDecimal("16.67"), parcelas.get(0).valor());
        assertEquals(new BigDecimal("33.33"), parcelas.get(1).valor());
        assertEquals(new BigDecimal("50.00"), parcelas.get(2).valor());
    }

    @Test
    void dividePorPesoDecimal() {
        Map<UUID, BigDecimal> pesos = new LinkedHashMap<>();
        pesos.put(UUID.randomUUID(), new BigDecimal("0.5"));
        pesos.put(UUID.randomUUID(), new BigDecimal("1.5"));

        List<Despesa.Parcela> parcelas = servico.dividirPorPeso(new BigDecimal("10.00"), pesos);

        assertEquals(new BigDecimal("2.50"), parcelas.get(0).valor());
        assertEquals(new BigDecimal("7.50"), parcelas.get(1).valor());
    }

    @Test
    void parcelasDePesoSomamExatamenteOTotal() {
        Map<UUID, BigDecimal> pesos = new LinkedHashMap<>();
        pesos.put(UUID.randomUUID(), new BigDecimal("3"));
        pesos.put(UUID.randomUUID(), new BigDecimal("7"));
        pesos.put(UUID.randomUUID(), new BigDecimal("11"));
        pesos.put(UUID.randomUUID(), new BigDecimal("13"));
        BigDecimal total = new BigDecimal("99.99");

        List<Despesa.Parcela> parcelas = servico.dividirPorPeso(total, pesos);

        BigDecimal soma = BigDecimal.ZERO;
        for (Despesa.Parcela parcela : parcelas) {
            soma = soma.add(parcela.valor());
        }
        assertEquals(0, total.compareTo(soma));
    }

    @Test
    void mantemOsValoresFixosInformados() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        Map<UUID, BigDecimal> valores = new LinkedHashMap<>();
        valores.put(a, new BigDecimal("30.00"));
        valores.put(b, new BigDecimal("70.00"));

        List<Despesa.Parcela> parcelas = servico.dividirPorValoresFixos(new BigDecimal("100.00"), valores);

        assertEquals(List.of(
                new Despesa.Parcela(a, new BigDecimal("30.00")),
                new Despesa.Parcela(b, new BigDecimal("70.00"))), parcelas);
    }

    @Test
    void rejeitaValoresFixosQueNaoSomamOTotal() {
        Map<UUID, BigDecimal> valores = new LinkedHashMap<>();
        valores.put(UUID.randomUUID(), new BigDecimal("30.00"));
        valores.put(UUID.randomUUID(), new BigDecimal("60.00"));

        assertThrows(IllegalArgumentException.class,
                () -> servico.dividirPorValoresFixos(new BigDecimal("100.00"), valores));
    }

    @Test
    void rejeitaValorFixoNulo() {
        Map<UUID, BigDecimal> valores = new LinkedHashMap<>();
        valores.put(UUID.randomUUID(), null);

        assertThrows(IllegalArgumentException.class,
                () -> servico.dividirPorValoresFixos(new BigDecimal("100.00"), valores));
    }

    @Test
    void rejeitaValorFixoNaoPositivo() {
        Map<UUID, BigDecimal> valores = new LinkedHashMap<>();
        valores.put(UUID.randomUUID(), BigDecimal.ZERO);

        assertThrows(IllegalArgumentException.class,
                () -> servico.dividirPorValoresFixos(BigDecimal.ZERO, valores));
    }

    @Test
    void rejeitaValorFixoComTresCasasDecimais() {
        Map<UUID, BigDecimal> valores = new LinkedHashMap<>();
        valores.put(UUID.randomUUID(), new BigDecimal("10.005"));

        assertThrows(IllegalArgumentException.class,
                () -> servico.dividirPorValoresFixos(new BigDecimal("10.005"), valores));
    }
}
