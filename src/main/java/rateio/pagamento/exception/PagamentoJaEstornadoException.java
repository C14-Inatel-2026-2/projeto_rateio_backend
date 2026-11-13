package rateio.pagamento.exception;

public class PagamentoJaEstornadoException extends RuntimeException {

    public PagamentoJaEstornadoException () {
        super("Pagamento ja estornado");
    }
}
