package rateio.grupo.exception;

import rateio.grupo.domain.Grupo;

public class GrupoComPoucosMembrosException extends RuntimeException {

    public GrupoComPoucosMembrosException() {
        super("Grupo precisa de ao menos 2 membros");
    }
}
