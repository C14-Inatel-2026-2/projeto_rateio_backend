package rateio.grupo;

public class GrupoComDespesasException extends RuntimeException {

    public GrupoComDespesasException() {
        super("Grupo com despesas nao permite remover membros");
    }
}
