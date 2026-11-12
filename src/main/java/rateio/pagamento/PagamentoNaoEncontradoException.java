package rateio.pagamento;

public class PagamentoNaoEncontradoException extends RuntimeException {

    public PagamentoNaoEncontradoException () {
        super("Pagamento nao encontrado");
    }
}
