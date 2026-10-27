package rateio.despesa;

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

    private UUID id;
    private UUID grupoId;
    private UUID pagadorId;
    private String descricao;
    private BigDecimal valorTotal;
    private EstrategiaDivisao estrategia;
    private List<Parcela> parcelas;
    private Instant registradaEm;
    private boolean estornada;
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
        this.estornada = false;
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

    /** H2: true quando a soma das parcelas bate exatamente com o valor total. */
    public boolean parcelasFecham() {
        if (valorTotal == null){
            return false;
        }
        return somaDasParcelas().compareTo(valorTotal) == 0;
    }

    /** H2: soma das parcelas, usada na validacao de fechamento. */
    public BigDecimal somaDasParcelas() {
        if(parcelas == null || parcelas.isEmpty()){
            return BigDecimal.ZERO;
        }
        BigDecimal soma = BigDecimal.ZERO;
        for (Parcela parcela : parcelas) {
            soma = soma.add(parcela.valor());
        }
        return soma;
    }

    /** H3: valor devido por um membro; zero se ele nao participa da despesa. */
    public BigDecimal parcelaDe(UUID membroId) {
        if (membroId == null || parcelas == null) {
            return BigDecimal.ZERO;
        }
        for (Parcela parcela : parcelas){
            if(parcela.membroId().equals(membroId)){
                return parcela.valor();
            }
        }
        return BigDecimal.ZERO;
    }

    /** H5: estorno logico. A despesa permanece no historico. */
    public void estornar() {
        if (estornada) {
            throw new IllegalStateException("A despesa ja foi estornada.");
        }
        this.estornada = true;
        this.estornadaEm = Instant.now();
    }

    /** H3: despesas estornadas nao entram no calculo de saldo. */
    public boolean estaAtiva() {
        return !this.estornada;
    }

    public boolean participa(UUID membroId) {
        if (membroId == null || parcelas == null) {
            return false;
        }
        for (Parcela parcela : parcelas) {
            if (parcela.membroId().equals(membroId)) {
                return true;
            }
        }
        return false;
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

    /** Retorna copia imutavel. */
    public List<Parcela> parcelas() {
        return parcelas;
    }

    public Instant registradaEm() {
        return registradaEm;
    }

    public boolean estornada() {
        return estornada;
    }

    public Instant estornadaEm() {
        return estornadaEm;
    }
}
