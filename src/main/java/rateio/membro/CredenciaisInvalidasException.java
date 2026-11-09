package rateio.membro;

public class CredenciaisInvalidasException extends RuntimeException {

    public CredenciaisInvalidasException() {
        super("Email ou senha invalidos");
    }

    public CredenciaisInvalidasException(String mensagem) {
        super(mensagem);
    }
}