package rateio.despesa;

public class DespesaJaEstornadaException extends RuntimeException{

    public DespesaJaEstornadaException() {
        super("Despesa ja estornada");
    }
}
