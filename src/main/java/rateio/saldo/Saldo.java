package rateio.saldo;

import java.math.BigDecimal;
import java.util.ArrayList;
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
        List<Saldo> saldos = new ArrayList<>();
        for (UUID membroId : membros) {
            BigDecimal valor = calcularSaldoDe(membroId, despesas, pagamentos);
            saldos.add(new Saldo(membroId, valor));
        }
        return saldos;
    }

    private static BigDecimal calcularSaldoDe(UUID membroId,
                                              Collection<Despesa> despesas,
                                              Collection<Pagamento> pagamentos) {
        BigDecimal saldoDoMembro = BigDecimal.ZERO;

        for (Despesa despesa : despesas) {
            if (despesa.estaAtiva()) {
                if (membroId.equals(despesa.pagadorId())) {
                    saldoDoMembro = saldoDoMembro.add(despesa.valorTotal());
                }
                saldoDoMembro = saldoDoMembro.subtract(despesa.parcelaDe(membroId));
            }
        }

        for (Pagamento pagamento : pagamentos) {
            if (pagamento.estaAtivo()) {
                saldoDoMembro = saldoDoMembro.add(pagamento.efeitoSobre(membroId));
            }
        }

        return saldoDoMembro;
    }

    public static boolean somaZero(Collection<Saldo> saldos) {
        return soma(saldos).signum() == 0;
    }

    public static BigDecimal soma(Collection<Saldo> saldos) {
        BigDecimal total = BigDecimal.ZERO;
        for (Saldo saldo : saldos) {
            total = total.add(saldo.valor());
        }
        return total;
    }
}
