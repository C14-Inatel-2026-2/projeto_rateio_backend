package rateio.membro.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import rateio.membro.service.MembroService;
import rateio.membro.dto.CadastroRequest;
import rateio.membro.dto.LoginRequest;
import rateio.membro.dto.MembroResponse;
import rateio.membro.entity.Membro;

@RestController
@RequestMapping("/membros")
public class MembroController {

    private final MembroService servico;

    public MembroController(MembroService servico) {
        this.servico = servico;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MembroResponse cadastrar(@RequestBody CadastroRequest requisicao) {
        Membro membro = servico.cadastrar(requisicao.nome(), requisicao.email(), requisicao.senha());
        return MembroResponse.de(membro);
    }

    @PostMapping("/login")
    public MembroResponse login(@RequestBody LoginRequest requisicao) {
        Membro membro = servico.autenticar(requisicao.email(), requisicao.senha());
        return MembroResponse.de(membro);
    }
}
