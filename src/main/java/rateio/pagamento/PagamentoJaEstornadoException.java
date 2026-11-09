package rateio.pagamento;

public class PagamentoJaEstornadoException extends RuntimeException {

    public PagamentoJaEstornadoException () {
        super("Pagamento ja estornado");
    }
}
