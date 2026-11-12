package rateio.grupo.controller;

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
import rateio.grupo.dto.AdicionarMembroRequest;
import rateio.grupo.dto.CriarGrupoRequest;
import rateio.grupo.dto.GrupoResponse;
import rateio.grupo.dto.GrupoResumoResponse;
import rateio.grupo.service.GrupoService;
import rateio.membro.MembroResponse;

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
        return GrupoResponse.de(servico.criar(membroId, requisicao.nome(), requisicao.emails()));
    }

    @GetMapping
    public List<GrupoResumoResponse> listar(@RequestParam UUID membroId) {
        return servico.listarDoMembro(membroId).stream()
                .map(GrupoResumoResponse::de)
                .toList();
    }

    @GetMapping("/{grupoId}")
    public GrupoResponse buscar(@PathVariable UUID grupoId, @RequestParam UUID membroId) {
        return GrupoResponse.de(servico.buscarDetalhe(grupoId, membroId));
    }

    @PostMapping("/{grupoId}/membros")
    @ResponseStatus(HttpStatus.CREATED)
    public MembroResponse adicionarMembro(@PathVariable UUID grupoId,
                                          @RequestParam UUID membroId,
                                          @RequestBody AdicionarMembroRequest requisicao) {
        return MembroResponse.de(servico.adicionarMembro(grupoId, membroId, requisicao.email()));
    }

    @DeleteMapping("/{grupoId}/membros/{alvoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removerMembro(@PathVariable UUID grupoId,
                              @PathVariable UUID alvoId,
                              @RequestParam UUID membroId) {
        servico.removerMembro(grupoId, membroId, alvoId);
    }
}
