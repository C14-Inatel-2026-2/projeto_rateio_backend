package rateio.quitacao;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/grupos/{grupoId}/quitacao")
public class QuitacaoController {

    private final QuitacaoService servico;

    public QuitacaoController(QuitacaoService servico) {
        this.servico = servico;
    }

    @GetMapping
    public List<QuitacaoResponse> sugerir(@PathVariable UUID grupoId, @RequestParam UUID membroId) {
        List<QuitacaoResponse> resposta = new ArrayList<>();
        for (QuitacaoDetalhe detalhe : servico.listaTransferencias(membroId, grupoId)) {
            resposta.add(QuitacaoResponse.de(detalhe));
        }
        return resposta;
    }
}
