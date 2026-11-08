package rateio.despesa;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
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
        DespesaDetalhe detalhe = servico.criar(
                membroId,
                grupoId,
                requisicao.descricao(),
                requisicao.valorTotal(),
                requisicao.pagadorId(),
                requisicao.estrategia(),
                requisicao.participantes());
        return DespesaResponse.de(detalhe);
    }

    @GetMapping
    public List<DespesaResumoResponse> listar(@PathVariable UUID grupoId,
                                              @RequestParam UUID membroId) {
        List<DespesaResumoResponse> resposta = new ArrayList<>();
        for (DespesaResumo resumo : servico.listar(membroId, grupoId)) {
            resposta.add(DespesaResumoResponse.de(resumo));
        }
        return resposta;
    }

    @PostMapping("/{despesaId}/estorno")
    public EstornoDetalhe estornar(@PathVariable UUID grupoId,
                                   @PathVariable UUID despesaId,
                                   @RequestParam UUID membroId) {
        return servico.estornar(grupoId, despesaId, membroId);
    }
}
