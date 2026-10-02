package rateio.quitacao;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.PriorityQueue;
import java.util.UUID;


import rateio.saldo.Saldo;

public class Quitacao {
    public record Transferencia(UUID deId, UUID paraId, BigDecimal valor) {
    }
    private final List<Transferencia> transferencias;

    private Quitacao(List<Transferencia> transferencias) {
        this.transferencias = transferencias;
    }

    /** H4: calcula o plano a partir dos saldos consolidados do grupo. */
    public static Quitacao apartirDe(Collection<Saldo> saldos) {
        BigDecimal somaTotal = saldos.stream()
                .map(Saldo::valor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (somaTotal.compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalArgumentException(
                    "Saldos inconsistentes: a soma deveria ser zero, mas foi " + somaTotal);
        }

        record Pendencia(UUID membroId, BigDecimal valor) {}

        PriorityQueue<Pendencia> credores = new PriorityQueue<>(
                (a, b) -> b.valor().compareTo(a.valor()));
        PriorityQueue<Pendencia> devedores = new PriorityQueue<>(
                (a, b) -> a.valor().compareTo(b.valor()));

        for (Saldo saldo : saldos) {
            int sinal = saldo.valor().signum();
            if (sinal > 0) {
                credores.add(new Pendencia(saldo.membroId(), saldo.valor()));
            } else if (sinal < 0) {
                devedores.add(new Pendencia(saldo.membroId(), saldo.valor()));
            }
        }

        List<Transferencia> resultado = new ArrayList<>();

        while (!credores.isEmpty() && !devedores.isEmpty()) {
            Pendencia credor = credores.poll();
            Pendencia devedor = devedores.poll();

            BigDecimal valorDevido = devedor.valor().abs();
            BigDecimal valorTransferido = credor.valor().min(valorDevido);

            resultado.add(new Transferencia(devedor.membroId(), credor.membroId(), valorTransferido));

            BigDecimal saldoCredorRestante = credor.valor().subtract(valorTransferido);
            BigDecimal saldoDevedorRestante = devedor.valor().add(valorTransferido);

            if (saldoCredorRestante.signum() > 0) {
                credores.add(new Pendencia(credor.membroId(), saldoCredorRestante));
            }
            if (saldoDevedorRestante.signum() < 0) {
                devedores.add(new Pendencia(devedor.membroId(), saldoDevedorRestante));
            }
        }

        return new Quitacao(resultado);
    }

    /** Retorna copia imutavel. */
    public List<Transferencia> transferencias() {
        return List.copyOf(transferencias);
    }

    public int quantidadeDeTransferencias() {
        return transferencias.size();
    }

    public BigDecimal totalTransferido() {
        return transferencias.stream()
                .map(Transferencia::valor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean envolve(UUID membroId) {
        return transferencias.stream()
                .anyMatch(t -> t.deId().equals(membroId) || t.paraId().equals(membroId));
    }

    public boolean estaVazia() {
        return transferencias.isEmpty();
    }
}
