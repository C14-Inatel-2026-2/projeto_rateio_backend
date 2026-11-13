package rateio.saldo;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rateio.despesa.domain.Despesa;
import rateio.despesa.entity.DespesaEntidade;
import rateio.despesa.entity.ParcelaEntidade;
import rateio.despesa.repository.ParcelaRepository;
import rateio.despesa.domain.StatusDespesa;
import rateio.grupo.entity.GrupoMembroEntidade;
import rateio.grupo.repository.GrupoMembroRepository;
import rateio.grupo.exception.GrupoNaoEncontradoException;
import rateio.grupo.repository.GrupoRepository;
import rateio.membro.entity.Membro;
import rateio.pagamento.domain.Pagamento;
import rateio.pagamento.entity.PagamentoEntidade;
import rateio.pagamento.repository.PagamentoRepository;
import rateio.pagamento.domain.StatusPagamento;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class SaldoService {

    private final GrupoRepository grupoRepositorio;
    private final GrupoMembroRepository participacaoRepositorio;
    private final ParcelaRepository parcelaRepositorio;
    private final PagamentoRepository pagamentoRepositorio;

    public SaldoService(GrupoRepository grupoRepositorio,
                        GrupoMembroRepository participacaoRepositorio,
                        ParcelaRepository parcelaRepositorio,
                        PagamentoRepository pagamentoRepositorio) {
        this.grupoRepositorio = grupoRepositorio;
        this.participacaoRepositorio = participacaoRepositorio;
        this.parcelaRepositorio = parcelaRepositorio;
        this.pagamentoRepositorio = pagamentoRepositorio;
    }

    @Transactional(readOnly = true)
    public List<SaldoDetalhe> consultar(UUID grupoId, UUID membroId) {
        validarParametrosEPreRequisitos(grupoId, membroId);

        List<GrupoMembroEntidade> participacoes = buscarParticipacoesOrdenadas(grupoId);

        Set<UUID> idsDosMembros = new LinkedHashSet<>();
        Map<UUID, String> nomes = new HashMap<>();
        for (GrupoMembroEntidade participacao : participacoes) {
            Membro membro = participacao.getMembro();
            idsDosMembros.add(membro.getId());
            nomes.put(membro.getId(), membro.getNome());
        }

        List<Despesa> despesasDominio = carregarDespesasDeDominio(grupoId);
        List<Pagamento> pagamentosDominio = carregarPagamentosDeDominio(grupoId);

        List<Saldo> saldosConsolidados = Saldo.consolidar(idsDosMembros, despesasDominio, pagamentosDominio);

        List<SaldoDetalhe> resposta = new ArrayList<>();
        for (Saldo saldo : saldosConsolidados) {
            resposta.add(new SaldoDetalhe(
                    saldo.membroId(),
                    nomes.get(saldo.membroId()),
                    saldo.valor().setScale(2)));
        }

        return resposta;
    }

    private void validarParametrosEPreRequisitos(UUID grupoId, UUID membroId) {
        if (grupoId == null || membroId == null) {
            throw new IllegalArgumentException("grupoId e membroId sao obrigatorios");
        }

        grupoRepositorio.findById(grupoId).orElseThrow(GrupoNaoEncontradoException::new);

        boolean membroPertence = participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId, membroId);
        if (!membroPertence) {
            throw new GrupoNaoEncontradoException();
        }
    }

    private List<GrupoMembroEntidade> buscarParticipacoesOrdenadas(UUID grupoId) {
        List<GrupoMembroEntidade> participacoes = new ArrayList<>(participacaoRepositorio.findByGrupoId(grupoId));
        participacoes.sort(Comparator
                .comparing((GrupoMembroEntidade p) -> p.getMembro().getNome())
                .thenComparing(p -> p.getMembro().getId()));
        return participacoes;
    }

    private List<Despesa> carregarDespesasDeDominio(UUID grupoId) {
        List<ParcelaEntidade> parcelasAtivas = parcelaRepositorio
                .findByDespesaGrupoIdAndDespesaStatus(grupoId, StatusDespesa.ATIVA);

        List<DespesaEntidade> despesasUnicas = extrairDespesasUnicas(parcelasAtivas);

        List<Despesa> despesasDominio = new ArrayList<>();
        for (DespesaEntidade despesa : despesasUnicas) {
            List<Despesa.Parcela> parcelasDaDespesa = extrairParcelasDaDespesa(despesa, parcelasAtivas);

            despesasDominio.add(new Despesa(
                    grupoId,
                    despesa.getPagador().getId(),
                    despesa.getDescricao(),
                    despesa.getValorTotal(),
                    despesa.getEstrategia(),
                    parcelasDaDespesa));
        }
        return despesasDominio;
    }

    private List<DespesaEntidade> extrairDespesasUnicas(List<ParcelaEntidade> parcelasAtivas) {
        List<DespesaEntidade> despesas = new ArrayList<>();
        for (ParcelaEntidade parcela : parcelasAtivas) {
            if (!despesas.contains(parcela.getDespesa())) {
                despesas.add(parcela.getDespesa());
            }
        }
        return despesas;
    }

    private List<Despesa.Parcela> extrairParcelasDaDespesa(DespesaEntidade despesa, List<ParcelaEntidade> parcelasAtivas) {
        List<Despesa.Parcela> parcelasDaDespesa = new ArrayList<>();
        for (ParcelaEntidade parcela : parcelasAtivas) {
            if (parcela.getDespesa().equals(despesa)) {
                parcelasDaDespesa.add(new Despesa.Parcela(parcela.getMembro().getId(), parcela.getValor()));
            }
        }
        return parcelasDaDespesa;
    }

    private List<Pagamento> carregarPagamentosDeDominio(UUID grupoId) {
        List<Pagamento> pagamentosDominio = new ArrayList<>();
        for (PagamentoEntidade pagamento : pagamentoRepositorio.findByGrupoIdAndStatus(grupoId, StatusPagamento.ATIVO)) {
            pagamentosDominio.add(new Pagamento(
                    grupoId,
                    pagamento.getDe().getId(),
                    pagamento.getPara().getId(),
                    pagamento.getValor()));
        }
        return pagamentosDominio;
    }
}