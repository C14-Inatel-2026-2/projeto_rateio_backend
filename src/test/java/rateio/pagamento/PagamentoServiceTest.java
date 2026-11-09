package rateio.pagamento;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rateio.grupo.GrupoEntidade;
import rateio.grupo.GrupoMembroRepository;
import rateio.grupo.GrupoNaoEncontradoException;
import rateio.grupo.GrupoRepository;
import rateio.membro.Membro;
import rateio.membro.MembroNaoEncontradoException;
import rateio.membro.MembroRepository;
import rateio.quitacao.QuitacaoDetalhe;
import rateio.quitacao.QuitacaoService;
import rateio.saldo.SaldoService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PagamentoServiceTest {

    @Mock
    private GrupoRepository grupoRepositorio;

    @Mock
    private GrupoMembroRepository participacaoRepositorio;

    @Mock
    private MembroRepository membroRepositorio;

    @Mock
    private QuitacaoService quitacaoService;

    @Mock
    private SaldoService saldoService;

    @Mock
    private PagamentoRepository pagamentoRepositorio;

    @InjectMocks
    private PagamentoService servico;

    @Captor
    private ArgumentCaptor<PagamentoEntidade> captor;

    private final UUID grupoId = UUID.randomUUID();
    private final UUID registradorId = UUID.randomUUID();
    private final UUID paraId = UUID.randomUUID();
    private final UUID pagamentoId = UUID.randomUUID();

    private Membro membro(UUID id) {
        Membro membro = new Membro();
        membro.setId(id);
        return membro;
    }

    private QuitacaoDetalhe transferencia(UUID deId, UUID paraId, String valor) {
        return new QuitacaoDetalhe(deId, "De", paraId, "Para", new BigDecimal(valor));
    }

    @Test
    void rejeitaGrupoIdNulo() {
        assertThrows(IllegalArgumentException.class,
                () -> servico.registrar(null, registradorId, paraId, new BigDecimal("10.00")));
    }

    @Test
    void rejeitaRegistradorIdNulo() {
        assertThrows(IllegalArgumentException.class,
                () -> servico.registrar(grupoId, null, paraId, new BigDecimal("10.00")));
    }

    @Test
    void rejeitaParaIdNulo() {
        assertThrows(IllegalArgumentException.class,
                () -> servico.registrar(grupoId, registradorId, null, new BigDecimal("10.00")));
    }

    @Test
    void rejeitaValorNulo() {
        assertThrows(IllegalArgumentException.class,
                () -> servico.registrar(grupoId, registradorId, paraId, null));
    }

    @Test
    void rejeitaValorZero() {
        assertThrows(ValorInvalidoException.class,
                () -> servico.registrar(grupoId, registradorId, paraId, BigDecimal.ZERO));

        verify(pagamentoRepositorio, never()).save(any());
    }

    @Test
    void rejeitaValorNegativo() {
        assertThrows(ValorInvalidoException.class,
                () -> servico.registrar(grupoId, registradorId, paraId, new BigDecimal("-5.00")));

        verify(pagamentoRepositorio, never()).save(any());
    }

    @Test
    void rejeitaValorComTresCasasDecimais() {
        assertThrows(ValorInvalidoException.class,
                () -> servico.registrar(grupoId, registradorId, paraId, new BigDecimal("10.005")));

        verify(pagamentoRepositorio, never()).save(any());
    }

    @Test
    void rejeitaPagarASiMesmo() {
        assertThrows(ValorInvalidoException.class,
                () -> servico.registrar(grupoId, registradorId, registradorId, new BigDecimal("10.00")));

        verify(pagamentoRepositorio, never()).save(any());
    }

    @Test
    void propagaGrupoNaoEncontrado() {
        when(quitacaoService.listaTransferencias(registradorId, grupoId))
                .thenThrow(new GrupoNaoEncontradoException());

        assertThrows(GrupoNaoEncontradoException.class,
                () -> servico.registrar(grupoId, registradorId, paraId, new BigDecimal("10.00")));

        verify(pagamentoRepositorio, never()).save(any());
    }

    @Test
    void rejeitaDestinatarioForaDoGrupo() {
        when(quitacaoService.listaTransferencias(registradorId, grupoId))
                .thenReturn(List.of(transferencia(registradorId, paraId, "50.00")));
        when(participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId, paraId)).thenReturn(false);

        assertThrows(MembroNaoEncontradoException.class,
                () -> servico.registrar(grupoId, registradorId, paraId, new BigDecimal("10.00")));

        verify(pagamentoRepositorio, never()).save(any());
    }

    @Test
    void rejeitaPagamentoSemDividaComODestinatario() {
        when(quitacaoService.listaTransferencias(registradorId, grupoId)).thenReturn(List.of());
        when(participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId, paraId)).thenReturn(true);

        assertThrows(ValorMaiorDividaException.class,
                () -> servico.registrar(grupoId, registradorId, paraId, new BigDecimal("10.00")));

        verify(pagamentoRepositorio, never()).save(any());
    }

    @Test
    void rejeitaPagamentoParaOutroQueNaoEOCredorDaTransferencia() {
        UUID outroId = UUID.randomUUID();
        when(quitacaoService.listaTransferencias(registradorId, grupoId))
                .thenReturn(List.of(transferencia(registradorId, outroId, "50.00")));
        when(participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId, paraId)).thenReturn(true);

        assertThrows(ValorMaiorDividaException.class,
                () -> servico.registrar(grupoId, registradorId, paraId, new BigDecimal("10.00")));

        verify(pagamentoRepositorio, never()).save(any());
    }

    @Test
    void rejeitaValorMaiorQueADivida() {
        when(quitacaoService.listaTransferencias(registradorId, grupoId))
                .thenReturn(List.of(transferencia(registradorId, paraId, "20.00")));
        when(participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId, paraId)).thenReturn(true);

        assertThrows(ValorMaiorDividaException.class,
                () -> servico.registrar(grupoId, registradorId, paraId, new BigDecimal("20.01")));

        verify(pagamentoRepositorio, never()).save(any());
    }

    @Test
    void registraPagamentoDoValorTotalDaDivida() {
        GrupoEntidade grupo = new GrupoEntidade();
        Membro de = membro(registradorId);
        Membro para = membro(paraId);
        UUID pagamentoId = UUID.randomUUID();
        when(quitacaoService.listaTransferencias(registradorId, grupoId))
                .thenReturn(List.of(transferencia(registradorId, paraId, "20.00")));
        when(participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId, paraId)).thenReturn(true);
        when(grupoRepositorio.findById(grupoId)).thenReturn(Optional.of(grupo));
        when(membroRepositorio.findById(registradorId)).thenReturn(Optional.of(de));
        when(membroRepositorio.findById(paraId)).thenReturn(Optional.of(para));
        when(pagamentoRepositorio.save(any(PagamentoEntidade.class))).thenAnswer(i -> {
            PagamentoEntidade salvo = i.getArgument(0);
            salvo.setId(pagamentoId);
            return salvo;
        });

        PagamentoDetalhe detalhe = servico.registrar(grupoId, registradorId, paraId, new BigDecimal("20.00"));

        verify(pagamentoRepositorio).save(captor.capture());
        PagamentoEntidade pagamento = captor.getValue();
        assertSame(grupo, pagamento.getGrupo());
        assertSame(de, pagamento.getDe());
        assertSame(para, pagamento.getPara());
        assertEquals(new BigDecimal("20.00"), pagamento.getValor());
        assertEquals(StatusPagamento.ATIVO, pagamento.getStatus());
        assertNotNull(pagamento.getCriadoEm());

        assertEquals(pagamentoId, detalhe.id());
        assertEquals(registradorId, detalhe.deId());
        assertEquals(paraId, detalhe.paraId());
        assertEquals(new BigDecimal("20.00"), detalhe.valor());
        assertEquals(StatusPagamento.ATIVO, detalhe.status());
    }

    @Test
    void registraPagamentoParcial() {
        when(quitacaoService.listaTransferencias(registradorId, grupoId))
                .thenReturn(List.of(transferencia(registradorId, paraId, "50.00")));
        when(participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId, paraId)).thenReturn(true);
        when(grupoRepositorio.findById(grupoId)).thenReturn(Optional.of(new GrupoEntidade()));
        when(membroRepositorio.findById(registradorId)).thenReturn(Optional.of(membro(registradorId)));
        when(membroRepositorio.findById(paraId)).thenReturn(Optional.of(membro(paraId)));

        PagamentoDetalhe detalhe = servico.registrar(grupoId, registradorId, paraId, new BigDecimal("10.00"));

        verify(pagamentoRepositorio).save(any(PagamentoEntidade.class));
        assertEquals(new BigDecimal("10.00"), detalhe.valor());
        assertEquals(StatusPagamento.ATIVO, detalhe.status());
    }

    @Test
    void rejeitaMembroIdNuloAoEstornar() {
        assertThrows(IllegalArgumentException.class,
                () -> servico.estornar(grupoId, pagamentoId, null));
    }

    @Test
    void rejeitaGrupoIdNuloAoEstornar() {
        assertThrows(IllegalArgumentException.class,
                () -> servico.estornar(null, pagamentoId, registradorId));
    }

    @Test
    void rejeitaPagamentoIdNuloAoEstornar() {
        assertThrows(IllegalArgumentException.class,
                () -> servico.estornar(grupoId, null, registradorId));
    }

    @Test
    void rejeitaGrupoNaoEncontradoAoEstornar() {
        when(grupoRepositorio.findById(grupoId)).thenReturn(Optional.empty());

        assertThrows(PagamentoNaoEncontradoException.class,
                () -> servico.estornar(grupoId, pagamentoId, registradorId));

        verify(pagamentoRepositorio, never()).findByIdAndGrupoId(any(), any());
        verify(participacaoRepositorio, never()).existsByGrupoIdAndMembroId(any(), any());
    }

    @Test
    void rejeitaQuemPedeForaDoGrupoAoEstornar() {
        when(grupoRepositorio.findById(grupoId)).thenReturn(Optional.of(new GrupoEntidade()));
        when(participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId, registradorId)).thenReturn(false);

        assertThrows(PagamentoNaoEncontradoException.class,
                () -> servico.estornar(grupoId, pagamentoId, registradorId));

        verify(pagamentoRepositorio, never()).findByIdAndGrupoId(any(), any());
    }

    @Test
    void rejeitaPagamentoNaoEncontradoAoEstornar() {
        when(grupoRepositorio.findById(grupoId)).thenReturn(Optional.of(new GrupoEntidade()));
        when(participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId, registradorId)).thenReturn(true);
        when(pagamentoRepositorio.findByIdAndGrupoId(pagamentoId, grupoId)).thenReturn(Optional.empty());

        assertThrows(PagamentoNaoEncontradoException.class,
                () -> servico.estornar(grupoId, pagamentoId, registradorId));

        verify(pagamentoRepositorio, never()).save(any());
    }

    @Test
    void rejeitaPagamentoJaEstornado() {
        PagamentoEntidade pagamento = new PagamentoEntidade();
        pagamento.setId(pagamentoId);
        pagamento.setStatus(StatusPagamento.ESTORNADO);
        when(grupoRepositorio.findById(grupoId)).thenReturn(Optional.of(new GrupoEntidade()));
        when(participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId, registradorId)).thenReturn(true);
        when(pagamentoRepositorio.findByIdAndGrupoId(pagamentoId, grupoId)).thenReturn(Optional.of(pagamento));

        assertThrows(PagamentoJaEstornadoException.class,
                () -> servico.estornar(grupoId, pagamentoId, registradorId));

        verify(pagamentoRepositorio, never()).save(any());
    }

    @Test
    void estornaPagamento() {
        PagamentoEntidade pagamento = new PagamentoEntidade();
        pagamento.setId(pagamentoId);
        pagamento.setDe(membro(registradorId));
        pagamento.setPara(membro(paraId));
        pagamento.setValor(new BigDecimal("20.00"));
        pagamento.setStatus(StatusPagamento.ATIVO);
        when(grupoRepositorio.findById(grupoId)).thenReturn(Optional.of(new GrupoEntidade()));
        when(participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId, registradorId)).thenReturn(true);
        when(pagamentoRepositorio.findByIdAndGrupoId(pagamentoId, grupoId)).thenReturn(Optional.of(pagamento));

        EstornoPagamentoDetalhe detalhe = servico.estornar(grupoId, pagamentoId, registradorId);

        assertEquals(StatusPagamento.ESTORNADO, pagamento.getStatus());
        assertNotNull(pagamento.getEstornadoEm());
        verify(pagamentoRepositorio).save(pagamento);
        assertEquals(pagamentoId, detalhe.id());
        assertEquals(StatusPagamento.ESTORNADO, detalhe.status());
    }


}