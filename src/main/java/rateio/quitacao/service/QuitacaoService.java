package rateio.quitacao.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rateio.grupo.repository.GrupoMembroRepository;
import rateio.grupo.repository.GrupoRepository;
import rateio.quitacao.domain.Quitacao;
import rateio.quitacao.dto.QuitacaoDetalhe;
import rateio.saldo.Saldo;
import rateio.saldo.SaldoDetalhe;
import rateio.saldo.SaldoService;

import java.util.*;

@Service
public class QuitacaoService {

    private final SaldoService saldoService;

    public QuitacaoService(GrupoRepository grupoRepositorio, GrupoMembroRepository participacaoRepositorio, SaldoService saldoService) {
        this.saldoService = saldoService;
    }

    @Transactional(readOnly = true)
    public List<QuitacaoDetalhe> listaTransferencias(UUID membroId, UUID grupoId) {
        List<SaldoDetalhe> saldos = saldoService.consultar(grupoId, membroId);

        List<Saldo> saldosDominio = new ArrayList<>();
        Map<UUID, String> nomes = new HashMap<>();
        for (SaldoDetalhe saldo : saldos) {
            saldosDominio.add(new Saldo(saldo.membroId(), saldo.saldo()));
            nomes.put(saldo.membroId(), saldo.nome());
        }

        Quitacao plano = Quitacao.apartirDe(saldosDominio);

        List<QuitacaoDetalhe> resposta = new ArrayList<>();
        for (Quitacao.Transferencia transferencia : plano.transferencias()) {
            resposta.add(new QuitacaoDetalhe(
                    transferencia.deId(),
                    nomes.get(transferencia.deId()),
                    transferencia.paraId(),
                    nomes.get(transferencia.paraId()),
                    transferencia.valor().setScale(2)));
        }
        return resposta;
    }
}
