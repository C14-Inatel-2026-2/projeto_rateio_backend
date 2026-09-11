package rateio.despesa;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
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
        throw new UnsupportedOperationException("H2 - nao implementado");
    }

    /** H2: true quando a soma das parcelas bate exatamente com o valor total. */
    public boolean parcelasFecham() {
        throw new UnsupportedOperationException("H2 - nao implementado");
    }

    /** H2: soma das parcelas, usada na validacao de fechamento. */
    public BigDecimal somaDasParcelas() {
        throw new UnsupportedOperationException("H2 - nao implementado");
    }

    /** H3: valor devido por um membro; zero se ele nao participa da despesa. */
    public BigDecimal parcelaDe(UUID membroId) {
        throw new UnsupportedOperationException("H2 - nao implementado");
    }

    /** H5: estorno logico. A despesa permanece no historico. */
    public void estornar() {
        throw new UnsupportedOperationException("H5 - nao implementado");
    }

    /** H3: despesas estornadas nao entram no calculo de saldo. */
    public boolean estaAtiva() {
        throw new UnsupportedOperationException("H5 - nao implementado");
    }

    public boolean participa(UUID membroId) {
        throw new UnsupportedOperationException("H2 - nao implementado");
    }

    public UUID id() {
        throw new UnsupportedOperationException("H2 - nao implementado");
    }

    public UUID grupoId() {
        throw new UnsupportedOperationException("H2 - nao implementado");
    }

    public UUID pagadorId() {
        throw new UnsupportedOperationException("H2 - nao implementado");
    }

    public String descricao() {
        throw new UnsupportedOperationException("H2 - nao implementado");
    }

    public BigDecimal valorTotal() {
        throw new UnsupportedOperationException("H2 - nao implementado");
    }

    public EstrategiaDivisao estrategia() {
        throw new UnsupportedOperationException("H2 - nao implementado");
    }

    /** Retorna copia imutavel. */
    public List<Parcela> parcelas() {
        throw new UnsupportedOperationException("H2 - nao implementado");
    }

    public Instant registradaEm() {
        throw new UnsupportedOperationException("H2 - nao implementado");
    }
}
