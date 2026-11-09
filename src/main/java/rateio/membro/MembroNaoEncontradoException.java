package rateio.membro;

public class MembroNaoEncontradoException extends RuntimeException {

    public MembroNaoEncontradoException() {
        super("Membro nao encontrado");
    }

    public MembroNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}