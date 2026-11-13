package rateio.membro.exception;

public class EmailJaCadastradoException extends RuntimeException {

    public EmailJaCadastradoException() {
        super("Email ja cadastrado");
    }
}