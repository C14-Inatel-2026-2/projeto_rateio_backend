package rateio.grupo;

public class GrupoNaoEncontradoException extends RuntimeException {

    public GrupoNaoEncontradoException() {
        super("Grupo nao encontrado");
    }
}
