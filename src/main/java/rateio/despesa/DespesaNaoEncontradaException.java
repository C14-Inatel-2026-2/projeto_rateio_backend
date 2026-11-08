package rateio.despesa;

public class DespesaNaoEncontradaException extends RuntimeException {

    public DespesaNaoEncontradaException() {
        super("Despesa nao encontrada");
    }
}