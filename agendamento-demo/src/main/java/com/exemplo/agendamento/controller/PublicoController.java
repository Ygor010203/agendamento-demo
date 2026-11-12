package com.exemplo.agendamento.controller;

import com.exemplo.agendamento.dto.BarbeiroPublicoResponse;
import com.exemplo.agendamento.model.Servico;
import com.exemplo.agendamento.repository.BarbeiroRepository;
import com.exemplo.agendamento.repository.ServicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Consultas abertas, usadas pela página de agendamento do cliente. */
@RestController
@RequestMapping("/api/publico")
@RequiredArgsConstructor
public class PublicoController {

    private final ServicoRepository servicos;
    private final BarbeiroRepository barbeiros;

    @GetMapping("/servicos")
    public List<Servico> servicos() {
        return servicos.findAll();
    }

    @GetMapping("/barbeiros")
    public List<BarbeiroPublicoResponse> barbeiros() {
        return barbeiros.findAll().stream().map(BarbeiroPublicoResponse::de).toList();
    }
}
