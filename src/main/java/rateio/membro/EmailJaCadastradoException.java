package rateio.membro;

public class EmailJaCadastradoException extends RuntimeException {

    public EmailJaCadastradoException() {
        super("Email ja cadastrado");
    }
}