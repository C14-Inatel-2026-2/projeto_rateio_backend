package rateio.grupo.service;

import org.springframework.stereotype.Component;
import rateio.grupo.entity.GrupoEntidade;
import rateio.grupo.exception.GrupoNaoEncontradoException;
import rateio.grupo.repository.GrupoMembroRepository;
import rateio.grupo.repository.GrupoRepository;

import java.util.Optional;
import java.util.UUID;

@Component
public class AcessoAoGrupo {

    private final GrupoRepository grupoRepositorio;
    private final GrupoMembroRepository participacaoRepositorio;

    public AcessoAoGrupo(GrupoRepository grupoRepositorio, GrupoMembroRepository participacaoRepositorio) {
        this.grupoRepositorio = grupoRepositorio;
        this.participacaoRepositorio = participacaoRepositorio;
    }

    // Para quem nao participa, o grupo se comporta como inexistente: responder de outro
    // jeito revelaria a existencia de grupos alheios.
    public Optional<GrupoEntidade> buscarComoMembro(UUID grupoId, UUID membroId) {
        return grupoRepositorio.findById(grupoId)
                .filter(grupo -> participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId, membroId));
    }

    public GrupoEntidade exigirMembro(UUID grupoId, UUID membroId) {
        return buscarComoMembro(grupoId, membroId).orElseThrow(GrupoNaoEncontradoException::new);
    }
}
