package rateio.pagamento;

public class ValorMaiorDividaException extends RuntimeException {

    public ValorMaiorDividaException () {
        super("Valor maior que a divida");
    }
}
