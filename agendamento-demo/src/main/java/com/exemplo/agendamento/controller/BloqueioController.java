package com.exemplo.agendamento.controller;

import com.exemplo.agendamento.dto.BloqueioRequest;
import com.exemplo.agendamento.dto.BloqueioResponse;
import com.exemplo.agendamento.service.BloqueioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BloqueioController {

    private final BloqueioService service;

    @PostMapping("/barbeiros/{barbeiroId}/bloqueios")
    @ResponseStatus(HttpStatus.CREATED)
    public BloqueioResponse criar(@PathVariable Long barbeiroId, @RequestBody @Valid BloqueioRequest req) {
        return BloqueioResponse.de(service.criar(barbeiroId, req));
    }

    @GetMapping("/barbeiros/{barbeiroId}/bloqueios")
    public List<BloqueioResponse> listar(@PathVariable Long barbeiroId) {
        return service.listarFuturos(barbeiroId).stream().map(BloqueioResponse::de).toList();
    }

    @DeleteMapping("/bloqueios/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}
