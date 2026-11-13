package rateio.pagamento.exception;

public class PagamentoNaoEncontradoException extends RuntimeException {

    public PagamentoNaoEncontradoException () {
        super("Pagamento nao encontrado");
    }
}
