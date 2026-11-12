package rateio.grupo.exception;

import rateio.grupo.domain.Grupo;

public class GrupoNaoEncontradoException extends RuntimeException {

    public GrupoNaoEncontradoException() {
        super("Grupo nao encontrado");
    }
}
