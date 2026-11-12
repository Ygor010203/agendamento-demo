package com.exemplo.agendamento.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record RemarcarRequest(@NotNull LocalDateTime novoInicio) {
}
