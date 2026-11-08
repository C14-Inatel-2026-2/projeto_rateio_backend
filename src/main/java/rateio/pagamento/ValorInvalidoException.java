package rateio.pagamento;

public class ValorInvalidoException extends RuntimeException {

    public ValorInvalidoException () {
        super("Valor invalido");
    }
}
