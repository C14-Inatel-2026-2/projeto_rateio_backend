package rateio.despesa.exception;

import rateio.despesa.domain.Despesa;

public class DespesaNaoEncontradaException extends RuntimeException {

    public DespesaNaoEncontradaException() {
        super("Despesa nao encontrada");
    }
}
