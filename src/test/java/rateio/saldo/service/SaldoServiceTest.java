package rateio.saldo.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rateio.despesa.domain.Despesa;
import rateio.despesa.entity.DespesaEntidade;
import rateio.despesa.entity.ParcelaEntidade;
import rateio.despesa.repository.ParcelaRepository;
import rateio.despesa.domain.StatusDespesa;
import rateio.grupo.entity.GrupoEntidade;
import rateio.grupo.entity.GrupoMembroEntidade;
import rateio.grupo.repository.GrupoMembroRepository;
import rateio.grupo.exception.GrupoNaoEncontradoException;
import rateio.grupo.repository.GrupoRepository;
import rateio.membro.entity.Membro;
import rateio.pagamento.entity.PagamentoEntidade;
import rateio.pagamento.repository.PagamentoRepository;
import rateio.pagamento.domain.StatusPagamento;
import rateio.saldo.dto.SaldoDetalhe;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SaldoServiceTest {

    @Mock
    private GrupoRepository grupoRepositorio;

    @Mock
    private GrupoMembroRepository participacaoRepositorio;

    @Mock
    private ParcelaRepository parcelaRepositorio;

    @Mock
    private PagamentoRepository pagamentoRepositorio;

    @InjectMocks
    private SaldoService servico;

    private Membro criarMembro(UUID id, String nome) {
        Membro membro = new Membro();
        membro.setId(id);
        membro.setNome(nome);
        return membro;
    }

    private GrupoMembroEntidade criarParticipacao(Membro membro) {
        GrupoMembroEntidade participacao = new GrupoMembroEntidade();
        participacao.setMembro(membro);
        return participacao;
    }

    private DespesaEntidade criarDespesaEntidade(Membro pagador, String valorTotal) {
        DespesaEntidade despesa = new DespesaEntidade();
        despesa.setId(UUID.randomUUID());
        despesa.setDescricao("Despesa");
        despesa.setValorTotal(new BigDecimal(valorTotal));
        despesa.setPagador(pagador);
        despesa.setEstrategia(Despesa.EstrategiaDivisao.IGUAL);
        despesa.setStatus(StatusDespesa.ATIVA);
        return despesa;
    }

    private ParcelaEntidade criarParcelaEntidade(DespesaEntidade despesa, Membro membro, String valor) {
        ParcelaEntidade parcela = new ParcelaEntidade();
        parcela.setDespesa(despesa);
        parcela.setMembro(membro);
        parcela.setValor(new BigDecimal(valor));
        return parcela;
    }

    private void configurarGrupoComMembros(UUID grupoId, Membro quemPede, Membro... membros) {
        when(grupoRepositorio.findById(grupoId)).thenReturn(Optional.of(new GrupoEntidade()));
        when(participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId, quemPede.getId())).thenReturn(true);
        List<GrupoMembroEntidade> participacoes = new ArrayList<>();
        for (Membro membro : membros) {
            participacoes.add(criarParticipacao(membro));
        }
        when(participacaoRepositorio.findByGrupoId(grupoId)).thenReturn(participacoes);
    }

    private PagamentoEntidade criarPagamentoEntidade(Membro de, Membro para, String valor) {
        PagamentoEntidade pagamento = new PagamentoEntidade();
        pagamento.setId(UUID.randomUUID());
        pagamento.setDe(de);
        pagamento.setPara(para);
        pagamento.setValor(new BigDecimal(valor));
        pagamento.setStatus(StatusPagamento.ATIVO);
        return pagamento;
    }

    @Test
    void rejeitaGrupoIdNulo() {
        assertThrows(IllegalArgumentException.class,
                () -> servico.consultar(null, UUID.randomUUID()));
    }

    @Test
    void rejeitaMembroIdNulo() {
        assertThrows(IllegalArgumentException.class,
                () -> servico.consultar(UUID.randomUUID(), null));
    }

    @Test
    void rejeitaGrupoNaoEncontrado() {
        UUID grupoId = UUID.randomUUID();
        when(grupoRepositorio.findById(grupoId)).thenReturn(Optional.empty());

        assertThrows(GrupoNaoEncontradoException.class,
                () -> servico.consultar(grupoId, UUID.randomUUID()));

        verify(participacaoRepositorio, never()).existsByGrupoIdAndMembroId(any(), any());
        verify(parcelaRepositorio, never()).findByDespesaGrupoIdAndDespesaStatus(any(), any());
    }

    @Test
    void rejeitaQuemPedeForaDoGrupo() {
        UUID grupoId = UUID.randomUUID();
        UUID membroId = UUID.randomUUID();
        when(grupoRepositorio.findById(grupoId)).thenReturn(Optional.of(new GrupoEntidade()));
        when(participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId, membroId)).thenReturn(false);

        assertThrows(GrupoNaoEncontradoException.class,
                () -> servico.consultar(grupoId, membroId));

        verify(participacaoRepositorio, never()).findByGrupoId(any());
        verify(parcelaRepositorio, never()).findByDespesaGrupoIdAndDespesaStatus(any(), any());
    }

    @Test
    void grupoSemDespesasDevolveSaldosZeradosEmOrdemAlfabetica() {
        UUID grupoId = UUID.randomUUID();
        Membro caio = criarMembro(UUID.randomUUID(), "Caio");
        Membro ana = criarMembro(UUID.randomUUID(), "Ana");
        Membro bia = criarMembro(UUID.randomUUID(), "Bia");
        configurarGrupoComMembros(grupoId, ana, caio, ana, bia);
        when(parcelaRepositorio.findByDespesaGrupoIdAndDespesaStatus(grupoId, StatusDespesa.ATIVA))
                .thenReturn(List.of());

        List<SaldoDetalhe> saldos = servico.consultar(grupoId, ana.getId());

        assertEquals(3, saldos.size());
        assertEquals("Ana", saldos.get(0).nome());
        assertEquals("Bia", saldos.get(1).nome());
        assertEquals("Caio", saldos.get(2).nome());
        for (SaldoDetalhe saldo : saldos) {
            assertEquals(new BigDecimal("0.00"), saldo.saldo());
        }
    }

    @Test
    void despesaDivididaDeixaPagadorPositivoEOsOutrosNegativos() {
        UUID grupoId = UUID.randomUUID();
        Membro ana = criarMembro(UUID.randomUUID(), "Ana");
        Membro bia = criarMembro(UUID.randomUUID(), "Bia");
        Membro caio = criarMembro(UUID.randomUUID(), "Caio");
        DespesaEntidade jantar = criarDespesaEntidade(ana, "100.00");
        configurarGrupoComMembros(grupoId, ana, ana, bia, caio);
        when(parcelaRepositorio.findByDespesaGrupoIdAndDespesaStatus(grupoId, StatusDespesa.ATIVA))
                .thenReturn(List.of(
                        criarParcelaEntidade(jantar, ana, "33.34"),
                        criarParcelaEntidade(jantar, bia, "33.33"),
                        criarParcelaEntidade(jantar, caio, "33.33")));

        List<SaldoDetalhe> saldos = servico.consultar(grupoId, ana.getId());

        assertEquals(new BigDecimal("66.66"), saldos.get(0).saldo());
        assertEquals(new BigDecimal("-33.33"), saldos.get(1).saldo());
        assertEquals(new BigDecimal("-33.33"), saldos.get(2).saldo());
    }

    @Test
    void somaDuasDespesasNoMesmoMembro() {
        UUID grupoId = UUID.randomUUID();
        Membro ana = criarMembro(UUID.randomUUID(), "Ana");
        Membro bia = criarMembro(UUID.randomUUID(), "Bia");
        DespesaEntidade jantar = criarDespesaEntidade(ana, "100.00");
        DespesaEntidade mercado = criarDespesaEntidade(bia, "60.00");
        configurarGrupoComMembros(grupoId, ana, ana, bia);
        when(parcelaRepositorio.findByDespesaGrupoIdAndDespesaStatus(grupoId, StatusDespesa.ATIVA))
                .thenReturn(List.of(
                        criarParcelaEntidade(jantar, ana, "50.00"),
                        criarParcelaEntidade(jantar, bia, "50.00"),
                        criarParcelaEntidade(mercado, ana, "30.00"),
                        criarParcelaEntidade(mercado, bia, "30.00")));

        List<SaldoDetalhe> saldos = servico.consultar(grupoId, ana.getId());

        assertEquals(2, saldos.size());
        assertEquals("Ana", saldos.get(0).nome());
        assertEquals(new BigDecimal("20.00"), saldos.get(0).saldo());
        assertEquals("Bia", saldos.get(1).nome());
        assertEquals(new BigDecimal("-20.00"), saldos.get(1).saldo());
    }

    @Test
    void somaDosSaldosEhSempreZero() {
        UUID grupoId = UUID.randomUUID();
        Membro ana = criarMembro(UUID.randomUUID(), "Ana");
        Membro bia = criarMembro(UUID.randomUUID(), "Bia");
        Membro caio = criarMembro(UUID.randomUUID(), "Caio");
        DespesaEntidade jantar = criarDespesaEntidade(ana, "100.00");
        DespesaEntidade mercado = criarDespesaEntidade(caio, "45.50");
        configurarGrupoComMembros(grupoId, ana, ana, bia, caio);
        when(parcelaRepositorio.findByDespesaGrupoIdAndDespesaStatus(grupoId, StatusDespesa.ATIVA))
                .thenReturn(List.of(
                        criarParcelaEntidade(jantar, ana, "33.34"),
                        criarParcelaEntidade(jantar, bia, "33.33"),
                        criarParcelaEntidade(jantar, caio, "33.33"),
                        criarParcelaEntidade(mercado, ana, "15.17"),
                        criarParcelaEntidade(mercado, bia, "15.17"),
                        criarParcelaEntidade(mercado, caio, "15.16")));

        List<SaldoDetalhe> saldos = servico.consultar(grupoId, ana.getId());

        BigDecimal soma = BigDecimal.ZERO;
        for (SaldoDetalhe saldo : saldos) {
            soma = soma.add(saldo.saldo());
        }
        assertEquals(0, soma.compareTo(BigDecimal.ZERO));
    }

    @Test
    void nomesIguaisSaoOrdenadosPeloId() {
        UUID grupoId = UUID.randomUUID();
        UUID idMenor = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID idMaior = UUID.fromString("00000000-0000-0000-0000-000000000002");
        Membro primeiro = criarMembro(idMenor, "Ana");
        Membro segundo = criarMembro(idMaior, "Ana");
        configurarGrupoComMembros(grupoId, primeiro, segundo, primeiro);
        when(parcelaRepositorio.findByDespesaGrupoIdAndDespesaStatus(grupoId, StatusDespesa.ATIVA))
                .thenReturn(List.of());

        List<SaldoDetalhe> saldos = servico.consultar(grupoId, idMenor);

        assertEquals(idMenor, saldos.get(0).membroId());
        assertEquals(idMaior, saldos.get(1).membroId());
    }

    @Test
    void pagamentoAtivoReduzOsSaldos() {
        UUID grupoId = UUID.randomUUID();
        Membro ana = criarMembro(UUID.randomUUID(), "Ana");
        Membro bia = criarMembro(UUID.randomUUID(), "Bia");
        DespesaEntidade jantar = criarDespesaEntidade(ana, "100.00");
        configurarGrupoComMembros(grupoId, ana, ana, bia);
        when(parcelaRepositorio.findByDespesaGrupoIdAndDespesaStatus(grupoId, StatusDespesa.ATIVA))
                .thenReturn(List.of(
                        criarParcelaEntidade(jantar, ana, "50.00"),
                        criarParcelaEntidade(jantar, bia, "50.00")));
        when(pagamentoRepositorio.findByGrupoIdAndStatus(grupoId, StatusPagamento.ATIVO))
                .thenReturn(List.of(criarPagamentoEntidade(bia, ana, "20.00")));

        List<SaldoDetalhe> saldos = servico.consultar(grupoId, ana.getId());

        assertEquals(new BigDecimal("30.00"), saldos.get(0).saldo());
        assertEquals(new BigDecimal("-30.00"), saldos.get(1).saldo());
    }

    @Test
    void pagamentoQuitaADividaEZeraOsSaldos() {
        UUID grupoId = UUID.randomUUID();
        Membro ana = criarMembro(UUID.randomUUID(), "Ana");
        Membro bia = criarMembro(UUID.randomUUID(), "Bia");
        DespesaEntidade jantar = criarDespesaEntidade(ana, "100.00");
        configurarGrupoComMembros(grupoId, ana, ana, bia);
        when(parcelaRepositorio.findByDespesaGrupoIdAndDespesaStatus(grupoId, StatusDespesa.ATIVA))
                .thenReturn(List.of(
                        criarParcelaEntidade(jantar, ana, "50.00"),
                        criarParcelaEntidade(jantar, bia, "50.00")));
        when(pagamentoRepositorio.findByGrupoIdAndStatus(grupoId, StatusPagamento.ATIVO))
                .thenReturn(List.of(criarPagamentoEntidade(bia, ana, "50.00")));

        List<SaldoDetalhe> saldos = servico.consultar(grupoId, ana.getId());

        assertEquals(new BigDecimal("0.00"), saldos.get(0).saldo());
        assertEquals(new BigDecimal("0.00"), saldos.get(1).saldo());
    }
}