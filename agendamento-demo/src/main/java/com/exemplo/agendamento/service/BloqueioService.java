package com.exemplo.agendamento.service;

import com.exemplo.agendamento.dto.BloqueioRequest;
import com.exemplo.agendamento.exception.ConflitoHorarioException;
import com.exemplo.agendamento.exception.NegocioException;
import com.exemplo.agendamento.exception.RecursoNaoEncontradoException;
import com.exemplo.agendamento.model.Barbeiro;
import com.exemplo.agendamento.model.Bloqueio;
import com.exemplo.agendamento.repository.AgendamentoRepository;
import com.exemplo.agendamento.repository.BarbeiroRepository;
import com.exemplo.agendamento.repository.BloqueioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BloqueioService {

    private final BloqueioRepository bloqueios;
    private final BarbeiroRepository barbeiros;
    private final AgendamentoRepository agendamentos;

    @Transactional
    public Bloqueio criar(Long barbeiroId, BloqueioRequest req) {
        if (!req.fim().isAfter(req.inicio())) {
            throw new NegocioException("O fim da folga deve ser depois do início");
        }
        Barbeiro barbeiro = barbeiros.buscarComLock(barbeiroId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Barbeiro não encontrado"));

        if (agendamentos.existeConflito(barbeiroId, req.inicio(), req.fim(), -1L)) {
            throw new ConflitoHorarioException(
                    "Há clientes agendados nesse período; cancele ou remarque antes de bloquear");
        }
        Bloqueio b = new Bloqueio();
        b.setBarbeiro(barbeiro);
        b.setInicio(req.inicio());
        b.setFim(req.fim());
        b.setMotivo(req.motivo());
        return bloqueios.save(b);
    }

    @Transactional(readOnly = true)
    public List<Bloqueio> listarFuturos(Long barbeiroId) {
        return bloqueios.findByBarbeiroIdAndFimAfterOrderByInicio(barbeiroId, LocalDateTime.now());
    }

    @Transactional
    public void excluir(Long id) {
        if (!bloqueios.existsById(id)) {
            throw new RecursoNaoEncontradoException("Bloqueio não encontrado");
        }
        bloqueios.deleteById(id);
    }
}
