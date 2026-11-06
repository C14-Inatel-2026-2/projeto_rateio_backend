package rateio.grupo;

public class GrupoComPoucosMembrosException extends RuntimeException {

    public GrupoComPoucosMembrosException() {
        super("Grupo precisa de ao menos 2 membros");
    }
}