package com.exemplo.agendamento.dto;

import com.exemplo.agendamento.model.Barbeiro;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;

/** O que o cliente pode ver do barbeiro (sem comissão e sem horário de almoço). */
public record BarbeiroPublicoResponse(
        Long id,
        String nome,
        LocalTime inicioExpediente,
        LocalTime fimExpediente,
        Set<DayOfWeek> diasTrabalho) {

    public static BarbeiroPublicoResponse de(Barbeiro b) {
        return new BarbeiroPublicoResponse(
                b.getId(), b.getNome(), b.getInicioExpediente(), b.getFimExpediente(), b.getDiasTrabalho());
    }
}
