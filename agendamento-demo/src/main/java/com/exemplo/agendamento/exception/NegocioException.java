package com.exemplo.agendamento.exception;

/** Violação de regra de negócio (horário inválido, status inválido etc.). */
public class NegocioException extends RuntimeException {
    public NegocioException(String mensagem) {
        super(mensagem);
    }
}
