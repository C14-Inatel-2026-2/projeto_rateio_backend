package rateio.pagamento.exception;

public class ValorMaiorDividaException extends RuntimeException {

    public ValorMaiorDividaException () {
        super("Valor maior que a divida");
    }
}
