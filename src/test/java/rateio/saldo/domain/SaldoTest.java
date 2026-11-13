package rateio.saldo.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import rateio.despesa.domain.Despesa;
import rateio.pagamento.domain.Pagamento;

public class SaldoTest {

    private static final UUID GRUPO_ID = UUID.randomUUID();

    private Despesa.Parcela criarParcela(UUID membroId, String valor) {
        return new Despesa.Parcela(membroId, new BigDecimal(valor));
    }

    private Despesa criarDespesa(UUID pagador, String total, Despesa.Parcela... parcelas) {
        return new Despesa(GRUPO_ID, pagador, "Despesa", new BigDecimal(total),
                Despesa.EstrategiaDivisao.VALORES_FIXOS, List.of(parcelas));
    }

    private Pagamento criarPagamento(UUID pagador, UUID recebedor, String valor) {
        return new Pagamento(GRUPO_ID, pagador, recebedor, new BigDecimal(valor));
    }

    private Saldo criarSaldo(UUID membroId, String valor) {
        return new Saldo(membroId, new BigDecimal(valor));
    }

    private void assertValor(String esperado, BigDecimal obtido) {
        assertEquals(0, new BigDecimal(esperado).compareTo(obtido));
    }

    private BigDecimal obterValorDe(List<Saldo> saldos, UUID membroId) {
        for (Saldo saldo : saldos) {
            if (saldo.membroId().equals(membroId)) {
                return saldo.valor();
            }
        }
        throw new AssertionError("Membro ausente no resultado");
    }

    @Test
    void rejeitaMembroNulo() {
        assertThrows(IllegalArgumentException.class, () -> new Saldo(null, BigDecimal.ONE));
    }

    @Test
    void rejeitaValorNulo() {
        assertThrows(IllegalArgumentException.class, () -> new Saldo(UUID.randomUUID(), null));
    }

    @Test
    void guardaMembroEValor() {
        UUID membro = UUID.randomUUID();
        Saldo saldo = criarSaldo(membro, "60.00");
        assertEquals(membro, saldo.membroId());
        assertValor("60.00", saldo.valor());
    }

    @Test
    void credor() {
        Saldo saldo = criarSaldo(UUID.randomUUID(), "60.00");
        assertTrue(saldo.ehCredor());
        assertFalse(saldo.ehDevedor());
        assertFalse(saldo.estaQuitado());
    }

    @Test
    void devedor() {
        Saldo saldo = criarSaldo(UUID.randomUUID(), "-40.00");
        assertTrue(saldo.ehDevedor());
        assertFalse(saldo.ehCredor());
        assertFalse(saldo.estaQuitado());
    }

    @Test
    void quitado() {
        Saldo saldo = criarSaldo(UUID.randomUUID(), "0.00");
        assertTrue(saldo.estaQuitado());
        assertFalse(saldo.ehCredor());
        assertFalse(saldo.ehDevedor());
    }

    @Test
    void valorAbsolutoDeDevedor() {
        assertValor("40.00", criarSaldo(UUID.randomUUID(), "-40.00").valorAbsoluto());
    }

    @Test
    void valorAbsolutoDeCredor() {
        assertValor("60.00", criarSaldo(UUID.randomUUID(), "60.00").valorAbsoluto());
    }

    @Test
    void semDespesasTodosZerados() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        List<Saldo> saldos = Saldo.consolidar(Set.of(a, b), List.of(), List.of());
        assertEquals(2, saldos.size());
        assertValor("0", obterValorDe(saldos, a));
        assertValor("0", obterValorDe(saldos, b));
    }

    @Test
    void quemPagouFicaPositivo() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        Despesa jantar = criarDespesa(a, "100.00", criarParcela(a, "50.00"), criarParcela(b, "50.00"));
        List<Saldo> saldos = Saldo.consolidar(Set.of(a, b), List.of(jantar), List.of());
        assertValor("50.00", obterValorDe(saldos, a));
        assertValor("-50.00", obterValorDe(saldos, b));
    }

    @Test
    void divisaoEntreTres() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        UUID c = UUID.randomUUID();
        Despesa jantar = criarDespesa(a, "90.00", criarParcela(a, "30.00"), criarParcela(b, "30.00"), criarParcela(c, "30.00"));
        List<Saldo> saldos = Saldo.consolidar(Set.of(a, b, c), List.of(jantar), List.of());
        assertValor("60.00", obterValorDe(saldos, a));
        assertValor("-30.00", obterValorDe(saldos, b));
        assertValor("-30.00", obterValorDe(saldos, c));
    }

    @Test
    void pagadorForaDaDivisao() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        Despesa presente = criarDespesa(a, "100.00", criarParcela(b, "100.00"));
        List<Saldo> saldos = Saldo.consolidar(Set.of(a, b), List.of(presente), List.of());
        assertValor("100.00", obterValorDe(saldos, a));
        assertValor("-100.00", obterValorDe(saldos, b));
    }

    @Test
    void membroSemMovimentoAparece() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        UUID c = UUID.randomUUID();
        Despesa jantar = criarDespesa(a, "100.00", criarParcela(a, "50.00"), criarParcela(b, "50.00"));
        List<Saldo> saldos = Saldo.consolidar(Set.of(a, b, c), List.of(jantar), List.of());
        assertEquals(3, saldos.size());
        assertValor("0", obterValorDe(saldos, c));
    }

    @Test
    void somaVariasDespesas() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        Despesa jantar = criarDespesa(a, "100.00", criarParcela(a, "50.00"), criarParcela(b, "50.00"));
        Despesa cinema = criarDespesa(b, "40.00", criarParcela(a, "20.00"), criarParcela(b, "20.00"));
        List<Saldo> saldos = Saldo.consolidar(Set.of(a, b), List.of(jantar, cinema), List.of());
        assertValor("30.00", obterValorDe(saldos, a));
        assertValor("-30.00", obterValorDe(saldos, b));
    }

    @Test
    void ignoraDespesaEstornada() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        Despesa jantar = criarDespesa(a, "100.00", criarParcela(a, "50.00"), criarParcela(b, "50.00"));
        jantar.estornar();
        List<Saldo> saldos = Saldo.consolidar(Set.of(a, b), List.of(jantar), List.of());
        assertValor("0", obterValorDe(saldos, a));
        assertValor("0", obterValorDe(saldos, b));
    }

    @Test
    void pagamentoReduzDivida() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        Despesa jantar = criarDespesa(a, "100.00", criarParcela(a, "50.00"), criarParcela(b, "50.00"));
        Pagamento acerto = criarPagamento(b, a, "30.00");
        List<Saldo> saldos = Saldo.consolidar(Set.of(a, b), List.of(jantar), List.of(acerto));
        assertValor("20.00", obterValorDe(saldos, a));
        assertValor("-20.00", obterValorDe(saldos, b));
    }

    @Test
    void ignoraPagamentoEstornado() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        Despesa jantar = criarDespesa(a, "100.00", criarParcela(a, "50.00"), criarParcela(b, "50.00"));
        Pagamento acerto = criarPagamento(b, a, "30.00");
        acerto.estornar();
        List<Saldo> saldos = Saldo.consolidar(Set.of(a, b), List.of(jantar), List.of(acerto));
        assertValor("50.00", obterValorDe(saldos, a));
        assertValor("-50.00", obterValorDe(saldos, b));
    }

    @Test
    void quitacaoCompletaZeraSaldos() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        Despesa jantar = criarDespesa(a, "100.00", criarParcela(a, "50.00"), criarParcela(b, "50.00"));
        Pagamento acerto = criarPagamento(b, a, "50.00");
        List<Saldo> saldos = Saldo.consolidar(Set.of(a, b), List.of(jantar), List.of(acerto));
        assertValor("0", obterValorDe(saldos, a));
        assertValor("0", obterValorDe(saldos, b));
    }

    @Test
    void consolidadoSomaZero() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        UUID c = UUID.randomUUID();
        Despesa jantar = criarDespesa(a, "90.00", criarParcela(a, "30.00"), criarParcela(b, "30.00"), criarParcela(c, "30.00"));
        Despesa cinema = criarDespesa(b, "40.00", criarParcela(a, "20.00"), criarParcela(b, "20.00"));
        Pagamento acerto = criarPagamento(c, a, "10.00");
        List<Saldo> saldos = Saldo.consolidar(Set.of(a, b, c), List.of(jantar, cinema), List.of(acerto));
        assertTrue(Saldo.somaZero(saldos));
    }

    @Test
    void somaDosSaldos() {
        List<Saldo> saldos = List.of(
                criarSaldo(UUID.randomUUID(), "60.00"),
                criarSaldo(UUID.randomUUID(), "-20.00"),
                criarSaldo(UUID.randomUUID(), "-40.00"));
        assertValor("0", Saldo.soma(saldos));
        assertTrue(Saldo.somaZero(saldos));
    }

    @Test
    void somaDiferenteDeZero() {
        List<Saldo> saldos = List.of(
                criarSaldo(UUID.randomUUID(), "60.00"),
                criarSaldo(UUID.randomUUID(), "-20.00"));
        assertValor("40.00", Saldo.soma(saldos));
        assertFalse(Saldo.somaZero(saldos));
    }

    @Test
    void somaListaVazia() {
        assertValor("0", Saldo.soma(List.of()));
        assertTrue(Saldo.somaZero(List.of()));
    }
}