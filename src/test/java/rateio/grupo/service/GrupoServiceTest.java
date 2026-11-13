package rateio.grupo.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rateio.despesa.repository.DespesaRepository;
import rateio.grupo.dto.GrupoDetalhe;
import rateio.grupo.entity.GrupoEntidade;
import rateio.grupo.entity.GrupoMembroEntidade;
import rateio.grupo.exception.GrupoComDespesasException;
import rateio.grupo.exception.GrupoComPoucosMembrosException;
import rateio.grupo.exception.GrupoNaoEncontradoException;
import rateio.grupo.exception.MembroRepetidoException;
import rateio.grupo.repository.GrupoMembroRepository;
import rateio.grupo.repository.GrupoRepository;
import rateio.membro.entity.Membro;
import rateio.membro.exception.MembroNaoEncontradoException;
import rateio.membro.repository.MembroRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GrupoServiceTest {

    @Mock
    private GrupoRepository grupoRepositorio;

    @Mock
    private GrupoMembroRepository participacaoRepositorio;

    @Mock
    private MembroRepository membroRepositorio;

    @Mock
    private DespesaRepository despesaRepositorio;

    @Captor
    private ArgumentCaptor<List<GrupoMembroEntidade>> captor;

    private GrupoService servico;

    @BeforeEach
    void montarServico() {
        servico = new GrupoService(grupoRepositorio, participacaoRepositorio, membroRepositorio,
                despesaRepositorio, new AcessoAoGrupo(grupoRepositorio, participacaoRepositorio));
    }

    private final UUID criadorId = UUID.randomUUID();

    @Test
    void rejeitaCriarGrupoSemIdCriador(){
        assertThrows(IllegalArgumentException.class,
                () -> servico.criar(null, "Viagem", List.of("maria@x.com", "joao@x.com")));
    }

    @Test
    void rejeitaCriarGrupoComNomeNulo(){
        assertThrows(IllegalArgumentException.class,
                () -> servico.criar(criadorId, null, List.of("maria@x.com", "joao@x.com")));
    }

    @Test
    void rejeitaCriarGrupoComNomeEmBranco(){
        assertThrows(IllegalArgumentException.class,
                () -> servico.criar(criadorId, "", List.of("maria@x.com", "joao@x.com")));
    }

    @Test
    void rejeitaCriarGrupoComListaEmailsNula(){
        assertThrows(IllegalArgumentException.class,
                () -> servico.criar(criadorId, "Viagem", null));
    }

    @Test
    void rejeitaListaDeEmailsVazia() {
        assertThrows(IllegalArgumentException.class,
                () -> servico.criar(criadorId, "Viagem", List.of()));
    }

    @Test
    void rejeitaEmailEmBranco() {
        Membro criador = new Membro();
        criador.setId(criadorId);
        criador.setEmail("ana@x.com");
        when(membroRepositorio.findById(criadorId)).thenReturn(Optional.of(criador));

        assertThrows(IllegalArgumentException.class,
                () -> servico.criar(criadorId, "Viagem", List.of("")));
    }

    @Test
    void rejeitaCriadorNaoCadastrado() {
        when(membroRepositorio.findById(criadorId)).thenReturn(Optional.empty());

        assertThrows(MembroNaoEncontradoException.class,
                () -> servico.criar(criadorId, "Viagem", List.of("maria@x.com")));

        verify(grupoRepositorio, never()).save(any());
    }

    @Test
    void rejeitaEmailDoCriadorNaLista(){
        Membro criador = new Membro();
        criador.setId(criadorId);
        criador.setEmail("ana@x.com");

        when(membroRepositorio.findById(criadorId)).thenReturn(Optional.of(criador));

        assertThrows(MembroRepetidoException.class,
                () -> servico.criar(criadorId, "Viagem", List.of("ana@x.com")));

        verify(grupoRepositorio, never()).save(any());
    }

    @Test
    void rejeitaEmailRepetidoNaLista(){
        Membro criador = new Membro();
        criador.setId(criadorId);
        criador.setEmail("ana@x.com");

        when(membroRepositorio.findById(criadorId)).thenReturn(Optional.of(criador));

        assertThrows(MembroRepetidoException.class,
                () -> servico.criar(criadorId, "Viagem", List.of("maria@x.com", "maria@x.com")));

        verify(grupoRepositorio, never()).save(any());
    }

    @Test
    void rejeitaEmailSemCadastro() {
        Membro criador = new Membro();
        criador.setId(criadorId);
        criador.setEmail("ana@x.com");

        when(membroRepositorio.findById(criadorId)).thenReturn(Optional.of(criador));
        when(membroRepositorio.findByEmail("maria@x.com")).thenReturn(Optional.empty());

        assertThrows(MembroNaoEncontradoException.class,
                () -> servico.criar(criadorId, "Viagem", List.of("Maria@X.com ")));

        verify(grupoRepositorio, never()).save(any());
        verify(participacaoRepositorio, never()).saveAll(any());
    }

    @Test
    void criaGrupoValido() {
        Membro criador = new Membro();
        criador.setId(criadorId);
        criador.setEmail("ana@x.com");

        Membro maria = new Membro();
        maria.setId(UUID.randomUUID());
        maria.setEmail("maria@x.com");

        Membro joao = new Membro();
        joao.setId(UUID.randomUUID());
        joao.setEmail("joao@x.com");

        when(membroRepositorio.findById(criadorId)).thenReturn(Optional.of(criador));
        when(membroRepositorio.findByEmail("maria@x.com")).thenReturn(Optional.of(maria));
        when(membroRepositorio.findByEmail("joao@x.com")).thenReturn(Optional.of(joao));

        GrupoDetalhe detalhe = servico.criar(criadorId, "  Viagem ", List.of(" Maria@X.com ", "joao@x.com"));
        GrupoEntidade grupo = detalhe.grupo();

        assertEquals("Viagem", grupo.getNome());
        assertSame(criador, grupo.getCriador());
        assertNotNull(grupo.getCriadoEm());
        assertEquals(List.of(criador, maria, joao), detalhe.membros());

        verify(grupoRepositorio).save(grupo);
        verify(participacaoRepositorio).saveAll(captor.capture());
        List<GrupoMembroEntidade> participacoes = captor.getValue();
        assertEquals(3, participacoes.size());
        assertEquals(List.of(criador, maria, joao),
                participacoes.stream().map(GrupoMembroEntidade::getMembro).toList());
        assertTrue(participacoes.stream().allMatch(p -> p.getGrupo() == grupo));
    }

    @Test
    void rejeitaListarGruposComMembroIdNulo(){
        assertThrows(IllegalArgumentException.class,
                () -> servico.listarDoMembro(null));
    }

    @Test
    void rejeitaListarGruposComMembroNaoCadastrado(){
        assertThrows(MembroNaoEncontradoException.class,
                () -> servico.listarDoMembro(UUID.randomUUID()));
    }

    @Test
    void ListaVaziaSemGrupos(){
        UUID membroId = UUID.randomUUID();
        Membro membro = new Membro();
        membro.setId(membroId);

        when(membroRepositorio.existsById(membroId)).thenReturn(true);
        when(participacaoRepositorio.findByMembroId(membroId)).thenReturn(List.of());

        assertTrue(servico.listarDoMembro(membroId).isEmpty());
    }

    @Test
    void ListaComGrupos() {
        UUID membroId = UUID.randomUUID();
        Membro criador = new Membro();
        criador.setId(membroId);

        GrupoMembroEntidade viagem = new GrupoMembroEntidade();
        GrupoEntidade viagemGrupo = new GrupoEntidade();
        viagemGrupo.setId(UUID.randomUUID());
        viagem.setGrupo(viagemGrupo);

        GrupoMembroEntidade cinema = new GrupoMembroEntidade();
        GrupoEntidade cinemaGrupo = new GrupoEntidade();
        cinemaGrupo.setId(UUID.randomUUID());
        cinema.setGrupo(cinemaGrupo);

        GrupoMembroEntidade parque = new GrupoMembroEntidade();
        GrupoEntidade parqueGrupo = new GrupoEntidade();
        parqueGrupo.setId(UUID.randomUUID());
        parque.setGrupo(parqueGrupo);

        when(membroRepositorio.existsById(membroId)).thenReturn(true);
        when(participacaoRepositorio.findByMembroId(membroId)).thenReturn(List.of(viagem, cinema, parque));

        assertEquals(servico.listarDoMembro(membroId), List.of(viagem.getGrupo(), cinema.getGrupo(), parque.getGrupo()));

    }

    @Test
    void rejeitaGrupoIdNulo(){
        assertThrows(IllegalArgumentException.class,
                () -> servico.buscarDetalhe(null, UUID.randomUUID()));
    }

    @Test
    void rejeitaMembroIdNulo(){
        assertThrows(IllegalArgumentException.class,
                () -> servico.buscarDetalhe(UUID.randomUUID(), null));
    }

    @Test
    void rejeitaGrupoNaoEncontrado(){

        UUID grupoId = UUID.randomUUID();
        when(grupoRepositorio.findById(grupoId)).thenReturn(Optional.empty());

        assertThrows(GrupoNaoEncontradoException.class,
                () -> servico.buscarDetalhe(grupoId, UUID.randomUUID()));
    }

    @Test
    void rejeitaMembroNaoPertenceAoGrupo(){
        UUID grupoId = UUID.randomUUID();
        UUID membroId = UUID.randomUUID();

        when(grupoRepositorio.findById(grupoId)).thenReturn(Optional.of(new GrupoEntidade()));
        when(participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId,membroId)).thenReturn(false);

        assertThrows(GrupoNaoEncontradoException.class,
                () -> servico.buscarDetalhe(grupoId, membroId));

        verify(participacaoRepositorio, never()).findByGrupoId(any());
    }

    @Test
    void buscaDetalheDoGrupo() {
        UUID grupoId = UUID.randomUUID();
        UUID membroId = UUID.randomUUID();

        Membro maria = new Membro();
        maria.setId(UUID.randomUUID());
        maria.setEmail("maria@x.com");

        Membro joao = new Membro();
        joao.setId(UUID.randomUUID());
        joao.setEmail("joao@x.com");

        Membro lucas = new Membro();
        lucas.setId(UUID.randomUUID());
        lucas.setEmail("lucas@x.com");

        GrupoEntidade grupo = new GrupoEntidade();
        grupo.setNome("Viagem");

        GrupoMembroEntidade participacaoMaria = new GrupoMembroEntidade();
        participacaoMaria.setMembro(maria);
        GrupoMembroEntidade participacaoJoao = new GrupoMembroEntidade();
        participacaoJoao.setMembro(joao);
        GrupoMembroEntidade participacaoLucas = new GrupoMembroEntidade();
        participacaoLucas.setMembro(lucas);

        when(grupoRepositorio.findById(grupoId)).thenReturn(Optional.of(grupo));
        when(participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId, membroId)).thenReturn(true);
        when(participacaoRepositorio.findByGrupoId(grupoId))
                .thenReturn(List.of(participacaoMaria, participacaoJoao, participacaoLucas));

        GrupoDetalhe detalhe = servico.buscarDetalhe(grupoId, membroId);

        assertSame(grupo, detalhe.grupo());
        assertEquals(List.of(maria, joao, lucas), detalhe.membros());
    }

    @Test
    void rejeitaGrupoIdNuloAoAdicionar(){
        assertThrows(IllegalArgumentException.class,
                () -> servico.adicionarMembro(null, UUID.randomUUID(), "maria@x.com"));
    }

    @Test
    void rejeitaMembroIdNuloAoAdicionar(){
        assertThrows(IllegalArgumentException.class,
                () -> servico.adicionarMembro(UUID.randomUUID(), null, "maria@x.com"));
    }

    @Test
    void rejeitaEmailNuloAoAdicionar(){
        assertThrows(IllegalArgumentException.class,
                () -> servico.adicionarMembro(UUID.randomUUID(), UUID.randomUUID(), null));
    }

    @Test
    void rejeitaEmailEmBrancoAoAdicionar(){
        assertThrows(IllegalArgumentException.class,
                () -> servico.adicionarMembro(UUID.randomUUID(), UUID.randomUUID(), "   "));
    }

    @Test
    void rejeitaGrupoNaoEncontradoAoAdicionar(){
        UUID grupoId = UUID.randomUUID();
        when(grupoRepositorio.findById(grupoId)).thenReturn(Optional.empty());

        assertThrows(GrupoNaoEncontradoException.class,
                () -> servico.adicionarMembro(grupoId, UUID.randomUUID(), "maria@x.com"));
    }

    @Test
    void rejeitaQuemPedeForaDoGrupoAoAdicionar(){
        UUID grupoId = UUID.randomUUID();
        UUID membroId = UUID.randomUUID();

        when(grupoRepositorio.findById(grupoId)).thenReturn(Optional.of(new GrupoEntidade()));
        when(participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId, membroId)).thenReturn(false);

        assertThrows(GrupoNaoEncontradoException.class,
                () -> servico.adicionarMembro(grupoId, membroId, "maria@x.com"));

        verify(membroRepositorio, never()).findByEmail(any());
        verify(participacaoRepositorio, never()).save(any());
    }

    @Test
    void rejeitaEmailNaoCadastradoAoAdicionar(){
        UUID grupoId = UUID.randomUUID();
        UUID membroId = UUID.randomUUID();

        when(grupoRepositorio.findById(grupoId)).thenReturn(Optional.of(new GrupoEntidade()));
        when(participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId, membroId)).thenReturn(true);
        when(membroRepositorio.findByEmail("maria@x.com")).thenReturn(Optional.empty());

        assertThrows(MembroNaoEncontradoException.class,
                () -> servico.adicionarMembro(grupoId, membroId, " Maria@X.com "));

        verify(participacaoRepositorio, never()).save(any());
    }

    @Test
    void rejeitaMembroJaPertencenteAoGrupo(){
        UUID grupoId = UUID.randomUUID();
        UUID membroId = UUID.randomUUID();

        Membro maria = new Membro();
        maria.setId(UUID.randomUUID());
        maria.setEmail("maria@x.com");

        GrupoEntidade grupo = new GrupoEntidade();
        grupo.setNome("Viagem");

        GrupoMembroEntidade participacaoMaria = new GrupoMembroEntidade();
        participacaoMaria.setMembro(maria);

        when(grupoRepositorio.findById(grupoId)).thenReturn(Optional.of(grupo));
        when(participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId, membroId)).thenReturn(true);
        when(membroRepositorio.findByEmail("maria@x.com")).thenReturn(Optional.of(maria));
        when(participacaoRepositorio.findByGrupoId(grupoId)).thenReturn(List.of(participacaoMaria));

        assertThrows(MembroRepetidoException.class,
                () -> servico.adicionarMembro(grupoId, membroId, "maria@x.com"));

        verify(participacaoRepositorio, never()).save(any());
    }

    @Test
    void adicionaMembroAoGrupo(){
        UUID grupoId = UUID.randomUUID();
        UUID membroId = UUID.randomUUID();

        Membro joao = new Membro();
        joao.setId(UUID.randomUUID());
        joao.setEmail("joao@x.com");

        Membro maria = new Membro();
        maria.setId(UUID.randomUUID());
        maria.setEmail("maria@x.com");

        GrupoEntidade grupo = new GrupoEntidade();
        grupo.setNome("Viagem");

        GrupoMembroEntidade participacaoJoao = new GrupoMembroEntidade();
        participacaoJoao.setMembro(joao);

        when(grupoRepositorio.findById(grupoId)).thenReturn(Optional.of(grupo));
        when(participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId, membroId)).thenReturn(true);
        when(membroRepositorio.findByEmail("maria@x.com")).thenReturn(Optional.of(maria));
        when(participacaoRepositorio.findByGrupoId(grupoId)).thenReturn(List.of(participacaoJoao));

        Membro adicionado = servico.adicionarMembro(grupoId, membroId, " Maria@X.com ");

        ArgumentCaptor<GrupoMembroEntidade> captorParticipacao = ArgumentCaptor.forClass(GrupoMembroEntidade.class);
        verify(participacaoRepositorio).save(captorParticipacao.capture());

        assertSame(maria, adicionado);
        assertSame(grupo, captorParticipacao.getValue().getGrupo());
        assertSame(maria, captorParticipacao.getValue().getMembro());
    }

    @Test
    void rejeitaGrupoIdNuloAoRemover(){
        assertThrows(IllegalArgumentException.class,
                () -> servico.removerMembro(null, UUID.randomUUID(), UUID.randomUUID()));
    }

    @Test
    void rejeitaMembroIdNuloAoRemover(){
        assertThrows(IllegalArgumentException.class,
                () -> servico.removerMembro(UUID.randomUUID(), null, UUID.randomUUID()));
    }

    @Test
    void rejeitaAlvoIdNuloAoRemover(){
        assertThrows(IllegalArgumentException.class,
                () -> servico.removerMembro(UUID.randomUUID(), UUID.randomUUID(), null));
    }

    @Test
    void rejeitaGrupoNaoEncontradoAoRemover(){
        UUID grupoId = UUID.randomUUID();
        when(grupoRepositorio.findById(grupoId)).thenReturn(Optional.empty());

        assertThrows(GrupoNaoEncontradoException.class,
                () -> servico.removerMembro(grupoId, UUID.randomUUID(), UUID.randomUUID()));
    }

    @Test
    void rejeitaQuemPedeForaDoGrupoAoRemover(){
        UUID grupoId = UUID.randomUUID();
        UUID membroId = UUID.randomUUID();

        when(grupoRepositorio.findById(grupoId)).thenReturn(Optional.of(new GrupoEntidade()));
        when(participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId, membroId)).thenReturn(false);

        assertThrows(GrupoNaoEncontradoException.class,
                () -> servico.removerMembro(grupoId, membroId, UUID.randomUUID()));

        verify(participacaoRepositorio, never()).findByGrupoId(any());
        verify(participacaoRepositorio, never()).delete(any());
    }

    @Test
    void rejeitaAlvoForaDoGrupoAoRemover(){
        UUID grupoId = UUID.randomUUID();
        UUID membroId = UUID.randomUUID();
        UUID alvoId = UUID.randomUUID();

        Membro ana = new Membro();
        ana.setId(membroId);

        Membro maria = new Membro();
        maria.setId(UUID.randomUUID());

        GrupoEntidade grupo = new GrupoEntidade();
        grupo.setNome("Viagem");

        GrupoMembroEntidade participacaoAna = new GrupoMembroEntidade();
        participacaoAna.setMembro(ana);
        GrupoMembroEntidade participacaoMaria = new GrupoMembroEntidade();
        participacaoMaria.setMembro(maria);

        when(grupoRepositorio.findById(grupoId)).thenReturn(Optional.of(grupo));
        when(participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId, membroId)).thenReturn(true);
        when(participacaoRepositorio.findByGrupoId(grupoId)).thenReturn(List.of(participacaoAna, participacaoMaria));

        assertThrows(MembroNaoEncontradoException.class,
                () -> servico.removerMembro(grupoId, membroId, alvoId));

        verify(participacaoRepositorio, never()).delete(any());
    }

    @Test
    void rejeitaGrupoComMenosDeDoisMembrosAoRemover(){
        UUID grupoId = UUID.randomUUID();
        UUID membroId = UUID.randomUUID();
        UUID alvoId = UUID.randomUUID();

        Membro ana = new Membro();
        ana.setId(membroId);

        Membro maria = new Membro();
        maria.setId(alvoId);

        GrupoEntidade grupo = new GrupoEntidade();
        grupo.setNome("Viagem");

        GrupoMembroEntidade participacaoAna = new GrupoMembroEntidade();
        participacaoAna.setMembro(ana);
        GrupoMembroEntidade participacaoMaria = new GrupoMembroEntidade();
        participacaoMaria.setMembro(maria);

        when(grupoRepositorio.findById(grupoId)).thenReturn(Optional.of(grupo));
        when(participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId, membroId)).thenReturn(true);
        when(participacaoRepositorio.findByGrupoId(grupoId)).thenReturn(List.of(participacaoAna, participacaoMaria));

        assertThrows(GrupoComPoucosMembrosException.class,
                () -> servico.removerMembro(grupoId, membroId, alvoId));

        verify(participacaoRepositorio, never()).delete(any());
    }

    @Test
    void rejeitaGrupoComDespesaAoRemover() {
        UUID grupoId = UUID.randomUUID();
        UUID membroId = UUID.randomUUID();
        UUID alvoId = UUID.randomUUID();

        Membro ana = new Membro();
        ana.setId(membroId);
        Membro maria = new Membro();
        maria.setId(alvoId);
        Membro joao = new Membro();
        joao.setId(UUID.randomUUID());

        GrupoEntidade grupo = new GrupoEntidade();
        grupo.setNome("Viagem");

        GrupoMembroEntidade participacaoAna = new GrupoMembroEntidade();
        participacaoAna.setMembro(ana);
        GrupoMembroEntidade participacaoMaria = new GrupoMembroEntidade();
        participacaoMaria.setMembro(maria);
        GrupoMembroEntidade participacaoJoao = new GrupoMembroEntidade();
        participacaoJoao.setMembro(joao);

        when(grupoRepositorio.findById(grupoId)).thenReturn(Optional.of(grupo));
        when(participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId, membroId)).thenReturn(true);
        when(participacaoRepositorio.findByGrupoId(grupoId))
                .thenReturn(List.of(participacaoAna, participacaoMaria, participacaoJoao));
        when(despesaRepositorio.existsByGrupoId(grupoId)).thenReturn(true);

        assertThrows(GrupoComDespesasException.class,
                () -> servico.removerMembro(grupoId, membroId, alvoId));

        verify(participacaoRepositorio, never()).delete(any());
    }

    @Test
    void removeMembroDoGrupo(){
        UUID grupoId = UUID.randomUUID();
        UUID membroId = UUID.randomUUID();
        UUID alvoId = UUID.randomUUID();

        Membro ana = new Membro();
        ana.setId(membroId);

        Membro maria = new Membro();
        maria.setId(alvoId);

        Membro joao = new Membro();
        joao.setId(UUID.randomUUID());

        GrupoEntidade grupo = new GrupoEntidade();
        grupo.setNome("Viagem");

        GrupoMembroEntidade participacaoAna = new GrupoMembroEntidade();
        participacaoAna.setMembro(ana);
        GrupoMembroEntidade participacaoMaria = new GrupoMembroEntidade();
        participacaoMaria.setMembro(maria);
        GrupoMembroEntidade participacaoJoao = new GrupoMembroEntidade();
        participacaoJoao.setMembro(joao);

        when(grupoRepositorio.findById(grupoId)).thenReturn(Optional.of(grupo));
        when(participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId, membroId)).thenReturn(true);
        when(participacaoRepositorio.findByGrupoId(grupoId))
                .thenReturn(List.of(participacaoAna, participacaoMaria, participacaoJoao));

        servico.removerMembro(grupoId, membroId, alvoId);

        verify(participacaoRepositorio).delete(participacaoMaria);
    }

}
