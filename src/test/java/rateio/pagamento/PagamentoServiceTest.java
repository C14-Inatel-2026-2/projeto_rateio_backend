package rateio.pagamento;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rateio.despesa.entity.DespesaEntidade;
import rateio.despesa.repository.DespesaRepository;
import rateio.grupo.entity.GrupoEntidade;
import rateio.grupo.exception.GrupoNaoEncontradoException;
import rateio.grupo.repository.GrupoMembroRepository;
import rateio.grupo.repository.GrupoRepository;
import rateio.membro.Membro;
import rateio.membro.MembroNaoEncontradoException;
import rateio.membro.MembroRepository;
import rateio.quitacao.QuitacaoDetalhe;
import rateio.quitacao.QuitacaoService;
import rateio.saldo.SaldoService;
import rateio.despesa.domain.StatusDespesa;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
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
    private PagamentoRepository pagamentoRepositorio;

    @Mock
    private DespesaRepository despesaRepositorio;

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

    private Membro membroComNome(UUID id, String nome) {
        Membro membro = new Membro();
        membro.setId(id);
        membro.setNome(nome);
        return membro;
    }

    private DespesaEntidade despesaEntidade(UUID id, Membro pagador, String valor, StatusDespesa status, Instant data) {
        DespesaEntidade despesa = new DespesaEntidade();
        despesa.setId(id);
        despesa.setPagador(pagador);
        despesa.setValorTotal(new BigDecimal(valor));
        despesa.setStatus(status);
        despesa.setCriadaEm(data);
        return despesa;
    }

    private PagamentoEntidade pagamentoEntidade(UUID id, Membro de, String valor, StatusPagamento status, Instant data) {
        PagamentoEntidade pagamento = new PagamentoEntidade();
        pagamento.setId(id);
        pagamento.setDe(de);
        pagamento.setValor(new BigDecimal(valor));
        pagamento.setStatus(status);
        pagamento.setCriadoEm(data);
        return pagamento;
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

    @Test
    void rejeitaGrupoIdNuloAoConsultarExtrato() {
        assertThrows(IllegalArgumentException.class,
                () -> servico.extrato(null, registradorId));
    }

    @Test
    void rejeitaMembroIdNuloAoConsultarExtrato() {
        assertThrows(IllegalArgumentException.class,
                () -> servico.extrato(grupoId, null));
    }

    @Test
    void rejeitaGrupoNaoEncontradoAoConsultarExtrato() {
        when(grupoRepositorio.findById(grupoId)).thenReturn(Optional.empty());

        assertThrows(GrupoNaoEncontradoException.class,
                () -> servico.extrato(grupoId, registradorId));

        verify(pagamentoRepositorio, never()).findByGrupoId(any());
        verify(despesaRepositorio, never()).findByGrupoId(any());
    }

    @Test
    void rejeitaQuemPedeForaDoGrupoAoConsultarExtrato() {
        when(grupoRepositorio.findById(grupoId)).thenReturn(Optional.of(new GrupoEntidade()));
        when(participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId, registradorId)).thenReturn(false);

        assertThrows(GrupoNaoEncontradoException.class,
                () -> servico.extrato(grupoId, registradorId));

        verify(pagamentoRepositorio, never()).findByGrupoId(any());
        verify(despesaRepositorio, never()).findByGrupoId(any());
    }

    @Test
    void devolveExtratoVazioQuandoNaoHaMovimentacoes() {
        when(grupoRepositorio.findById(grupoId)).thenReturn(Optional.of(new GrupoEntidade()));
        when(participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId, registradorId)).thenReturn(true);
        when(pagamentoRepositorio.findByGrupoId(grupoId)).thenReturn(List.of());
        when(despesaRepositorio.findByGrupoId(grupoId)).thenReturn(List.of());

        List<ExtratoDetalhe> extrato = servico.extrato(grupoId, registradorId);

        assertTrue(extrato.isEmpty());
    }

    @Test
    void ordenaDespesasEPagamentosEmOrdemCronologica() {
        Membro ana = membroComNome(UUID.randomUUID(), "Ana");
        Membro bia = membroComNome(UUID.randomUUID(), "Bia");
        DespesaEntidade jantar = despesaEntidade(UUID.randomUUID(), ana, "100.00",
                StatusDespesa.ATIVA, Instant.parse("2026-11-01T10:00:00Z"));
        PagamentoEntidade pagamento = pagamentoEntidade(UUID.randomUUID(), bia, "20.00",
                StatusPagamento.ATIVO, Instant.parse("2026-11-02T10:00:00Z"));
        DespesaEntidade mercado = despesaEntidade(UUID.randomUUID(), bia, "60.00",
                StatusDespesa.ESTORNADA, Instant.parse("2026-11-03T10:00:00Z"));
        when(grupoRepositorio.findById(grupoId)).thenReturn(Optional.of(new GrupoEntidade()));
        when(participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId, registradorId)).thenReturn(true);
        when(pagamentoRepositorio.findByGrupoId(grupoId)).thenReturn(List.of(pagamento));
        when(despesaRepositorio.findByGrupoId(grupoId)).thenReturn(List.of(mercado, jantar));

        List<ExtratoDetalhe> extrato = servico.extrato(grupoId, registradorId);

        assertEquals(3, extrato.size());
        assertEquals(jantar.getId(), extrato.get(0).id());
        assertEquals(TipoMovimentacao.DESPESA, extrato.get(0).tipo());
        assertEquals(pagamento.getId(), extrato.get(1).id());
        assertEquals(TipoMovimentacao.PAGAMENTO, extrato.get(1).tipo());
        assertEquals(mercado.getId(), extrato.get(2).id());
        assertEquals(TipoMovimentacao.DESPESA, extrato.get(2).tipo());
    }

    @Test
    void extratoTrazAutorValorEStatusDeCadaItem() {
        Membro ana = membroComNome(UUID.randomUUID(), "Ana");
        Membro bia = membroComNome(UUID.randomUUID(), "Bia");
        DespesaEntidade jantar = despesaEntidade(UUID.randomUUID(), ana, "100.00",
                StatusDespesa.ATIVA, Instant.parse("2026-11-01T10:00:00Z"));
        PagamentoEntidade pagamento = pagamentoEntidade(UUID.randomUUID(), bia, "20.00",
                StatusPagamento.ESTORNADO, Instant.parse("2026-11-02T10:00:00Z"));
        when(grupoRepositorio.findById(grupoId)).thenReturn(Optional.of(new GrupoEntidade()));
        when(participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId, registradorId)).thenReturn(true);
        when(pagamentoRepositorio.findByGrupoId(grupoId)).thenReturn(List.of(pagamento));
        when(despesaRepositorio.findByGrupoId(grupoId)).thenReturn(List.of(jantar));

        List<ExtratoDetalhe> extrato = servico.extrato(grupoId, registradorId);

        assertEquals(ana.getId(), extrato.get(0).autorId());
        assertEquals("Ana", extrato.get(0).autorNome());
        assertEquals(new BigDecimal("100.00"), extrato.get(0).valor());
        assertEquals("ATIVA", extrato.get(0).status());
        assertEquals(Instant.parse("2026-11-01T10:00:00Z"), extrato.get(0).data());
        assertEquals(bia.getId(), extrato.get(1).autorId());
        assertEquals("Bia", extrato.get(1).autorNome());
        assertEquals(new BigDecimal("20.00"), extrato.get(1).valor());
        assertEquals("ESTORNADO", extrato.get(1).status());
    }

    @Test
    void datasIguaisSaoOrdenadasPeloId() {
        Instant mesmaData = Instant.parse("2026-11-01T10:00:00Z");
        UUID idMenor = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID idMaior = UUID.fromString("00000000-0000-0000-0000-000000000002");
        Membro ana = membroComNome(UUID.randomUUID(), "Ana");
        DespesaEntidade despesa = despesaEntidade(idMaior, ana, "10.00", StatusDespesa.ATIVA, mesmaData);
        PagamentoEntidade pagamento = pagamentoEntidade(idMenor, ana, "5.00", StatusPagamento.ATIVO, mesmaData);
        when(grupoRepositorio.findById(grupoId)).thenReturn(Optional.of(new GrupoEntidade()));
        when(participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId, registradorId)).thenReturn(true);
        when(pagamentoRepositorio.findByGrupoId(grupoId)).thenReturn(List.of(pagamento));
        when(despesaRepositorio.findByGrupoId(grupoId)).thenReturn(List.of(despesa));

        List<ExtratoDetalhe> extrato = servico.extrato(grupoId, registradorId);

        assertEquals(idMenor, extrato.get(0).id());
        assertEquals(idMaior, extrato.get(1).id());
    }


}