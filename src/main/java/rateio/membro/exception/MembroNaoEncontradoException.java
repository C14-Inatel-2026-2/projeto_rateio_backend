package rateio.membro.exception;

public class MembroNaoEncontradoException extends RuntimeException {

    public MembroNaoEncontradoException() {
        super("Membro nao encontrado");
    }
}