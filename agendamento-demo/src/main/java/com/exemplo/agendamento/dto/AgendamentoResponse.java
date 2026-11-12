package com.exemplo.agendamento.dto;

import com.exemplo.agendamento.model.Agendamento;
import com.exemplo.agendamento.model.StatusAgendamento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AgendamentoResponse(
        Long id,
        Long clienteId, String cliente, String clienteTelefone,
        Long barbeiroId, String barbeiro,
        Long servicoId, String servico,
        LocalDateTime inicio, LocalDateTime fim,
        BigDecimal valor,
        StatusAgendamento status) {

    public static AgendamentoResponse de(Agendamento a) {
        return new AgendamentoResponse(
                a.getId(),
                a.getCliente().getId(), a.getCliente().getNome(), a.getCliente().getTelefone(),
                a.getBarbeiro().getId(), a.getBarbeiro().getNome(),
                a.getServico().getId(), a.getServico().getNome(),
                a.getInicio(), a.getFim(),
                a.getValor(), a.getStatus());
    }
}
