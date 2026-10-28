package rateio.saldo;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import rateio.despesa.Despesa;
import rateio.pagamento.Pagamento;

public record Saldo(UUID membroId, BigDecimal valor) {
    public Saldo {
        if (membroId == null || valor == null) {
            throw new IllegalArgumentException("O ID do membro e o valor são obrigatorios.");
        }

    }

    public boolean ehCredor() {
        return valor.signum() > 0;
    }

    public boolean ehDevedor() {
        return valor.signum() < 0;
    }

    public boolean estaQuitado() {
        return valor.signum() == 0;
    }

    public BigDecimal valorAbsoluto() {
        return valor.abs();
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
