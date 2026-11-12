package rateio.pagamento;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rateio.despesa.*;
import rateio.grupo.GrupoMembroRepository;
import rateio.grupo.GrupoRepository;
import rateio.membro.MembroNaoEncontradoException;
import rateio.membro.MembroRepository;

import rateio.quitacao.QuitacaoDetalhe;
import rateio.quitacao.QuitacaoService;
import rateio.saldo.SaldoService;
import rateio.grupo.GrupoEntidade;
import rateio.grupo.GrupoNaoEncontradoException;
import rateio.membro.Membro;

import java.math.BigDecimal;
import java.util.*;
import java.time.Instant;

@Service
public class PagamentoService {

    private final GrupoRepository grupoRepositorio;
    private final GrupoMembroRepository participacaoRepositorio;
    private final MembroRepository membroRepositorio;
    private final QuitacaoService quitacaoService;
    private final PagamentoRepository pagamentoRepositorio;
    private final DespesaRepository despesaRepositorio;

    public PagamentoService(
            GrupoRepository grupoRepositorio,
            GrupoMembroRepository participacaoRepositorio,
            MembroRepository membroRepositorio,
            DespesaRepository despesaRepositorio,
            QuitacaoService quitacaoService,
            PagamentoRepository pagamentoRepositorio) {

        this.grupoRepositorio = grupoRepositorio;
        this.participacaoRepositorio = participacaoRepositorio;
        this.membroRepositorio = membroRepositorio;
        this.quitacaoService = quitacaoService;
        this.despesaRepositorio = despesaRepositorio;
        this.pagamentoRepositorio = pagamentoRepositorio;
    }

    @Transactional
    public PagamentoDetalhe registrar(UUID grupoId, UUID registradorId, UUID paraId, BigDecimal valor){
        if(grupoId == null|| registradorId == null || paraId == null || valor == null){
            throw new IllegalArgumentException("grupoId, registradorId, paraId e valor sao obrigatorios");
        }

        if(valor.signum() <= 0 || valor.scale() > 2 || registradorId.equals(paraId)){
            throw new ValorInvalidoException();
        }

        List <QuitacaoDetalhe> transferencias = quitacaoService.listaTransferencias(registradorId, grupoId);

        if (!participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId, paraId)) {
            throw new MembroNaoEncontradoException();
        }

        QuitacaoDetalhe transferenciaEncontrada = transferencias.stream()
                .filter(transferencia ->
                        transferencia.deId().equals(registradorId)
                                && transferencia.paraId().equals(paraId))
                .findFirst()
                .orElse(null);

        if (transferenciaEncontrada == null || valor.compareTo(transferenciaEncontrada.valor()) > 0) {
            throw new ValorMaiorDividaException();
        }

        new Pagamento(grupoId, registradorId, paraId, valor);

        GrupoEntidade grupo = grupoRepositorio.findById(grupoId)
                .orElseThrow(GrupoNaoEncontradoException::new);
        Membro de = membroRepositorio.findById(registradorId)
                .orElseThrow(MembroNaoEncontradoException::new);
        Membro para = membroRepositorio.findById(paraId)
                .orElseThrow(MembroNaoEncontradoException::new);

        PagamentoEntidade pagamento = new PagamentoEntidade();
        pagamento.setGrupo(grupo);
        pagamento.setDe(de);
        pagamento.setPara(para);
        pagamento.setValor(valor);
        pagamento.setStatus(StatusPagamento.ATIVO);
        pagamento.setCriadoEm(Instant.now());
        pagamentoRepositorio.save(pagamento);

        return new PagamentoDetalhe(
                pagamento.getId(),
                de.getId(),
                para.getId(),
                pagamento.getValor(),
                pagamento.getStatus(),
                pagamento.getCriadoEm());
    }

    @Transactional
    public EstornoPagamentoDetalhe estornar(UUID grupoId, UUID pagamentoId, UUID membroId) {
        if (membroId == null || grupoId == null || pagamentoId == null) {
            throw new IllegalArgumentException("grupoId, membroId e pagamentoId sao obrigatorios");
        }

        grupoRepositorio.findById(grupoId).orElseThrow(PagamentoNaoEncontradoException::new);

        if (!participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId, membroId)) {
            throw new PagamentoNaoEncontradoException();
        }

        PagamentoEntidade pagamento = pagamentoRepositorio.findByIdAndGrupoId(pagamentoId, grupoId)
                .orElseThrow(PagamentoNaoEncontradoException::new);

        if (pagamento.getStatus() == StatusPagamento.ESTORNADO) {
            throw new PagamentoJaEstornadoException();
        }

        Pagamento pagamentoDominio = new Pagamento(
                grupoId,
                pagamento.getDe().getId(),
                pagamento.getPara().getId(),
                pagamento.getValor());

        pagamentoDominio.estornar();
        pagamento.setStatus(StatusPagamento.ESTORNADO);
        pagamento.setEstornadoEm(pagamentoDominio.estornadoEm());
        pagamentoRepositorio.save(pagamento);

        return new EstornoPagamentoDetalhe(pagamento.getId(), pagamento.getStatus());
    }

    @Transactional(readOnly = true)
    public List<ExtratoDetalhe> extrato(UUID grupoId, UUID membroId) {
        if (membroId == null || grupoId == null) {
            throw new IllegalArgumentException("grupoId e membroId sao obrigatorios");
        }

        grupoRepositorio.findById(grupoId).orElseThrow(GrupoNaoEncontradoException::new);

        if (!participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId, membroId)) {
            throw new GrupoNaoEncontradoException();
        }

        List<PagamentoEntidade> pagamentos = pagamentoRepositorio.findByGrupoId(grupoId);
        List<DespesaEntidade> despesas = despesaRepositorio.findByGrupoId(grupoId);

        List<ExtratoDetalhe> extrato = new ArrayList<>();

        for (PagamentoEntidade pagamento : pagamentos) {
            extrato.add(new ExtratoDetalhe(
                    TipoMovimentacao.PAGAMENTO,
                    pagamento.getId(),
                    pagamento.getDe().getId(),
                    pagamento.getDe().getNome(),
                    pagamento.getValor(),
                    pagamento.getStatus().name(),
                    pagamento.getCriadoEm()));
        }

        for (DespesaEntidade despesa : despesas) {
            extrato.add(new ExtratoDetalhe(
                    TipoMovimentacao.DESPESA,
                    despesa.getId(),
                    despesa.getPagador().getId(),
                    despesa.getPagador().getNome(),
                    despesa.getValorTotal(),
                    despesa.getStatus().name(),
                    despesa.getCriadaEm()));
        }

        extrato.sort(Comparator.comparing(ExtratoDetalhe::data).thenComparing(ExtratoDetalhe::id));

        return extrato;
    }

}
