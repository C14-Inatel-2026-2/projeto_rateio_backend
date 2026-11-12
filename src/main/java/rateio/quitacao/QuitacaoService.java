package rateio.quitacao;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rateio.grupo.GrupoMembroRepository;
import rateio.grupo.GrupoRepository;
import rateio.saldo.Saldo;
import rateio.saldo.SaldoDetalhe;
import rateio.saldo.SaldoService;

import java.util.*;

@Service
public class QuitacaoService {

    private final SaldoService saldoService;

    private Saldo converterParaSaldo(SaldoDetalhe saldo) {
        return new Saldo(saldo.membroId(), saldo.saldo());
    }


    private QuitacaoDetalhe converterParaDetalhe(
            Quitacao.Transferencia transferencia,
            Map<UUID, String> nomes) {

        return new QuitacaoDetalhe(
                transferencia.deId(),
                nomes.get(transferencia.deId()),
                transferencia.paraId(),
                nomes.get(transferencia.paraId()),
                transferencia.valor().setScale(2)
        );
    }

    public QuitacaoService(GrupoRepository grupoRepositorio, GrupoMembroRepository participacaoRepositorio, SaldoService saldoService) {
        this.saldoService = saldoService;
    }

    @Transactional(readOnly = true)
    public List<QuitacaoDetalhe> listaTransferencias(UUID membroId, UUID grupoId) {
        List<SaldoDetalhe> saldos = saldoService.consultar(grupoId, membroId);

        List<Saldo> saldosDominio = new ArrayList<>();
        Map<UUID, String> nomes = new HashMap<>();

        for (SaldoDetalhe saldo : saldos) {
            saldosDominio.add(converterParaSaldo(saldo));

            nomes.put(saldo.membroId(), saldo.nome());
        }

        Quitacao plano = Quitacao.apartirDe(saldosDominio);

        List<QuitacaoDetalhe> resposta = new ArrayList<>();

        for (Quitacao.Transferencia transferencia : plano.transferencias()) {
            resposta.add(converterParaDetalhe(transferencia, nomes));
        }

        return resposta;
    }
}
