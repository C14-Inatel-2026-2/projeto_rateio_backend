package rateio.saldo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import rateio.saldo.dto.SaldoDetalhe;
import rateio.saldo.dto.SaldoResponse;
import rateio.saldo.service.SaldoService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/grupos/{grupoId}/saldos")
public class SaldoController {

    private final SaldoService servico;

    public SaldoController(SaldoService servico) {
        this.servico = servico;
    }

    @GetMapping
    public List<SaldoResponse> consultar(@PathVariable UUID grupoId, @RequestParam UUID membroId) {
        List<SaldoResponse> resposta = new ArrayList<>();
        for (SaldoDetalhe detalhe : servico.consultar(grupoId, membroId)) {
            resposta.add(SaldoResponse.de(detalhe));
        }
        return resposta;
    }
}