package rateio.pagamento.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rateio.grupo.repository.GrupoMembroRepository;
import rateio.grupo.repository.GrupoRepository;
import rateio.membro.exception.MembroNaoEncontradoException;
import rateio.membro.repository.MembroRepository;
import rateio.despesa.repository.DespesaRepository;
import rateio.despesa.entity.DespesaEntidade;
import rateio.grupo.entity.GrupoEntidade;

import rateio.pagamento.domain.Pagamento;
import rateio.pagamento.domain.StatusPagamento;
import rateio.pagamento.domain.TipoMovimentacao;
import rateio.pagamento.dto.EstornoPagamentoDetalhe;
import rateio.pagamento.dto.ExtratoDetalhe;
import rateio.pagamento.dto.PagamentoDetalhe;
import rateio.pagamento.entity.PagamentoEntidade;
import rateio.pagamento.exception.PagamentoJaEstornadoException;
import rateio.pagamento.exception.PagamentoNaoEncontradoException;
import rateio.pagamento.exception.ValorInvalidoException;
import rateio.pagamento.exception.ValorMaiorDividaException;
import rateio.pagamento.repository.PagamentoRepository;
import rateio.quitacao.dto.QuitacaoDetalhe;
import rateio.quitacao.service.QuitacaoService;
import rateio.saldo.service.SaldoService;
import rateio.grupo.exception.GrupoNaoEncontradoException;
import rateio.membro.entity.Membro;

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

    public PagamentoService(GrupoRepository grupoRepositorio, GrupoMembroRepository participacaoRepositorio, MembroRepository membroRepositorio, DespesaRepository despesaRepositorio, QuitacaoService quitacaoService, SaldoService saldoService, PagamentoRepository pagamentoRepositorio, DespesaRepository despesaRepositorio1) {
        this.grupoRepositorio = grupoRepositorio;
        this.participacaoRepositorio = participacaoRepositorio;
        this.membroRepositorio = membroRepositorio;
        this.quitacaoService = quitacaoService;
        this.despesaRepositorio = despesaRepositorio1;
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

        QuitacaoDetalhe transferenciaEncontrada = null;

        for(QuitacaoDetalhe transferencia : transferencias){
            if(transferencia.deId().equals(registradorId) && transferencia.paraId().equals(paraId)){
                transferenciaEncontrada = transferencia;
                break;
            }
        }

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
