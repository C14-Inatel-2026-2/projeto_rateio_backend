package rateio.grupo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Captor;
import org.mockito.ArgumentCaptor;

import rateio.membro.MembroNaoEncontradoException;
import rateio.membro.MembroRepository;
import rateio.membro.Membro;

import java.time.Instant;
import java.util.Optional;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.any;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class GrupoServiceTest {

    @Mock
    private GrupoRepository grupoRepositorio;

    @Mock
    private GrupoMembroRepository participacaoRepositorio;

    @Mock
    private MembroRepository membroRepositorio;

    @Captor
    private ArgumentCaptor<List<GrupoMembroEntidade>> captor;

    @InjectMocks
    private GrupoService servico;

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

        GrupoEntidade grupo = servico.criar(criadorId, "  Viagem ", List.of(" Maria@X.com ", "joao@x.com"));

        assertEquals("Viagem", grupo.getNome());
        assertSame(criador, grupo.getCriador());
        assertNotNull(grupo.getCriadoEm());

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
        viagem.setGrupoEntidade(viagemGrupo);

        GrupoMembroEntidade cinema = new GrupoMembroEntidade();
        GrupoEntidade cinemaGrupo = new GrupoEntidade();
        cinemaGrupo.setId(UUID.randomUUID());
        cinema.setGrupoEntidade(cinemaGrupo);

        GrupoMembroEntidade parque = new GrupoMembroEntidade();
        GrupoEntidade parqueGrupo = new GrupoEntidade();
        parqueGrupo.setId(UUID.randomUUID());
        parque.setGrupoEntidade(parqueGrupo);

        when(membroRepositorio.existsById(membroId)).thenReturn(true);
        when(participacaoRepositorio.findByMembroId(membroId)).thenReturn(List.of(viagem, cinema, parque));

        assertEquals(servico.listarDoMembro(membroId), List.of(viagem.getGrupoEntidade(), cinema.getGrupoEntidade(), parque.getGrupoEntidade()));

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

}
