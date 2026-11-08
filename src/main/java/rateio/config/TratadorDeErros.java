package rateio.config;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;
import rateio.membro.EmailJaCadastradoException;
import rateio.membro.CredenciaisInvalidasException;

import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import rateio.grupo.GrupoComPoucosMembrosException;
import rateio.grupo.GrupoNaoEncontradoException;
import rateio.grupo.MembroRepetidoException;
import rateio.membro.MembroNaoEncontradoException;
import rateio.despesa.DespesaJaEstornadaException;
import rateio.despesa.DespesaNaoEncontradaException;
import rateio.grupo.GrupoComDespesasException;


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

    @ExceptionHandler({GrupoNaoEncontradoException.class, MembroNaoEncontradoException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErroResponse naoEncontrado(RuntimeException e) {
        return new ErroResponse(e.getMessage());
    }

    @ExceptionHandler({MembroRepetidoException.class, GrupoComPoucosMembrosException.class, GrupoComDespesasException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErroResponse conflitoDeGrupo(RuntimeException e) {
        return new ErroResponse(e.getMessage());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErroResponse parametroAusente(MissingServletRequestParameterException e) {
        return new ErroResponse(e.getParameterName() + " e obrigatorio");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErroResponse parametroInvalido(MethodArgumentTypeMismatchException e) {
        return new ErroResponse(e.getName() + " invalido");
    }

    @ExceptionHandler(DespesaNaoEncontradaException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErroResponse despesaNaoEncontrada(DespesaNaoEncontradaException e) {
        return new ErroResponse(e.getMessage());
    }

    @ExceptionHandler(DespesaJaEstornadaException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErroResponse despesaJaEstornada(DespesaJaEstornadaException e) {
        return new ErroResponse(e.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErroResponse corpoInvalido(HttpMessageNotReadableException e) {
        return new ErroResponse("Corpo da requisicao invalido");
    }
}