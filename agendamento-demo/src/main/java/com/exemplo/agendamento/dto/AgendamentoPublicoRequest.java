package com.exemplo.agendamento.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/** Pedido feito pelo próprio cliente na página pública: não exige cadastro prévio. */
public record AgendamentoPublicoRequest(
        @NotBlank String nome,
        @NotBlank String telefone,
        @NotNull Long servicoId,
        @NotNull Long barbeiroId,
        @NotNull LocalDateTime inicio) {
}
