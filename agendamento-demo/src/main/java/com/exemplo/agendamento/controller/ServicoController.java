package com.exemplo.agendamento.controller;

import com.exemplo.agendamento.exception.RecursoNaoEncontradoException;
import com.exemplo.agendamento.model.Servico;
import com.exemplo.agendamento.repository.ServicoRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/servicos")
@RequiredArgsConstructor
public class ServicoController {

    private final ServicoRepository repository;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Servico criar(@RequestBody @Valid Servico servico) {
        servico.setId(null);
        return repository.save(servico);
    }

    @GetMapping
    public List<Servico> listar() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public Servico buscar(@PathVariable Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Servico não encontrado(a)"));
    }

    @PutMapping("/{id}")
    public Servico atualizar(@PathVariable Long id, @RequestBody @Valid Servico servico) {
        if (!repository.existsById(id)) {
            throw new RecursoNaoEncontradoException("Servico não encontrado(a)");
        }
        servico.setId(id);
        return repository.save(servico);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        repository.deleteById(id);
    }
}
