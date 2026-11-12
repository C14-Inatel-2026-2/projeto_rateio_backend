package rateio.despesa.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class Despesa {

    public enum EstrategiaDivisao {
        IGUAL,
        POR_PESO,
        VALORES_FIXOS
    }

    public record Parcela(UUID membroId, BigDecimal valor) {
    }

    private final UUID id;
    private final UUID grupoId;
    private final UUID pagadorId;
    private final String descricao;
    private final BigDecimal valorTotal;
    private final EstrategiaDivisao estrategia;
    private final List<Parcela> parcelas;
    private final Instant registradaEm;
    private Instant estornadaEm;

    public Despesa(UUID grupoId,
                   UUID pagadorId,
                   String descricao,
                   BigDecimal valorTotal,
                   EstrategiaDivisao estrategia,
                   List<Parcela> parcelas) {
        if (grupoId == null || pagadorId == null) {
            throw new IllegalArgumentException("Grupo e pagador sao obrigatorios.");
        }
        if (descricao == null || descricao.isBlank()) {
            throw new IllegalArgumentException("A descricao e obrigatoria.");
        }
        if (valorTotal == null || valorTotal.signum() <= 0) {
            throw new IllegalArgumentException("O valor total deve ser positivo.");
        }
        if (estrategia == null) {
            throw new IllegalArgumentException("A estrategia de divisao e obrigatoria.");
        }
        if (parcelas == null || parcelas.isEmpty()) {
            throw new IllegalArgumentException("A despesa precisa de ao menos uma parcela.");
        }
        this.id = UUID.randomUUID();
        this.grupoId = grupoId;
        this.pagadorId = pagadorId;
        this.descricao = descricao;
        this.valorTotal = valorTotal;
        this.estrategia = estrategia;
        this.parcelas = List.copyOf(parcelas);
        this.registradaEm = Instant.now();
        validarParcelas();
    }

    private void validarParcelas() {
        Set<UUID> membros = new HashSet<>();
        for (Parcela parcela : parcelas) {
            if (parcela.membroId() == null || parcela.valor() == null || parcela.valor().signum() < 0) {
                throw new IllegalArgumentException("Parcela invalida.");
            }
            if (!membros.add(parcela.membroId())) {
                throw new IllegalArgumentException("Membro repetido nas parcelas.");
            }
        }
        if (!parcelasFecham()) {
            throw new IllegalArgumentException("A soma das parcelas difere do valor total.");
        }
    }

    public boolean parcelasFecham() {
        return somaDasParcelas().compareTo(valorTotal) == 0;
    }

    public BigDecimal somaDasParcelas() {
        return parcelas.stream()
                .map(Parcela::valor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal parcelaDe(UUID membroId) {
        return parcelas.stream()
                .filter(parcela -> parcela.membroId().equals(membroId))
                .map(Parcela::valor)
                .findFirst()
                .orElse(BigDecimal.ZERO);
    }

    public boolean participa(UUID membroId) {
        return parcelas.stream().anyMatch(parcela -> parcela.membroId().equals(membroId));
    }

    // O estorno e logico: a despesa continua no historico e apenas deixa de contar no saldo.
    public void estornar() {
        if (estornada()) {
            throw new IllegalStateException("A despesa ja foi estornada.");
        }
        this.estornadaEm = Instant.now();
    }

    public boolean estaAtiva() {
        return !estornada();
    }

    public boolean estornada() {
        return estornadaEm != null;
    }

    public UUID id() {
        return id;
    }

    public UUID grupoId() {
        return grupoId;
    }

    public UUID pagadorId() {
        return pagadorId;
    }

    public String descricao() {
        return descricao;
    }

    public BigDecimal valorTotal() {
        return valorTotal;
    }

    public EstrategiaDivisao estrategia() {
        return estrategia;
    }

    public List<Parcela> parcelas() {
        return parcelas;
    }

    public Instant registradaEm() {
        return registradaEm;
    }

    public Instant estornadaEm() {
        return estornadaEm;
    }
}
