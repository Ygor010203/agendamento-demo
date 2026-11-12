package com.exemplo.agendamento.controller;

import com.exemplo.agendamento.exception.RecursoNaoEncontradoException;
import com.exemplo.agendamento.model.Barbeiro;
import com.exemplo.agendamento.repository.BarbeiroRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/barbeiros")
@RequiredArgsConstructor
public class BarbeiroController {

    private final BarbeiroRepository repository;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Barbeiro criar(@RequestBody @Valid Barbeiro barbeiro) {
        barbeiro.setId(null);
        return repository.save(barbeiro);
    }

    @GetMapping
    public List<Barbeiro> listar() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public Barbeiro buscar(@PathVariable Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Barbeiro não encontrado(a)"));
    }

    @PutMapping("/{id}")
    public Barbeiro atualizar(@PathVariable Long id, @RequestBody @Valid Barbeiro barbeiro) {
        if (!repository.existsById(id)) {
            throw new RecursoNaoEncontradoException("Barbeiro não encontrado(a)");
        }
        barbeiro.setId(id);
        return repository.save(barbeiro);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        repository.deleteById(id);
    }
}
