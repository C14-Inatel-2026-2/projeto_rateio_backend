package rateio.grupo;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import rateio.membro.Membro;
import rateio.membro.MembroResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/grupos")
public class GrupoController {

    private final GrupoService servico;

    public GrupoController(GrupoService servico) {
        this.servico = servico;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GrupoResponse criar(@RequestParam UUID membroId, @RequestBody CriarGrupoRequest requisicao) {
        GrupoDetalhe detalhe = servico.criar(membroId, requisicao.nome(), requisicao.emails());
        return GrupoResponse.de(detalhe);
    }

    @GetMapping
    public List<GrupoResumoResponse> listar(@RequestParam UUID membroId) {
        List<GrupoResumoResponse> resposta = new ArrayList<>();
        for (GrupoEntidade grupo : servico.listarDoMembro(membroId)) {
            resposta.add(GrupoResumoResponse.de(grupo));
        }
        return resposta;
    }

    @GetMapping("/{grupoId}")
    public GrupoResponse buscar(@PathVariable UUID grupoId, @RequestParam UUID membroId) {
        GrupoDetalhe detalhe = servico.buscarDetalhe(grupoId, membroId);
        return GrupoResponse.de(detalhe);
    }

    @PostMapping("/{grupoId}/membros")
    @ResponseStatus(HttpStatus.CREATED)
    public MembroResponse adicionarMembro(@PathVariable UUID grupoId,
                                          @RequestParam UUID membroId,
                                          @RequestBody AdicionarMembroRequest requisicao) {
        Membro adicionado = servico.adicionarMembro(grupoId, membroId, requisicao.email());
        return MembroResponse.de(adicionado);
    }

    @DeleteMapping("/{grupoId}/membros/{alvoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removerMembro(@PathVariable UUID grupoId,
                              @PathVariable UUID alvoId,
                              @RequestParam UUID membroId) {
        servico.removerMembro(grupoId, membroId, alvoId);
    }
}