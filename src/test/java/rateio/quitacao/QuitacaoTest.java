package rateio.quitacao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.atLeastOnce;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import rateio.saldo.Saldo;
public class QuitacaoTest {

    @Test
    void tresSaldos() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        UUID c = UUID.randomUUID();

        List<Saldo> saldos = List.of(
                new Saldo(a, new BigDecimal("60")),
                new Saldo(b, new BigDecimal("-20")),
                new Saldo(c, new BigDecimal("-40"))
        );

        Quitacao quitacao = Quitacao.apartirDe(saldos);

        assertEquals(2, quitacao.quantidadeDeTransferencias());
        assertEquals(0, quitacao.totalTransferido().compareTo(new BigDecimal("60")));
    }

    @Test
    void saldoZero() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        UUID semMovimentacao = UUID.randomUUID();

        List<Saldo> saldos = List.of(
                new Saldo(a, new BigDecimal("30")),
                new Saldo(b, new BigDecimal("-30")),
                new Saldo(semMovimentacao, BigDecimal.ZERO)
        );

        Quitacao quitacao = Quitacao.apartirDe(saldos);

        assertFalse(quitacao.envolve(semMovimentacao));
        assertEquals(1, quitacao.quantidadeDeTransferencias());
    }

    @Test
    void mockCredorDevedor() {
        UUID credorId = UUID.randomUUID();
        UUID devedorId = UUID.randomUUID();

        Saldo credor = mock(Saldo.class);
        when(credor.membroId()).thenReturn(credorId);
        when(credor.valor()).thenReturn(new BigDecimal("100"));

        Saldo devedor = mock(Saldo.class);
        when(devedor.membroId()).thenReturn(devedorId);
        when(devedor.valor()).thenReturn(new BigDecimal("-100"));

        Quitacao quitacao = Quitacao.apartirDe(List.of(credor, devedor));

        assertEquals(1, quitacao.quantidadeDeTransferencias());
        assertEquals(devedorId, quitacao.transferencias().get(0).deId());
        assertEquals(credorId, quitacao.transferencias().get(0).paraId());

        verify(credor, atLeastOnce()).valor();
        verify(devedor, atLeastOnce()).valor();
    }

    @Test
    void mockSomaInvalida() {
        Saldo saldo1 = mock(Saldo.class);
        when(saldo1.valor()).thenReturn(new BigDecimal("100"));

        Saldo saldo2 = mock(Saldo.class);
        when(saldo2.valor()).thenReturn(new BigDecimal("-50"));

        List<Saldo> saldosInconsistentes = List.of(saldo1, saldo2);

        assertThrows(IllegalArgumentException.class,
                () -> Quitacao.apartirDe(saldosInconsistentes));
    }
}
