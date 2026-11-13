package rateio.grupo.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rateio.despesa.repository.DespesaRepository;
import rateio.grupo.domain.Grupo;
import rateio.grupo.dto.GrupoDetalhe;
import rateio.grupo.entity.GrupoEntidade;
import rateio.grupo.entity.GrupoMembroEntidade;
import rateio.grupo.exception.GrupoComDespesasException;
import rateio.grupo.exception.GrupoComPoucosMembrosException;
import rateio.grupo.exception.MembroRepetidoException;
import rateio.grupo.repository.GrupoMembroRepository;
import rateio.grupo.repository.GrupoRepository;
import rateio.membro.entity.Membro;
import rateio.membro.exception.MembroNaoEncontradoException;
import rateio.membro.repository.MembroRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GrupoService {

    private static final int MINIMO_DE_MEMBROS = 2;

    private final GrupoRepository grupoRepositorio;
    private final GrupoMembroRepository participacaoRepositorio;
    private final MembroRepository membroRepositorio;
    private final DespesaRepository despesaRepositorio;
    private final AcessoAoGrupo acesso;

    public GrupoService(GrupoRepository grupoRepositorio,
                        GrupoMembroRepository participacaoRepositorio,
                        MembroRepository membroRepositorio,
                        DespesaRepository despesaRepositorio,
                        AcessoAoGrupo acesso) {
        this.grupoRepositorio = grupoRepositorio;
        this.participacaoRepositorio = participacaoRepositorio;
        this.membroRepositorio = membroRepositorio;
        this.despesaRepositorio = despesaRepositorio;
        this.acesso = acesso;
    }

    @Transactional
    public GrupoDetalhe criar(UUID criadorId, String nome, List<String> emails) {
        if (criadorId == null || nome == null || nome.isBlank()
                || emails == null || emails.isEmpty()) {
            throw new IllegalArgumentException("Nome do grupo e ao menos um membro sao obrigatorios");
        }

        Membro criador = membroRepositorio.findById(criadorId)
                .orElseThrow(MembroNaoEncontradoException::new);

        List<Membro> participantes = new ArrayList<>();
        participantes.add(criador);
        for (String email : emailsDosConvidados(emails, criador)) {
            participantes.add(buscarPorEmail(email));
        }

        GrupoEntidade grupo = new GrupoEntidade();
        grupo.setNome(nome.trim());
        grupo.setCriador(criador);
        grupo.setCriadoEm(Instant.now());
        grupoRepositorio.save(grupo);

        participacaoRepositorio.saveAll(participantes.stream()
                .map(membro -> new GrupoMembroEntidade(grupo, membro))
                .toList());

        return new GrupoDetalhe(grupo, participantes);
    }

    @Transactional(readOnly = true)
    public List<GrupoEntidade> listarDoMembro(UUID membroId) {
        if (membroId == null) {
            throw new IllegalArgumentException("membroId e obrigatorio");
        }
        if (!membroRepositorio.existsById(membroId)) {
            throw new MembroNaoEncontradoException();
        }

        return participacaoRepositorio.findByMembroId(membroId).stream()
                .map(GrupoMembroEntidade::getGrupo)
                .toList();
    }

    @Transactional(readOnly = true)
    public GrupoDetalhe buscarDetalhe(UUID grupoId, UUID membroId) {
        if (grupoId == null || membroId == null) {
            throw new IllegalArgumentException("grupoId e membroId sao obrigatorios");
        }

        GrupoEntidade grupo = acesso.exigirMembro(grupoId, membroId);
        List<Membro> membros = participacaoRepositorio.findByGrupoId(grupoId).stream()
                .map(GrupoMembroEntidade::getMembro)
                .toList();

        return new GrupoDetalhe(grupo, membros);
    }

    @Transactional
    public Membro adicionarMembro(UUID grupoId, UUID membroId, String email) {
        if (grupoId == null || membroId == null || email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email, membroId e grupoId sao obrigatorios");
        }

        GrupoEntidade grupo = acesso.exigirMembro(grupoId, membroId);
        Membro novoMembro = buscarPorEmail(normalizar(email));

        if (dominioDe(grupo, participacaoRepositorio.findByGrupoId(grupoId)).contem(novoMembro.getId())) {
            throw new MembroRepetidoException();
        }

        participacaoRepositorio.save(new GrupoMembroEntidade(grupo, novoMembro));
        return novoMembro;
    }

    @Transactional
    public void removerMembro(UUID grupoId, UUID membroId, UUID alvoId) {
        if (grupoId == null || membroId == null || alvoId == null) {
            throw new IllegalArgumentException("GrupoId, membroId e alvoId sao obrigatorios");
        }

        GrupoEntidade grupo = acesso.exigirMembro(grupoId, membroId);
        List<GrupoMembroEntidade> participacoes = participacaoRepositorio.findByGrupoId(grupoId);
        GrupoMembroEntidade participacaoAlvo = participacoes.stream()
                .filter(participacao -> participacao.getMembro().getId().equals(alvoId))
                .findFirst()
                .orElseThrow(MembroNaoEncontradoException::new);

        Grupo dominio = dominioDe(grupo, participacoes);
        dominio.removerMembro(alvoId);
        if (dominio.getQuantidadeDeMembros() < MINIMO_DE_MEMBROS) {
            throw new GrupoComPoucosMembrosException();
        }
        if (despesaRepositorio.existsByGrupoId(grupoId)) {
            throw new GrupoComDespesasException();
        }

        participacaoRepositorio.delete(participacaoAlvo);
    }

    private Set<String> emailsDosConvidados(List<String> emails, Membro criador) {
        Set<String> normalizados = new LinkedHashSet<>();
        for (String email : emails) {
            if (email == null || email.isBlank()) {
                throw new IllegalArgumentException("Emails dos membros nao podem ser vazios");
            }
            String normalizado = normalizar(email);
            if (normalizado.equals(criador.getEmail()) || !normalizados.add(normalizado)) {
                throw new MembroRepetidoException();
            }
        }
        return normalizados;
    }

    private Membro buscarPorEmail(String email) {
        return membroRepositorio.findByEmail(email).orElseThrow(MembroNaoEncontradoException::new);
    }

    private static String normalizar(String email) {
        return email.trim().toLowerCase();
    }

    private static Grupo dominioDe(GrupoEntidade grupo, List<GrupoMembroEntidade> participacoes) {
        Set<UUID> idsDosMembros = participacoes.stream()
                .map(participacao -> participacao.getMembro().getId())
                .collect(Collectors.toSet());
        return new Grupo(grupo.getNome(), idsDosMembros);
    }
}
