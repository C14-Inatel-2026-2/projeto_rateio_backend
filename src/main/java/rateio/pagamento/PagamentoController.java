package rateio.pagamento;

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
@RequestMapping("/grupos/{grupoId}")
public class PagamentoController {

    private final PagamentoService servico;

    public PagamentoController(PagamentoService servico) {
        this.servico = servico;
    }

    @PostMapping("/pagamentos")
    @ResponseStatus(HttpStatus.CREATED)
    public PagamentoResponse registrar(@PathVariable UUID grupoId,
                                       @RequestParam UUID membroId,
                                       @RequestBody RegistrarPagamentoRequest requisicao) {
        PagamentoDetalhe detalhe = servico.registrar(grupoId, membroId, requisicao.paraId(), requisicao.valor());
        return PagamentoResponse.de(detalhe);
    }

    @PostMapping("/pagamentos/{pagamentoId}/estorno")
    public EstornoPagamentoDetalhe estornar(@PathVariable UUID grupoId,
                                            @PathVariable UUID pagamentoId,
                                            @RequestParam UUID membroId) {
        return servico.estornar(grupoId, pagamentoId, membroId);
    }

    @GetMapping("/extrato")
    public List<ExtratoResponse> extrato(@PathVariable UUID grupoId, @RequestParam UUID membroId) {
        List<ExtratoResponse> resposta = new ArrayList<>();
        for (ExtratoDetalhe detalhe : servico.extrato(grupoId, membroId)) {
            resposta.add(ExtratoResponse.de(detalhe));
        }
        return resposta;
    }
}
