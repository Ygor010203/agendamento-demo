package com.exemplo.agendamento.controller;

import com.exemplo.agendamento.dto.AgendamentoPublicoRequest;
import com.exemplo.agendamento.dto.AgendamentoResponse;
import com.exemplo.agendamento.dto.RemarcarRequest;
import com.exemplo.agendamento.service.AgendamentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AgendamentoController {

    private final AgendamentoService service;

    /** Aberto: usado pela página de agendamento do cliente. */
    @PostMapping("/agendamentos/publico")
    @ResponseStatus(HttpStatus.CREATED)
    public AgendamentoResponse agendarPublico(@RequestBody @Valid AgendamentoPublicoRequest req) {
        return AgendamentoResponse.de(service.agendarPublico(req));
    }

    @GetMapping("/agendamentos/{id}")
    public AgendamentoResponse buscar(@PathVariable Long id) {
        return AgendamentoResponse.de(service.buscar(id));
    }

    @PatchMapping("/agendamentos/{id}/remarcar")
    public AgendamentoResponse remarcar(@PathVariable Long id, @RequestBody @Valid RemarcarRequest req) {
        return AgendamentoResponse.de(service.remarcar(id, req.novoInicio()));
    }

    @PatchMapping("/agendamentos/{id}/cancelar")
    public AgendamentoResponse cancelar(@PathVariable Long id) {
        return AgendamentoResponse.de(service.cancelar(id));
    }

    @GetMapping("/barbeiros/{id}/agenda")
    public List<AgendamentoResponse> agenda(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return service.agendaDoDia(id, data).stream().map(AgendamentoResponse::de).toList();
    }

    /** Aberto: horários livres de um barbeiro num dia, para um serviço. */
    @GetMapping("/barbeiros/{id}/disponibilidade")
    public List<LocalDateTime> disponibilidade(
            @PathVariable Long id,
            @RequestParam Long servicoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return service.horariosDisponiveis(id, servicoId, data);
    }

    @GetMapping("/clientes/{id}/agendamentos")
    public List<AgendamentoResponse> historico(@PathVariable Long id) {
        return service.historicoDoCliente(id).stream().map(AgendamentoResponse::de).toList();
    }
}
