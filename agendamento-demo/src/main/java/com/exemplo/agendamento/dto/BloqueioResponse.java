package com.exemplo.agendamento.dto;

import com.exemplo.agendamento.model.Bloqueio;

import java.time.LocalDateTime;

public record BloqueioResponse(Long id, Long barbeiroId, LocalDateTime inicio, LocalDateTime fim, String motivo) {

    public static BloqueioResponse de(Bloqueio b) {
        return new BloqueioResponse(b.getId(), b.getBarbeiro().getId(), b.getInicio(), b.getFim(), b.getMotivo());
    }
}
