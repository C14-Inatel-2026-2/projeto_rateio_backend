package rateio.grupo.exception;

import rateio.grupo.domain.Grupo;

public class GrupoComDespesasException extends RuntimeException {

    public GrupoComDespesasException() {
        super("Grupo com despesas nao permite remover membros");
    }
}
