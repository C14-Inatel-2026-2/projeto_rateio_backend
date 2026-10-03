package rateio.quitacao;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import rateio.quitacao.Quitacao.Transferencia;
import rateio.saldo.Saldo;
public class TesteQuitacao {

    private Saldo saldoMock(UUID membroId, String valor) {
        Saldo saldo = mock(Saldo.class);
        when(saldo.membroId()).thenReturn(membroId);
        when(saldo.valor()).thenReturn(new BigDecimal(valor));
        return saldo;
    }
    @Test
    @DisplayName("Sem mock: saldos vazios geram quitacao vazia")
    void deveGerarQuitacaoVaziaQuandoNaoHaSaldos() {
        Quitacao quitacao = Quitacao.apartirDe(List.of());

        assertTrue(quitacao.estaVazia());
        assertEquals(0, quitacao.quantidadeDeTransferencias());
        assertEquals(0, BigDecimal.ZERO.compareTo(quitacao.totalTransferido()));
        assertTrue(quitacao.transferencias().isEmpty());
    }
}
