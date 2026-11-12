package rateio.despesa.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import rateio.despesa.dto.CriarDespesaRequest;
import rateio.despesa.dto.DespesaResponse;
import rateio.despesa.dto.DespesaResumoResponse;
import rateio.despesa.dto.EstornoDetalhe;
import rateio.despesa.service.DespesaService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/grupos/{grupoId}/despesas")
public class DespesaController {

    private final DespesaService servico;

    public DespesaController(DespesaService servico) {
        this.servico = servico;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DespesaResponse criar(@PathVariable UUID grupoId,
                                 @RequestParam UUID membroId,
                                 @RequestBody CriarDespesaRequest requisicao) {
        return DespesaResponse.de(servico.criar(membroId, grupoId, requisicao));
    }

    @GetMapping
    public List<DespesaResumoResponse> listar(@PathVariable UUID grupoId,
                                              @RequestParam UUID membroId) {
        return servico.listar(membroId, grupoId).stream()
                .map(DespesaResumoResponse::de)
                .toList();
    }

    @PostMapping("/{despesaId}/estorno")
    public EstornoDetalhe estornar(@PathVariable UUID grupoId,
                                   @PathVariable UUID despesaId,
                                   @RequestParam UUID membroId) {
        return servico.estornar(grupoId, despesaId, membroId);
    }
}
