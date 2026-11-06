package rateio.grupo;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rateio.membro.Membro;
import rateio.membro.MembroNaoEncontradoException;
import rateio.membro.MembroRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class GrupoService {

    private final GrupoRepository grupoRepositorio;
    private final GrupoMembroRepository participacaoRepositorio;
    private final MembroRepository membroRepositorio;

    public GrupoService(GrupoRepository grupoRepositorio,
                        GrupoMembroRepository participacaoRepositorio,
                        MembroRepository membroRepositorio) {
        this.grupoRepositorio = grupoRepositorio;
        this.participacaoRepositorio = participacaoRepositorio;
        this.membroRepositorio = membroRepositorio;
    }

    @Transactional
    public GrupoEntidade criar(UUID criadorId, String nome, List<String> emails) {
        if (criadorId == null || nome == null || nome.isBlank()
                || emails == null || emails.isEmpty()) {
            throw new IllegalArgumentException("Nome do grupo e ao menos um membro sao obrigatorios");
        }

        Membro criador = membroRepositorio.findById(criadorId)
                .orElseThrow(MembroNaoEncontradoException::new);

        Set<String> emailsNormalizados = new LinkedHashSet<>();
        for (String email : emails) {
            if (email == null || email.isBlank()) {
                throw new IllegalArgumentException("Emails dos membros nao podem ser vazios");
            }
            String normalizado = email.trim().toLowerCase();
            if (normalizado.equals(criador.getEmail()) || !emailsNormalizados.add(normalizado)) {
                throw new MembroRepetidoException();
            }
        }

        List<Membro> participantes = new ArrayList<>();
        participantes.add(criador);
        for (String email : emailsNormalizados) {
            participantes.add(membroRepositorio.findByEmail(email)
                    .orElseThrow(MembroNaoEncontradoException::new));
        }

        GrupoEntidade grupo = new GrupoEntidade();
        grupo.setNome(nome.trim());
        grupo.setCriador(criador);
        grupo.setCriadoEm(Instant.now());
        grupoRepositorio.save(grupo);

        List<GrupoMembroEntidade> participacoes = new ArrayList<>();
        for (Membro membro : participantes) {
            GrupoMembroEntidade participacao = new GrupoMembroEntidade();
            participacao.setGrupo(grupo);
            participacao.setMembro(membro);
            participacoes.add(participacao);
        }
        participacaoRepositorio.saveAll(participacoes);

        return grupo;
    }

    @Transactional(readOnly = true)
    public List<GrupoEntidade> listarDoMembro(UUID membroId){
        if (membroId == null ) {
            throw new IllegalArgumentException("membroId e obrigatorio");
        }

        boolean membroExiste = membroRepositorio.existsById(membroId);
        if(!membroExiste){
            throw new MembroNaoEncontradoException();
        }

        List<GrupoEntidade> grupos = new ArrayList<>();
        for(GrupoMembroEntidade participacao : participacaoRepositorio.findByMembroId(membroId)){
            grupos.add(participacao.getGrupoEntidade());
        }

        return grupos;
    }
}