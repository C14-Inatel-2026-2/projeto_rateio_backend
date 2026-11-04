package rateio.config;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import rateio.membro.EmailJaCadastradoException;
import rateio.membro.CredenciaisInvalidasException;

@RestControllerAdvice
public class TratadorDeErros {

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErroResponse entradaInvalida(IllegalArgumentException e) {
        return new ErroResponse(e.getMessage());
    }

    @ExceptionHandler(EmailJaCadastradoException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErroResponse conflito(EmailJaCadastradoException e) {
        return new ErroResponse(e.getMessage());
    }

    @ExceptionHandler(CredenciaisInvalidasException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErroResponse naoAutorizado(CredenciaisInvalidasException e) {
        return new ErroResponse(e.getMessage());
    }
}