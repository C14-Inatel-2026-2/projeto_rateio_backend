package rateio.despesa.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rateio.despesa.domain.Despesa;
import rateio.despesa.domain.Despesa.Parcela;
import rateio.despesa.domain.StatusDespesa;
import rateio.despesa.dto.CriarDespesaRequest;
import rateio.despesa.dto.DespesaDetalhe;
import rateio.despesa.dto.DespesaResumo;
import rateio.despesa.dto.EstornoDetalhe;
import rateio.despesa.dto.ParticipanteRequest;
import rateio.despesa.entity.DespesaEntidade;
import rateio.despesa.entity.ParcelaEntidade;
import rateio.despesa.exception.DespesaJaEstornadaException;
import rateio.despesa.exception.DespesaNaoEncontradaException;
import rateio.despesa.repository.DespesaRepository;
import rateio.despesa.repository.ParcelaRepository;
import rateio.grupo.entity.GrupoEntidade;
import rateio.grupo.repository.GrupoMembroRepository;
import rateio.grupo.service.AcessoAoGrupo;
import rateio.membro.entity.Membro;
import rateio.membro.exception.MembroNaoEncontradoException;
import rateio.membro.repository.MembroRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

@Service
public class DespesaService {

    private static final int CASAS_DECIMAIS = 2;

    private final DespesaRepository despesaRepositorio;
    private final ParcelaRepository parcelaRepositorio;
    private final GrupoMembroRepository participacaoRepositorio;
    private final MembroRepository membroRepositorio;
    private final AcessoAoGrupo acesso;
    private final DivisaoService divisao;

    public DespesaService(DespesaRepository despesaRepositorio,
                          ParcelaRepository parcelaRepositorio,
                          GrupoMembroRepository participacaoRepositorio,
                          MembroRepository membroRepositorio,
                          AcessoAoGrupo acesso,
                          DivisaoService divisao) {
        this.despesaRepositorio = despesaRepositorio;
        this.parcelaRepositorio = parcelaRepositorio;
        this.participacaoRepositorio = participacaoRepositorio;
        this.membroRepositorio = membroRepositorio;
        this.acesso = acesso;
        this.divisao = divisao;
    }

    @Transactional
    public DespesaDetalhe criar(UUID registradorId, UUID grupoId, CriarDespesaRequest requisicao) {
        if (registradorId == null || grupoId == null || !preenchida(requisicao)) {
            throw new IllegalArgumentException("Valor invalido");
        }

        GrupoEntidade grupo = acesso.exigirMembro(grupoId, registradorId);
        if (!participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId, requisicao.pagadorId())) {
            throw new IllegalArgumentException("Pagador fora do grupo");
        }
        validarParticipantes(grupoId, requisicao.participantes());

        Despesa despesa = new Despesa(
                grupoId,
                requisicao.pagadorId(),
                requisicao.descricao(),
                requisicao.valorTotal(),
                requisicao.estrategia(),
                calcularParcelas(requisicao));

        DespesaEntidade entidade = new DespesaEntidade();
        entidade.setGrupo(grupo);
        entidade.setRegistrador(buscarMembro(registradorId));
        entidade.setPagador(buscarMembro(despesa.pagadorId()));
        entidade.setDescricao(despesa.descricao());
        entidade.setValorTotal(despesa.valorTotal());
        entidade.setEstrategia(despesa.estrategia());
        entidade.setStatus(StatusDespesa.ATIVA);
        entidade.setCriadaEm(despesa.registradaEm());
        despesaRepositorio.save(entidade);

        List<ParcelaEntidade> parcelas = new ArrayList<>();
        for (Parcela parcela : despesa.parcelas()) {
            ParcelaEntidade parcelaEntidade = new ParcelaEntidade();
            parcelaEntidade.setDespesa(entidade);
            parcelaEntidade.setMembro(buscarMembro(parcela.membroId()));
            parcelaEntidade.setValor(parcela.valor());
            parcelas.add(parcelaEntidade);
        }
        parcelaRepositorio.saveAll(parcelas);

        return DespesaDetalhe.de(entidade, despesa.parcelas());
    }

    @Transactional(readOnly = true)
    public List<DespesaResumo> listar(UUID membroId, UUID grupoId) {
        if (membroId == null || grupoId == null) {
            throw new IllegalArgumentException("grupoId e membroId sao obrigatorios");
        }

        acesso.exigirMembro(grupoId, membroId);

        return despesaRepositorio.findByGrupoId(grupoId).stream()
                .map(DespesaResumo::de)
                .toList();
    }

    @Transactional
    public EstornoDetalhe estornar(UUID grupoId, UUID despesaId, UUID membroId) {
        if (membroId == null || grupoId == null || despesaId == null) {
            throw new IllegalArgumentException("grupoId, membroId e despesaId sao obrigatorios");
        }

        DespesaEntidade entidade = acesso.buscarComoMembro(grupoId, membroId)
                .flatMap(grupo -> despesaRepositorio.findByIdAndGrupoId(despesaId, grupoId))
                .orElseThrow(DespesaNaoEncontradaException::new);
        if (entidade.getStatus() == StatusDespesa.ESTORNADA) {
            throw new DespesaJaEstornadaException();
        }

        Despesa despesa = paraDominio(entidade, grupoId);
        despesa.estornar();

        entidade.setStatus(StatusDespesa.ESTORNADA);
        entidade.setEstornadaEm(despesa.estornadaEm());
        despesaRepositorio.save(entidade);

        return EstornoDetalhe.de(entidade);
    }

    private static boolean preenchida(CriarDespesaRequest requisicao) {
        if (requisicao == null) {
            return false;
        }
        BigDecimal valorTotal = requisicao.valorTotal();
        return requisicao.descricao() != null && !requisicao.descricao().isBlank()
                && valorTotal != null && valorTotal.signum() > 0 && valorTotal.scale() <= CASAS_DECIMAIS
                && requisicao.pagadorId() != null
                && requisicao.estrategia() != null
                && requisicao.participantes() != null && !requisicao.participantes().isEmpty();
    }

    private void validarParticipantes(UUID grupoId, List<ParticipanteRequest> participantes) {
        Set<UUID> ids = new HashSet<>();
        for (ParticipanteRequest participante : participantes) {
            if (participante == null || participante.membroId() == null) {
                throw new IllegalArgumentException("Valor invalido");
            }
            if (!ids.add(participante.membroId())) {
                throw new IllegalArgumentException("Participante repetido");
            }
            if (!participacaoRepositorio.existsByGrupoIdAndMembroId(grupoId, participante.membroId())) {
                throw new IllegalArgumentException("Participante fora do grupo");
            }
        }
    }

    private List<Parcela> calcularParcelas(CriarDespesaRequest requisicao) {
        BigDecimal valorTotal = requisicao.valorTotal();
        List<ParticipanteRequest> participantes = requisicao.participantes();
        return switch (requisicao.estrategia()) {
            case IGUAL -> divisao.dividirIgual(
                    valorTotal, participantes.stream().map(ParticipanteRequest::membroId).toList());
            case POR_PESO -> divisao.dividirPorPeso(
                    valorTotal, porMembro(participantes, ParticipanteRequest::peso));
            case VALORES_FIXOS -> divisao.dividirPorValoresFixos(
                    valorTotal, porMembro(participantes, ParticipanteRequest::valor));
        };
    }

    // Collectors.toMap rejeita valores nulos, e peso ou valor ausente precisa chegar ao
    // DivisaoService para ser recusado la. O LinkedHashMap preserva a ordem dos participantes,
    // que decide quem recebe os centavos de arredondamento.
    private static Map<UUID, BigDecimal> porMembro(List<ParticipanteRequest> participantes,
                                                   Function<ParticipanteRequest, BigDecimal> campo) {
        Map<UUID, BigDecimal> valores = new LinkedHashMap<>();
        for (ParticipanteRequest participante : participantes) {
            valores.put(participante.membroId(), campo.apply(participante));
        }
        return valores;
    }

    private Despesa paraDominio(DespesaEntidade entidade, UUID grupoId) {
        List<Parcela> parcelas = parcelaRepositorio.findByDespesaId(entidade.getId()).stream()
                .map(parcela -> new Parcela(parcela.getMembro().getId(), parcela.getValor()))
                .toList();
        return new Despesa(
                grupoId,
                entidade.getPagador().getId(),
                entidade.getDescricao(),
                entidade.getValorTotal(),
                entidade.getEstrategia(),
                parcelas);
    }

    private Membro buscarMembro(UUID id) {
        return membroRepositorio.findById(id).orElseThrow(MembroNaoEncontradoException::new);
    }
}
