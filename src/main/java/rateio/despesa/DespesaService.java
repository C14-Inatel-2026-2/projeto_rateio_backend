package rateio.despesa;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rateio.grupo.*;
import rateio.membro.Membro;
import rateio.membro.MembroNaoEncontradoException;
import rateio.membro.MembroRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Service
public class DespesaService {

    private final DespesaRepository despesaRepositorio;
    private final ParcelaRepository parcelaRepositorio;
    private final GrupoRepository grupoRepositorio;
    private final GrupoMembroRepository participacaoRepositorio;
    private final MembroRepository membroRepositorio;
    private final DivisaoService divisao = new DivisaoService();

    public DespesaService(DespesaRepository despesaRepositorio,
                          ParcelaRepository parcelaRepositorio,
                          GrupoRepository grupoRepositorio,
                          GrupoMembroRepository participacaoRepositorio,
                          MembroRepository membroRepositorio) {
        this.despesaRepositorio = despesaRepositorio;
        this.parcelaRepositorio = parcelaRepositorio;
        this.grupoRepositorio = grupoRepositorio;
        this.participacaoRepositorio = participacaoRepositorio;
        this.membroRepositorio = membroRepositorio;
    }
    @Transactional
    public DespesaDetalhe criar(UUID registradorId,
                                UUID grupoId,
                                String descricao,
                                BigDecimal valorTotal,
                                UUID pagadorId,
                                Despesa.EstrategiaDivisao estrategiaDivisao,
                                List<ParticipanteRequest> participantes){
        if (registradorId == null || grupoId == null || descricao == null || descricao.isBlank() ||
                valorTotal == null || valorTotal.compareTo(BigDecimal.ZERO) <= 0 ||
                valorTotal.scale() > 2 || pagadorId == null || estrategiaDivisao == null|| participantes == null ||
                participantes.isEmpty()) {
            throw new IllegalArgumentException("Valor invalido");
        }

        GrupoEntidade grupo = grupoRepositorio.findById(grupoId)
                .orElseThrow(GrupoNaoEncontradoException::new);

        boolean registradorPertence = participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId, registradorId);
        if (!registradorPertence) {
            throw new GrupoNaoEncontradoException();
        }

        boolean pagadorPertence = participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId, pagadorId);
        if (!pagadorPertence) {
            throw new IllegalArgumentException("Pagador fora do grupo");
        }

        Set<UUID> idsParticipantes = new HashSet<>();
        for (ParticipanteRequest p : participantes) {
            if (p == null || p.membroId() == null) {
                throw new IllegalArgumentException("Valor invalido");
            }
            if (!idsParticipantes.add(p.membroId())) {
                throw new IllegalArgumentException("Participante repetido");
            }
            boolean participantePertence = participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId, p.membroId());
            if (!participantePertence) {
                throw new IllegalArgumentException("Participante fora do grupo");
            }
        }

        Map<UUID, BigDecimal> pesosMap = new LinkedHashMap<>();
        for (ParticipanteRequest p : participantes) {
            if (estrategiaDivisao == Despesa.EstrategiaDivisao.POR_PESO) {
                if (p.peso() == null || p.peso().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException("Valor invalido");
                }
                pesosMap.put(p.membroId(), p.peso());
            } else if (estrategiaDivisao == Despesa.EstrategiaDivisao.VALORES_FIXOS) {
                if (p.valor() == null || p.valor().compareTo(BigDecimal.ZERO) <= 0 || p.valor().scale() > 2) {
                    throw new IllegalArgumentException("Valor invalido");
                }
            }
        }

        List<Despesa.Parcela> parcelas = new ArrayList<>();
        if (estrategiaDivisao == Despesa.EstrategiaDivisao.POR_PESO) {
            parcelas = divisao.dividirPorPeso(valorTotal, pesosMap);
        }

        if (estrategiaDivisao == Despesa.EstrategiaDivisao.VALORES_FIXOS) {
            BigDecimal somaParcelas = BigDecimal.ZERO;

            for (ParticipanteRequest p : participantes) {
                if (p.valor() != null) {
                    somaParcelas = somaParcelas.add(p.valor());
                    parcelas.add(new Despesa.Parcela(p.membroId(), p.valor()));
                }
            }
            if (somaParcelas.compareTo(valorTotal) != 0) {
                throw new IllegalArgumentException("Soma das parcelas diferente do total");
            }
        }

        if (estrategiaDivisao == Despesa.EstrategiaDivisao.IGUAL) {
            List<UUID> membrosIds = new ArrayList<>();
            for (ParticipanteRequest p : participantes) {
                membrosIds.add(p.membroId());
            }
            parcelas = divisao.dividirIgual(valorTotal, membrosIds);
        }

        new Despesa(grupoId, pagadorId, descricao, valorTotal, estrategiaDivisao, parcelas);

        List<ParcelaDetalhe> parcelasDetalhe = new ArrayList<>();

        for (Despesa.Parcela parcelaCalculada : parcelas) {
            ParcelaDetalhe detalhe = new ParcelaDetalhe(
                    parcelaCalculada.membroId(),
                    parcelaCalculada.valor()
            );
            parcelasDetalhe.add(detalhe);
        }

        Membro registrador = membroRepositorio.findById(registradorId)
                .orElseThrow(MembroNaoEncontradoException::new);

        Membro pagador = membroRepositorio.findById(pagadorId)
                .orElseThrow(MembroNaoEncontradoException::new);

        DespesaEntidade despesa = new DespesaEntidade();
        despesa.setGrupo(grupo);
        despesa.setPagador(pagador);
        despesa.setRegistrador(registrador);
        despesa.setDescricao(descricao);
        despesa.setValorTotal(valorTotal);
        despesa.setEstrategia(estrategiaDivisao);
        despesa.setStatus(StatusDespesa.ATIVA);
        despesa.setCriadaEm(Instant.now());
        despesaRepositorio.save(despesa);

        List<ParcelaEntidade> parcelasEntidade = new ArrayList<>();
        for (Despesa.Parcela pCalculada : parcelas) {
            Membro membro = membroRepositorio.findById(pCalculada.membroId())
                    .orElseThrow(MembroNaoEncontradoException::new);

            ParcelaEntidade parcelaEntidade = new ParcelaEntidade();
            parcelaEntidade.setDespesa(despesa);
            parcelaEntidade.setMembro(membro);
            parcelaEntidade.setValor(pCalculada.valor());
            parcelasEntidade.add(parcelaEntidade);
        }
        parcelaRepositorio.saveAll(parcelasEntidade);

        DespesaDetalhe despesaDetalhe = new DespesaDetalhe(
                despesa.getId(),
                despesa.getDescricao(),
                despesa.getValorTotal(),
                despesa.getPagador().getId(),
                despesa.getEstrategia(),
                despesa.getStatus(),
                despesa.getCriadaEm(),
                parcelasDetalhe);

        return despesaDetalhe;
    }

    @Transactional(readOnly = true)
    public List<DespesaResumo> listar(UUID membroId, UUID grupoId){
        if (membroId == null || grupoId == null ) {
            throw new IllegalArgumentException("grupoId e membroId sao obrigatorios");
        }

        grupoRepositorio.findById(grupoId).orElseThrow(GrupoNaoEncontradoException::new);

        boolean membroPertence = participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId, membroId);
        if(!membroPertence){
            throw new GrupoNaoEncontradoException();
        }

        List<DespesaResumo> despesasResumo = new ArrayList<>();
        for (DespesaEntidade despesa : despesaRepositorio.findByGrupoId(grupoId)) {
            despesasResumo.add(new DespesaResumo(
                    despesa.getId(),
                    despesa.getDescricao(),
                    despesa.getValorTotal(),
                    despesa.getPagador().getId(),
                    despesa.getStatus(),
                    despesa.getCriadaEm()));
        }
        return despesasResumo;

    }

}
