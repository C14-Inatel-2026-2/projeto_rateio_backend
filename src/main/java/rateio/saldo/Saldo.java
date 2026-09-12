package rateio.saldo;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import rateio.despesa.Despesa;
import rateio.pagamento.Pagamento;

public record Saldo(UUID membroId, BigDecimal valor) {
    public boolean ehCredor() {
        throw new UnsupportedOperationException("H3 - nao implementado");
    }

    public boolean ehDevedor() {
        throw new UnsupportedOperationException("H3 - nao implementado");
    }

    public boolean estaQuitado() {
        throw new UnsupportedOperationException("H3 - nao implementado");
    }

    public BigDecimal valorAbsoluto() {
        throw new UnsupportedOperationException("H3 - nao implementado");
    }

    /**
     * H3: consolida o razao do grupo em um saldo por membro.
     *
     * Membros sem movimentacao aparecem com saldo zero, nao sao omitidos.
     * Despesas e pagamentos estornados sao ignorados.
     */
    public static List<Saldo> consolidar(Set<UUID> membros,
                                         Collection<Despesa> despesas,
                                         Collection<Pagamento> pagamentos) {
        throw new UnsupportedOperationException("H3 - nao implementado");
    }

    /** H3: verifica a invariante de soma zero. Usado em teste e em assercao interna. */
    public static boolean somaZero(Collection<Saldo> saldos) {
        throw new UnsupportedOperationException("H3 - nao implementado");
    }

    public static BigDecimal soma(Collection<Saldo> saldos) {
        throw new UnsupportedOperationException("H3 - nao implementado");
    }
}
