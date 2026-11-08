package rateio.despesa;

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
import rateio.membro.MembroRepository;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DespesaServiceTest {

    @Mock
    private DespesaRepository despesaRepositorio;

    @Mock
    private ParcelaRepository parcelaRepositorio;

    @Mock
    private GrupoRepository grupoRepositorio;

    @Mock
    private GrupoMembroRepository participacaoRepositorio;

    @Mock
    private MembroRepository membroRepositorio;

    @InjectMocks
    private DespesaService servico;

    @Captor
    private ArgumentCaptor<DespesaEntidade> captorDespesa;

    @Captor
    private ArgumentCaptor<List<ParcelaEntidade>> captorParcelas;

    private final UUID registradorId = UUID.randomUUID();
    private final UUID grupoId = UUID.randomUUID();
    private final UUID pagadorId = UUID.randomUUID();
    private final UUID outroId = UUID.randomUUID();
    private final UUID terceiroId = UUID.randomUUID();

    private Membro membro(UUID id) {
        Membro membro = new Membro();
        membro.setId(id);
        return membro;
    }

    private ParticipanteRequest igual(UUID id) {
        return new ParticipanteRequest(id, null, null);
    }

    private ParticipanteRequest comPeso(UUID id, String peso) {
        return new ParticipanteRequest(id, peso == null ? null : new BigDecimal(peso), null);
    }

    private ParticipanteRequest comValor(UUID id, String valor) {
        return new ParticipanteRequest(id, null, valor == null ? null : new BigDecimal(valor));
    }

    @Test
    void rejeitaRegistradorIdNulo() {
        assertThrows(IllegalArgumentException.class,
                () -> servico.criar(null, grupoId, "Jantar", new BigDecimal("100.00"), pagadorId,
                        Despesa.EstrategiaDivisao.IGUAL, List.of(igual(pagadorId))));
    }

    @Test
    void rejeitaGrupoIdNulo() {
        assertThrows(IllegalArgumentException.class,
                () -> servico.criar(registradorId, null, "Jantar", new BigDecimal("100.00"), pagadorId,
                        Despesa.EstrategiaDivisao.IGUAL, List.of(igual(pagadorId))));
    }

    @Test
    void rejeitaDescricaoNula() {
        assertThrows(IllegalArgumentException.class,
                () -> servico.criar(registradorId, grupoId, null, new BigDecimal("100.00"), pagadorId,
                        Despesa.EstrategiaDivisao.IGUAL, List.of(igual(pagadorId))));
    }

    @Test
    void rejeitaDescricaoEmBranco() {
        assertThrows(IllegalArgumentException.class,
                () -> servico.criar(registradorId, grupoId, "   ", new BigDecimal("100.00"), pagadorId,
                        Despesa.EstrategiaDivisao.IGUAL, List.of(igual(pagadorId))));
    }

    @Test
    void rejeitaValorNulo() {
        assertThrows(IllegalArgumentException.class,
                () -> servico.criar(registradorId, grupoId, "Jantar", null, pagadorId,
                        Despesa.EstrategiaDivisao.IGUAL, List.of(igual(pagadorId))));
    }

    @Test
    void rejeitaValorZero() {
        assertThrows(IllegalArgumentException.class,
                () -> servico.criar(registradorId, grupoId, "Jantar", BigDecimal.ZERO, pagadorId,
                        Despesa.EstrategiaDivisao.IGUAL, List.of(igual(pagadorId))));
    }

    @Test
    void rejeitaValorNegativo() {
        assertThrows(IllegalArgumentException.class,
                () -> servico.criar(registradorId, grupoId, "Jantar", new BigDecimal("-5.00"), pagadorId,
                        Despesa.EstrategiaDivisao.IGUAL, List.of(igual(pagadorId))));
    }

    @Test
    void rejeitaValorComTresCasasDecimais() {
        assertThrows(IllegalArgumentException.class,
                () -> servico.criar(registradorId, grupoId, "Jantar", new BigDecimal("10.005"), pagadorId,
                        Despesa.EstrategiaDivisao.IGUAL, List.of(igual(pagadorId))));
    }

    @Test
    void rejeitaPagadorIdNulo() {
        assertThrows(IllegalArgumentException.class,
                () -> servico.criar(registradorId, grupoId, "Jantar", new BigDecimal("100.00"), null,
                        Despesa.EstrategiaDivisao.IGUAL, List.of(igual(pagadorId))));
    }

    @Test
    void rejeitaEstrategiaNula() {
        assertThrows(IllegalArgumentException.class,
                () -> servico.criar(registradorId, grupoId, "Jantar", new BigDecimal("100.00"), pagadorId,
                        null, List.of(igual(pagadorId))));
    }

    @Test
    void rejeitaParticipantesNulos() {
        assertThrows(IllegalArgumentException.class,
                () -> servico.criar(registradorId, grupoId, "Jantar", new BigDecimal("100.00"), pagadorId,
                        Despesa.EstrategiaDivisao.IGUAL, null));
    }

    @Test
    void rejeitaParticipantesVazios() {
        assertThrows(IllegalArgumentException.class,
                () -> servico.criar(registradorId, grupoId, "Jantar", new BigDecimal("100.00"), pagadorId,
                        Despesa.EstrategiaDivisao.IGUAL, List.of()));
    }

    @Test
    void rejeitaGrupoNaoEncontrado() {
        when(grupoRepositorio.findById(grupoId)).thenReturn(Optional.empty());

        assertThrows(GrupoNaoEncontradoException.class,
                () -> servico.criar(registradorId, grupoId, "Jantar", new BigDecimal("100.00"), pagadorId,
                        Despesa.EstrategiaDivisao.IGUAL, List.of(igual(pagadorId))));

        verify(despesaRepositorio, never()).save(any());
    }

    @Test
    void rejeitaRegistradorForaDoGrupo() {
        when(grupoRepositorio.findById(grupoId)).thenReturn(Optional.of(new GrupoEntidade()));
        when(participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId, registradorId)).thenReturn(false);

        assertThrows(GrupoNaoEncontradoException.class,
                () -> servico.criar(registradorId, grupoId, "Jantar", new BigDecimal("100.00"), pagadorId,
                        Despesa.EstrategiaDivisao.IGUAL, List.of(igual(pagadorId))));

        verify(despesaRepositorio, never()).save(any());
    }

    @Test
    void rejeitaPagadorForaDoGrupo() {
        when(grupoRepositorio.findById(grupoId)).thenReturn(Optional.of(new GrupoEntidade()));
        when(participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId, registradorId)).thenReturn(true);
        when(participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId, pagadorId)).thenReturn(false);

        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> servico.criar(registradorId, grupoId, "Jantar", new BigDecimal("100.00"), pagadorId,
                        Despesa.EstrategiaDivisao.IGUAL, List.of(igual(pagadorId))));

        assertEquals("Pagador fora do grupo", erro.getMessage());
        verify(despesaRepositorio, never()).save(any());
    }

    @Test
    void rejeitaParticipanteNulo() {
        when(grupoRepositorio.findById(grupoId)).thenReturn(Optional.of(new GrupoEntidade()));
        when(participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId, registradorId)).thenReturn(true);
        when(participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId, pagadorId)).thenReturn(true);
        List<ParticipanteRequest> participantes = Arrays.asList((ParticipanteRequest) null);

        assertThrows(IllegalArgumentException.class,
                () -> servico.criar(registradorId, grupoId, "Jantar", new BigDecimal("100.00"), pagadorId,
                        Despesa.EstrategiaDivisao.IGUAL, participantes));

        verify(despesaRepositorio, never()).save(any());
    }

    @Test
    void rejeitaParticipanteSemMembroId() {
        when(grupoRepositorio.findById(grupoId)).thenReturn(Optional.of(new GrupoEntidade()));
        when(participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId, registradorId)).thenReturn(true);
        when(participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId, pagadorId)).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> servico.criar(registradorId, grupoId, "Jantar", new BigDecimal("100.00"), pagadorId,
                        Despesa.EstrategiaDivisao.IGUAL, List.of(igual(null))));

        verify(despesaRepositorio, never()).save(any());
    }

    @Test
    void rejeitaParticipanteRepetido() {
        when(grupoRepositorio.findById(grupoId)).thenReturn(Optional.of(new GrupoEntidade()));
        when(participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId, registradorId)).thenReturn(true);
        when(participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId, pagadorId)).thenReturn(true);
        when(participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId, outroId)).thenReturn(true);

        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> servico.criar(registradorId, grupoId, "Jantar", new BigDecimal("100.00"), pagadorId,
                        Despesa.EstrategiaDivisao.IGUAL, List.of(igual(outroId), igual(outroId))));

        assertEquals("Participante repetido", erro.getMessage());
        verify(despesaRepositorio, never()).save(any());
    }

    @Test
    void rejeitaParticipanteForaDoGrupo() {
        when(grupoRepositorio.findById(grupoId)).thenReturn(Optional.of(new GrupoEntidade()));
        when(participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId, registradorId)).thenReturn(true);
        when(participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId, pagadorId)).thenReturn(true);
        when(participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId, outroId)).thenReturn(false);

        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> servico.criar(registradorId, grupoId, "Jantar", new BigDecimal("100.00"), pagadorId,
                        Despesa.EstrategiaDivisao.IGUAL, List.of(igual(pagadorId), igual(outroId))));

        assertEquals("Participante fora do grupo", erro.getMessage());
        verify(despesaRepositorio, never()).save(any());
    }

    @Test
    void rejeitaPesoNulo() {
        when(grupoRepositorio.findById(grupoId)).thenReturn(Optional.of(new GrupoEntidade()));
        when(participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId, registradorId)).thenReturn(true);
        when(participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId, pagadorId)).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> servico.criar(registradorId, grupoId, "Jantar", new BigDecimal("100.00"), pagadorId,
                        Despesa.EstrategiaDivisao.POR_PESO, List.of(comPeso(pagadorId, null))));

        verify(despesaRepositorio, never()).save(any());
    }

    @Test
    void rejeitaPesoZero() {
        when(grupoRepositorio.findById(grupoId)).thenReturn(Optional.of(new GrupoEntidade()));
        when(participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId, registradorId)).thenReturn(true);
        when(participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId, pagadorId)).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> servico.criar(registradorId, grupoId, "Jantar", new BigDecimal("100.00"), pagadorId,
                        Despesa.EstrategiaDivisao.POR_PESO, List.of(comPeso(pagadorId, "0"))));

        verify(despesaRepositorio, never()).save(any());
    }

    @Test
    void rejeitaValorFixoNulo() {
        when(grupoRepositorio.findById(grupoId)).thenReturn(Optional.of(new GrupoEntidade()));
        when(participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId, registradorId)).thenReturn(true);
        when(participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId, pagadorId)).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> servico.criar(registradorId, grupoId, "Jantar", new BigDecimal("100.00"), pagadorId,
                        Despesa.EstrategiaDivisao.VALORES_FIXOS, List.of(comValor(pagadorId, null))));

        verify(despesaRepositorio, never()).save(any());
    }

    @Test
    void rejeitaValorFixoZero() {
        when(grupoRepositorio.findById(grupoId)).thenReturn(Optional.of(new GrupoEntidade()));
        when(participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId, registradorId)).thenReturn(true);
        when(participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId, pagadorId)).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> servico.criar(registradorId, grupoId, "Jantar", new BigDecimal("100.00"), pagadorId,
                        Despesa.EstrategiaDivisao.VALORES_FIXOS, List.of(comValor(pagadorId, "0.00"))));

        verify(despesaRepositorio, never()).save(any());
    }

    @Test
    void rejeitaValorFixoComTresCasasDecimais() {
        when(grupoRepositorio.findById(grupoId)).thenReturn(Optional.of(new GrupoEntidade()));
        when(participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId, registradorId)).thenReturn(true);
        when(participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId, pagadorId)).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> servico.criar(registradorId, grupoId, "Jantar", new BigDecimal("100.00"), pagadorId,
                        Despesa.EstrategiaDivisao.VALORES_FIXOS, List.of(comValor(pagadorId, "10.005"))));

        verify(despesaRepositorio, never()).save(any());
    }

    @Test
    void rejeitaSomaDasParcelasDiferenteDoTotal() {
        when(grupoRepositorio.findById(grupoId)).thenReturn(Optional.of(new GrupoEntidade()));
        when(participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId, registradorId)).thenReturn(true);
        when(participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId, pagadorId)).thenReturn(true);
        when(participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId, outroId)).thenReturn(true);

        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> servico.criar(registradorId, grupoId, "Jantar", new BigDecimal("100.00"), pagadorId,
                        Despesa.EstrategiaDivisao.VALORES_FIXOS,
                        List.of(comValor(pagadorId, "30.00"), comValor(outroId, "60.00"))));

        assertEquals("Soma das parcelas diferente do total", erro.getMessage());
        verify(despesaRepositorio, never()).save(any());
        verify(parcelaRepositorio, never()).saveAll(any());
    }

    @Test
    void registraDespesaIgual() {
        GrupoEntidade grupo = new GrupoEntidade();
        Membro registrador = membro(registradorId);
        Membro pagador = membro(pagadorId);
        Membro outro = membro(outroId);
        Membro terceiro = membro(terceiroId);
        when(grupoRepositorio.findById(grupoId)).thenReturn(Optional.of(grupo));
        when(participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId, registradorId)).thenReturn(true);
        when(participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId, pagadorId)).thenReturn(true);
        when(participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId, outroId)).thenReturn(true);
        when(participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId, terceiroId)).thenReturn(true);
        when(membroRepositorio.findById(registradorId)).thenReturn(Optional.of(registrador));
        when(membroRepositorio.findById(pagadorId)).thenReturn(Optional.of(pagador));
        when(membroRepositorio.findById(outroId)).thenReturn(Optional.of(outro));
        when(membroRepositorio.findById(terceiroId)).thenReturn(Optional.of(terceiro));

        DespesaDetalhe detalhe = servico.criar(registradorId, grupoId, "Jantar", new BigDecimal("100.00"), pagadorId,
                Despesa.EstrategiaDivisao.IGUAL,
                List.of(igual(pagadorId), igual(outroId), igual(terceiroId)));

        verify(despesaRepositorio).save(captorDespesa.capture());
        DespesaEntidade despesa = captorDespesa.getValue();
        assertSame(grupo, despesa.getGrupo());
        assertSame(pagador, despesa.getPagador());
        assertSame(registrador, despesa.getRegistrador());
        assertEquals("Jantar", despesa.getDescricao());
        assertEquals(new BigDecimal("100.00"), despesa.getValorTotal());
        assertEquals(Despesa.EstrategiaDivisao.IGUAL, despesa.getEstrategia());
        assertEquals(StatusDespesa.ATIVA, despesa.getStatus());
        assertNotNull(despesa.getCriadaEm());

        verify(parcelaRepositorio).saveAll(captorParcelas.capture());
        List<ParcelaEntidade> parcelas = captorParcelas.getValue();
        assertEquals(3, parcelas.size());
        assertEquals(new BigDecimal("33.34"), parcelas.get(0).getValor());
        assertEquals(new BigDecimal("33.33"), parcelas.get(1).getValor());
        assertEquals(new BigDecimal("33.33"), parcelas.get(2).getValor());
        assertEquals(List.of(pagador, outro, terceiro),
                parcelas.stream().map(ParcelaEntidade::getMembro).toList());
        assertTrue(parcelas.stream().allMatch(p -> p.getDespesa() == despesa));

        assertEquals(pagadorId, detalhe.pagadorId());
        assertEquals(StatusDespesa.ATIVA, detalhe.status());
        assertEquals(3, detalhe.parcelas().size());
        assertEquals(new BigDecimal("33.34"), detalhe.parcelas().get(0).valor());
    }

    @Test
    void registraDespesaPorPeso() {
        GrupoEntidade grupo = new GrupoEntidade();
        Membro registrador = membro(registradorId);
        Membro pagador = membro(pagadorId);
        Membro outro = membro(outroId);
        when(grupoRepositorio.findById(grupoId)).thenReturn(Optional.of(grupo));
        when(participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId, registradorId)).thenReturn(true);
        when(participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId, pagadorId)).thenReturn(true);
        when(participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId, outroId)).thenReturn(true);
        when(membroRepositorio.findById(registradorId)).thenReturn(Optional.of(registrador));
        when(membroRepositorio.findById(pagadorId)).thenReturn(Optional.of(pagador));
        when(membroRepositorio.findById(outroId)).thenReturn(Optional.of(outro));

        DespesaDetalhe detalhe = servico.criar(registradorId, grupoId, "Aluguel", new BigDecimal("100.00"), pagadorId,
                Despesa.EstrategiaDivisao.POR_PESO,
                List.of(comPeso(pagadorId, "1"), comPeso(outroId, "2")));

        verify(parcelaRepositorio).saveAll(captorParcelas.capture());
        List<ParcelaEntidade> parcelas = captorParcelas.getValue();
        assertEquals(2, parcelas.size());
        assertEquals(new BigDecimal("33.33"), parcelas.get(0).getValor());
        assertEquals(new BigDecimal("66.67"), parcelas.get(1).getValor());
        assertEquals(Despesa.EstrategiaDivisao.POR_PESO, detalhe.estrategia());
    }

    @Test
    void registraDespesaComValoresFixos() {
        GrupoEntidade grupo = new GrupoEntidade();
        Membro registrador = membro(registradorId);
        Membro pagador = membro(pagadorId);
        Membro outro = membro(outroId);
        when(grupoRepositorio.findById(grupoId)).thenReturn(Optional.of(grupo));
        when(participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId, registradorId)).thenReturn(true);
        when(participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId, pagadorId)).thenReturn(true);
        when(participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId, outroId)).thenReturn(true);
        when(membroRepositorio.findById(registradorId)).thenReturn(Optional.of(registrador));
        when(membroRepositorio.findById(pagadorId)).thenReturn(Optional.of(pagador));
        when(membroRepositorio.findById(outroId)).thenReturn(Optional.of(outro));

        DespesaDetalhe detalhe = servico.criar(registradorId, grupoId, "Mercado", new BigDecimal("100.00"), pagadorId,
                Despesa.EstrategiaDivisao.VALORES_FIXOS,
                List.of(comValor(pagadorId, "30.00"), comValor(outroId, "70.00")));

        verify(parcelaRepositorio).saveAll(captorParcelas.capture());
        List<ParcelaEntidade> parcelas = captorParcelas.getValue();
        assertEquals(2, parcelas.size());
        assertEquals(new BigDecimal("30.00"), parcelas.get(0).getValor());
        assertEquals(new BigDecimal("70.00"), parcelas.get(1).getValor());
        assertEquals(Despesa.EstrategiaDivisao.VALORES_FIXOS, detalhe.estrategia());
    }
    @Test
    void rejeitaMembroIdNuloAoListar() {
        assertThrows(IllegalArgumentException.class,
                () -> servico.listar(null, grupoId));
    }

    @Test
    void rejeitaGrupoIdNuloAoListar() {
        assertThrows(IllegalArgumentException.class,
                () -> servico.listar(registradorId, null));
    }

    @Test
    void rejeitaGrupoNaoEncontradoAoListar() {
        when(grupoRepositorio.findById(grupoId)).thenReturn(Optional.empty());

        assertThrows(GrupoNaoEncontradoException.class,
                () -> servico.listar(registradorId, grupoId));

        verify(despesaRepositorio, never()).findByGrupoId(any());
    }

    @Test
    void rejeitaQuemPedeForaDoGrupoAoListar() {
        when(grupoRepositorio.findById(grupoId)).thenReturn(Optional.of(new GrupoEntidade()));
        when(participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId, registradorId)).thenReturn(false);

        assertThrows(GrupoNaoEncontradoException.class,
                () -> servico.listar(registradorId, grupoId));

        verify(despesaRepositorio, never()).findByGrupoId(any());
    }

    @Test
    void devolveListaVaziaQuandoGrupoNaoTemDespesas() {
        when(grupoRepositorio.findById(grupoId)).thenReturn(Optional.of(new GrupoEntidade()));
        when(participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId, registradorId)).thenReturn(true);
        when(despesaRepositorio.findByGrupoId(grupoId)).thenReturn(List.of());

        List<DespesaResumo> resumos = servico.listar(registradorId, grupoId);

        assertTrue(resumos.isEmpty());
    }

    @Test
    void listaDespesasDoGrupo() {
        Membro pagador = membro(pagadorId);

        DespesaEntidade jantar = new DespesaEntidade();
        jantar.setId(UUID.randomUUID());
        jantar.setDescricao("Jantar");
        jantar.setValorTotal(new BigDecimal("100.00"));
        jantar.setPagador(pagador);
        jantar.setStatus(StatusDespesa.ATIVA);
        jantar.setCriadaEm(Instant.parse("2026-11-04T20:15:00Z"));

        DespesaEntidade mercado = new DespesaEntidade();
        mercado.setId(UUID.randomUUID());
        mercado.setDescricao("Mercado");
        mercado.setValorTotal(new BigDecimal("50.00"));
        mercado.setPagador(pagador);
        mercado.setStatus(StatusDespesa.ESTORNADA);
        mercado.setCriadaEm(Instant.parse("2026-11-05T10:00:00Z"));

        when(grupoRepositorio.findById(grupoId)).thenReturn(Optional.of(new GrupoEntidade()));
        when(participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId, registradorId)).thenReturn(true);
        when(despesaRepositorio.findByGrupoId(grupoId)).thenReturn(List.of(jantar, mercado));

        List<DespesaResumo> resumos = servico.listar(registradorId, grupoId);

        assertEquals(2, resumos.size());
        assertEquals(jantar.getId(), resumos.get(0).id());
        assertEquals("Jantar", resumos.get(0).descricao());
        assertEquals(new BigDecimal("100.00"), resumos.get(0).valorTotal());
        assertEquals(pagadorId, resumos.get(0).pagadorId());
        assertEquals(StatusDespesa.ATIVA, resumos.get(0).status());
        assertEquals(Instant.parse("2026-11-04T20:15:00Z"), resumos.get(0).data());
        assertEquals(mercado.getId(), resumos.get(1).id());
        assertEquals(StatusDespesa.ESTORNADA, resumos.get(1).status());
    }

    @Test
    void rejeitaMembroIdNuloAoEstornar() {
        assertThrows(IllegalArgumentException.class,
                () -> servico.estornar(grupoId, outroId, null));
    }

    @Test
    void rejeitaGrupoIdNuloAoEstornar() {
        assertThrows(IllegalArgumentException.class,
                () -> servico.estornar(null, outroId, registradorId ));
    }

    @Test
    void rejeitaDespesaIdNuloAoEstornar() {
        assertThrows(IllegalArgumentException.class,
                () -> servico.estornar(grupoId, null, registradorId ));
    }

    @Test
    void rejeitaGrupoNaoEncontradoAoEstornar() {
        when(grupoRepositorio.findById(grupoId)).thenReturn(Optional.empty());

        assertThrows(DespesaNaoEncontradaException.class,
                () -> servico.estornar(grupoId, outroId, registradorId));

        verify(despesaRepositorio, never()).findByIdAndGrupoId(any(), any());
        verify(participacaoRepositorio, never()).existsByGrupoIdAndMembroId(any(), any());
    }

    @Test
    void rejeitaQuemPedeForaDoGrupoAoEstornar() {
        when(grupoRepositorio.findById(grupoId)).thenReturn(Optional.of(new GrupoEntidade()));
        when(participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId, registradorId)).thenReturn(false);

        assertThrows(DespesaNaoEncontradaException.class,
                () -> servico.estornar(grupoId, outroId, registradorId));

        verify(despesaRepositorio, never()).findByIdAndGrupoId(any(), any());
    }

    @Test
    void rejeitaDespesaNaoEncontradaAoEstornar() {
        when(grupoRepositorio.findById(grupoId)).thenReturn(Optional.of(new GrupoEntidade()));
        when(participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId, registradorId)).thenReturn(true);
        when(despesaRepositorio.findByIdAndGrupoId(outroId, grupoId)).thenReturn(Optional.empty());

        assertThrows(DespesaNaoEncontradaException.class,
                () -> servico.estornar(grupoId, outroId, registradorId));

        verify(despesaRepositorio, never()).save(any());
    }

    @Test
    void rejeitaDespesaJaEstornada() {
        DespesaEntidade despesa = new DespesaEntidade();
        despesa.setId(outroId);
        despesa.setStatus(StatusDespesa.ESTORNADA);
        when(grupoRepositorio.findById(grupoId)).thenReturn(Optional.of(new GrupoEntidade()));
        when(participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId, registradorId)).thenReturn(true);
        when(despesaRepositorio.findByIdAndGrupoId(outroId, grupoId)).thenReturn(Optional.of(despesa));

        assertThrows(DespesaJaEstornadaException.class,
                () -> servico.estornar(grupoId, outroId, registradorId));

        verify(despesaRepositorio, never()).save(any());
    }

    @Test
    void estornaDespesa() {
        UUID despesaId = UUID.randomUUID();
        Membro pagador = membro(pagadorId);
        Membro outro = membro(outroId);

        DespesaEntidade despesa = new DespesaEntidade();
        despesa.setId(despesaId);
        despesa.setPagador(pagador);
        despesa.setDescricao("Jantar");
        despesa.setValorTotal(new BigDecimal("100.00"));
        despesa.setEstrategia(Despesa.EstrategiaDivisao.IGUAL);
        despesa.setStatus(StatusDespesa.ATIVA);

        ParcelaEntidade parcelaPagador = new ParcelaEntidade();
        parcelaPagador.setMembro(pagador);
        parcelaPagador.setValor(new BigDecimal("50.00"));
        ParcelaEntidade parcelaOutro = new ParcelaEntidade();
        parcelaOutro.setMembro(outro);
        parcelaOutro.setValor(new BigDecimal("50.00"));

        when(grupoRepositorio.findById(grupoId)).thenReturn(Optional.of(new GrupoEntidade()));
        when(participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId, registradorId)).thenReturn(true);
        when(despesaRepositorio.findByIdAndGrupoId(despesaId, grupoId)).thenReturn(Optional.of(despesa));
        when(parcelaRepositorio.findByDespesaId(despesaId)).thenReturn(List.of(parcelaPagador, parcelaOutro));

        EstornoDetalhe detalhe = servico.estornar(grupoId, despesaId, registradorId);

        assertEquals(StatusDespesa.ESTORNADA, despesa.getStatus());
        assertNotNull(despesa.getEstornadaEm());
        verify(despesaRepositorio).save(despesa);
        assertEquals(despesaId, detalhe.id());
        assertEquals(StatusDespesa.ESTORNADA, detalhe.status());
    }


}