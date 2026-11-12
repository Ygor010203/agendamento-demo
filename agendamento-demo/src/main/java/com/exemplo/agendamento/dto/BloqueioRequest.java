package com.exemplo.agendamento.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record BloqueioRequest(
        @NotNull LocalDateTime inicio,
        @NotNull LocalDateTime fim,
        String motivo) {
}
