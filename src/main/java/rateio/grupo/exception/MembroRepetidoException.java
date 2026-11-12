package rateio.grupo.exception;

public class MembroRepetidoException extends RuntimeException {

    public MembroRepetidoException() {
        super("Membro repetido no grupo");
    }
}
