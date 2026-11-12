package rateio.quitacao;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rateio.grupo.exception.GrupoNaoEncontradoException;
import rateio.saldo.SaldoDetalhe;
import rateio.saldo.SaldoService;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class QuitacaoServiceTest {

    @Mock
    private SaldoService saldoService;

    @InjectMocks
    private QuitacaoService servico;

    private final UUID grupoId = UUID.randomUUID();
    private final UUID membroId = UUID.randomUUID();

    private SaldoDetalhe saldo(UUID id, String nome, String valor) {
        return new SaldoDetalhe(id, nome, new BigDecimal(valor));
    }

    @Test
    void propagaGrupoNaoEncontrado() {
        when(saldoService.consultar(grupoId, membroId)).thenThrow(new GrupoNaoEncontradoException());

        assertThrows(GrupoNaoEncontradoException.class,
                () -> servico.listaTransferencias(membroId, grupoId));
    }

    @Test
    void todosQuitadosDevolveListaVazia() {
        when(saldoService.consultar(grupoId, membroId)).thenReturn(List.of(
                saldo(UUID.randomUUID(), "Ana", "0.00"),
                saldo(UUID.randomUUID(), "Bia", "0.00")));

        List<QuitacaoDetalhe> transferencias = servico.listaTransferencias(membroId, grupoId);

        assertTrue(transferencias.isEmpty());
        verify(saldoService).consultar(grupoId, membroId);
    }

    @Test
    void saldosSessentaMenosVinteMenosQuarentaGeramDuasTransferencias() {
        UUID ana = UUID.randomUUID();
        UUID bia = UUID.randomUUID();
        UUID caio = UUID.randomUUID();
        when(saldoService.consultar(grupoId, membroId)).thenReturn(List.of(
                saldo(ana, "Ana", "60.00"),
                saldo(bia, "Bia", "-20.00"),
                saldo(caio, "Caio", "-40.00")));

        List<QuitacaoDetalhe> transferencias = servico.listaTransferencias(membroId, grupoId);

        assertEquals(2, transferencias.size());
        assertEquals(caio, transferencias.get(0).deId());
        assertEquals("Caio", transferencias.get(0).deNome());
        assertEquals(ana, transferencias.get(0).paraId());
        assertEquals("Ana", transferencias.get(0).paraNome());
        assertEquals(new BigDecimal("40.00"), transferencias.get(0).valor());
        assertEquals(bia, transferencias.get(1).deId());
        assertEquals("Bia", transferencias.get(1).deNome());
        assertEquals("Ana", transferencias.get(1).paraNome());
        assertEquals(new BigDecimal("20.00"), transferencias.get(1).valor());
    }

    @Test
    void membroComSaldoZeroNaoApareceEmNenhumaTransferencia() {
        UUID ana = UUID.randomUUID();
        UUID bia = UUID.randomUUID();
        UUID caio = UUID.randomUUID();
        when(saldoService.consultar(grupoId, membroId)).thenReturn(List.of(
                saldo(ana, "Ana", "30.00"),
                saldo(bia, "Bia", "-30.00"),
                saldo(caio, "Caio", "0.00")));

        List<QuitacaoDetalhe> transferencias = servico.listaTransferencias(membroId, grupoId);

        assertEquals(1, transferencias.size());
        for (QuitacaoDetalhe transferencia : transferencias) {
            assertTrue(!transferencia.deId().equals(caio));
            assertTrue(!transferencia.paraId().equals(caio));
        }
    }

    @Test
    void saldosEmpatadosGeramSempreAMesmaOrdem() {
        UUID ana = UUID.randomUUID();
        UUID bia = UUID.randomUUID();
        UUID caio = UUID.randomUUID();
        List<SaldoDetalhe> saldos = List.of(
                saldo(ana, "Ana", "50.00"),
                saldo(bia, "Bia", "-25.00"),
                saldo(caio, "Caio", "-25.00"));
        when(saldoService.consultar(grupoId, membroId)).thenReturn(saldos);

        List<QuitacaoDetalhe> primeira = servico.listaTransferencias(membroId, grupoId);
        List<QuitacaoDetalhe> segunda = servico.listaTransferencias(membroId, grupoId);

        assertEquals(2, primeira.size());
        assertEquals(bia, primeira.get(0).deId());
        assertEquals(caio, primeira.get(1).deId());
        assertEquals(primeira, segunda);
    }

    @Test
    void aplicarAQuitacaoZeraTodosOsSaldos() {
        UUID ana = UUID.randomUUID();
        UUID bia = UUID.randomUUID();
        UUID caio = UUID.randomUUID();
        UUID duda = UUID.randomUUID();
        List<SaldoDetalhe> saldos = List.of(
                saldo(ana, "Ana", "70.00"),
                saldo(bia, "Bia", "-30.00"),
                saldo(caio, "Caio", "-25.00"),
                saldo(duda, "Duda", "-15.00"));
        when(saldoService.consultar(grupoId, membroId)).thenReturn(saldos);

        List<QuitacaoDetalhe> transferencias = servico.listaTransferencias(membroId, grupoId);

        for (SaldoDetalhe saldo : saldos) {
            BigDecimal restante = saldo.saldo();
            for (QuitacaoDetalhe transferencia : transferencias) {
                if (transferencia.deId().equals(saldo.membroId())) {
                    restante = restante.add(transferencia.valor());
                }
                if (transferencia.paraId().equals(saldo.membroId())) {
                    restante = restante.subtract(transferencia.valor());
                }
            }
            assertEquals(0, restante.compareTo(BigDecimal.ZERO));
        }
    }
}