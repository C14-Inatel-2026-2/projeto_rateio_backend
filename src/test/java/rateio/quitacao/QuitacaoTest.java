package rateio.quitacao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
}
