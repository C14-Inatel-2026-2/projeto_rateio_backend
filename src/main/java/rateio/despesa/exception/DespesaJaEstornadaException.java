package rateio.despesa.exception;

import rateio.despesa.domain.Despesa;

public class DespesaJaEstornadaException extends RuntimeException{

    public DespesaJaEstornadaException() {
        super("Despesa ja estornada");
    }
}
