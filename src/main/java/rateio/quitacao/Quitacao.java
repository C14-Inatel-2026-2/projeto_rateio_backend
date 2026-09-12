package rateio.quitacao;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import rateio.saldo.Saldo;

public class Quitacao {
    public record Transferencia(UUID deId, UUID paraId, BigDecimal valor) {
    }

    private final List<Transferencia> transferencias;

    private Quitacao(List<Transferencia> transferencias) {
        throw new UnsupportedOperationException("H4 - nao implementado");
    }

    /** H4: calcula o plano a partir dos saldos consolidados do grupo. */
    public static Quitacao apartirDe(Collection<Saldo> saldos) {
        throw new UnsupportedOperationException("H4 - nao implementado");
    }

    /** Retorna copia imutavel. */
    public List<Transferencia> transferencias() {
        throw new UnsupportedOperationException("H4 - nao implementado");
    }

    public int quantidadeDeTransferencias() {
        throw new UnsupportedOperationException("H4 - nao implementado");
    }

    public BigDecimal totalTransferido() {
        throw new UnsupportedOperationException("H4 - nao implementado");
    }

    public boolean envolve(UUID membroId) {
        throw new UnsupportedOperationException("H4 - nao implementado");
    }

    public boolean estaVazia() {
        throw new UnsupportedOperationException("H4 - nao implementado");
    }
}
