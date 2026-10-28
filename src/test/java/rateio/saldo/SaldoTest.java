package rateio.saldo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import rateio.despesa.Despesa;
import rateio.pagamento.Pagamento;


public class SaldoTest {

    private Despesa.Parcela parcela(UUID membroId, String valor) {
        return new Despesa.Parcela(membroId, new BigDecimal(valor));
    }

    private Despesa despesa(UUID pagador, String total, Despesa.Parcela... parcelas) {
        return new Despesa(UUID.randomUUID(), pagador, "Despesa", new BigDecimal(total),
                Despesa.EstrategiaDivisao.VALORES_FIXOS, List.of(parcelas));
    }

    private Pagamento pagamento(UUID pagador, UUID recebedor, String valor) {
        return new Pagamento(UUID.randomUUID(), pagador, recebedor, new BigDecimal(valor));
    }

    private Saldo novoSaldo(UUID membroId, String valor) {
        return new Saldo(membroId, new BigDecimal(valor));
    }

    private void assertValor(String esperado, BigDecimal obtido) {
        assertEquals(0, new BigDecimal(esperado).compareTo(obtido));
    }

    private BigDecimal valorDe(List<Saldo> saldos, UUID membroId) {
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
        Saldo saldo = novoSaldo(membro, "60.00");
        assertEquals(membro, saldo.membroId());
        assertValor("60.00", saldo.valor());
    }

    @Test
    void credor() {
        Saldo saldo = novoSaldo(UUID.randomUUID(), "60.00");
        assertTrue(saldo.ehCredor());
        assertFalse(saldo.ehDevedor());
        assertFalse(saldo.estaQuitado());
    }

    @Test
    void devedor() {
        Saldo saldo = novoSaldo(UUID.randomUUID(), "-40.00");
        assertTrue(saldo.ehDevedor());
        assertFalse(saldo.ehCredor());
        assertFalse(saldo.estaQuitado());
    }

    @Test
    void quitado() {
        Saldo saldo = novoSaldo(UUID.randomUUID(), "0.00");
        assertTrue(saldo.estaQuitado());
        assertFalse(saldo.ehCredor());
        assertFalse(saldo.ehDevedor());
    }

    @Test
    void valorAbsolutoDeDevedor() {
        assertValor("40.00", novoSaldo(UUID.randomUUID(), "-40.00").valorAbsoluto());
    }

    @Test
    void valorAbsolutoDeCredor() {
        assertValor("60.00", novoSaldo(UUID.randomUUID(), "60.00").valorAbsoluto());
    }


}
