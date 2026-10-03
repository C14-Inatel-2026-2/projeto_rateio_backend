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

    @Test
    @DisplayName("Sem mock: lista retornada e imutavel e nao envolve membros desconhecidos")
    void deveRetornarCopiaImutavelENaoEnvolverMembroDesconhecido() {
        Quitacao quitacao = Quitacao.apartirDe(List.of());

        List<Transferencia> transferencias = quitacao.transferencias();
        Transferencia qualquer = new Transferencia(UUID.randomUUID(), UUID.randomUUID(), BigDecimal.TEN);

        assertThrows(UnsupportedOperationException.class, () -> transferencias.add(qualquer));
        assertFalse(quitacao.envolve(UUID.randomUUID()));
    }
    @Test
    @DisplayName("Com mock: um devedor e um credor geram uma unica transferencia")
    void deveGerarUmaTransferenciaParaDevedorECredor() {
        UUID devedorId = UUID.randomUUID();
        UUID credorId = UUID.randomUUID();
        Saldo devedor = saldoMock(devedorId, "-50.00");
        Saldo credor = saldoMock(credorId, "50.00");

        Quitacao quitacao = Quitacao.apartirDe(List.of(devedor, credor));

        assertFalse(quitacao.estaVazia());
        assertEquals(1, quitacao.quantidadeDeTransferencias());
        assertEquals(0, new BigDecimal("50.00").compareTo(quitacao.totalTransferido()));

        Transferencia t = quitacao.transferencias().get(0);
        assertEquals(devedorId, t.deId());
        assertEquals(credorId, t.paraId());
        assertEquals(0, new BigDecimal("50.00").compareTo(t.valor()));

        assertTrue(quitacao.envolve(devedorId));
        assertTrue(quitacao.envolve(credorId));
        verify(devedor, atLeastOnce()).valor();
        verify(credor, atLeastOnce()).valor();
    }
}
