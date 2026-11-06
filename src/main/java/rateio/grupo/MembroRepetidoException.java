package rateio.grupo;

public class MembroRepetidoException extends RuntimeException {

    public MembroRepetidoException() {
        super("Membro repetido no grupo");
    }
}
