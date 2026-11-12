package com.exemplo.agendamento.service;

import com.exemplo.agendamento.dto.AgendamentoPublicoRequest;
import com.exemplo.agendamento.exception.ConflitoHorarioException;
import com.exemplo.agendamento.exception.NegocioException;
import com.exemplo.agendamento.exception.RecursoNaoEncontradoException;
import com.exemplo.agendamento.model.*;
import com.exemplo.agendamento.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AgendamentoService {

    /** Intervalo entre os horários oferecidos na consulta de disponibilidade. */
    private static final int PASSO_MINUTOS = 30;
    private static final Long SEM_IGNORAR = -1L;

    private final AgendamentoRepository agendamentos;
    private final BloqueioRepository bloqueios;
    private final ClienteRepository clientes;
    private final BarbeiroRepository barbeiros;
    private final ServicoRepository servicos;

    /** Agendamento feito pelo cliente: localiza o cadastro pelo telefone ou cria um novo. */
    @Transactional
    public Agendamento agendarPublico(AgendamentoPublicoRequest req) {
        Cliente cliente = localizarOuCriarCliente(req.nome(), req.telefone());
        return criar(cliente, req.barbeiroId(), req.servicoId(), req.inicio());
    }

    @Transactional
    public Agendamento remarcar(Long id, LocalDateTime novoInicio) {
        Agendamento a = buscar(id);
        exigirAgendado(a);

        Barbeiro barbeiro = barbeiros.buscarComLock(a.getBarbeiro().getId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Barbeiro não encontrado"));

        LocalDateTime novoFim = novoInicio.plusMinutes(a.getServico().getDuracaoMinutos());
        validar(barbeiro, novoInicio, novoFim, a.getId());

        a.setInicio(novoInicio);
        a.setFim(novoFim);
        return a;
    }

    @Transactional
    public Agendamento cancelar(Long id) {
        Agendamento a = buscar(id);
        exigirAgendado(a);
        a.setStatus(StatusAgendamento.CANCELADO);
        return a;
    }

    @Transactional(readOnly = true)
    public Agendamento buscar(Long id) {
        return agendamentos.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Agendamento não encontrado"));
    }

    @Transactional(readOnly = true)
    public List<Agendamento> agendaDoDia(Long barbeiroId, LocalDate data) {
        return agendamentos.findByBarbeiroIdAndInicioBetweenOrderByInicio(
                barbeiroId, data.atStartOfDay(), data.plusDays(1).atStartOfDay());
    }

    @Transactional(readOnly = true)
    public List<Agendamento> historicoDoCliente(Long clienteId) {
        return agendamentos.findByClienteIdOrderByInicioDesc(clienteId);
    }

    /** Lista os horários de início livres de um barbeiro num dia, para um serviço. */
    @Transactional(readOnly = true)
    public List<LocalDateTime> horariosDisponiveis(Long barbeiroId, Long servicoId, LocalDate data) {
        Barbeiro b = barbeiros.findById(barbeiroId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Barbeiro não encontrado"));
        Servico s = servicos.findById(servicoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Serviço não encontrado"));

        List<LocalDateTime> livres = new ArrayList<>();
        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime inicio = data.atTime(b.getInicioExpediente());
        LocalDateTime limite = data.atTime(b.getFimExpediente());

        while (!inicio.plusMinutes(s.getDuracaoMinutos()).isAfter(limite)) {
            LocalDateTime fim = inicio.plusMinutes(s.getDuracaoMinutos());
            if (!inicio.isBefore(agora)
                    && motivoForaDoExpediente(b, inicio, fim) == null
                    && !ocupado(barbeiroId, inicio, fim, SEM_IGNORAR)) {
                livres.add(inicio);
            }
            inicio = inicio.plusMinutes(PASSO_MINUTOS);
        }
        return livres;
    }

    // ---------- criação ----------

    private Agendamento criar(Cliente cliente, Long barbeiroId, Long servicoId, LocalDateTime inicio) {
        Servico servico = servicos.findById(servicoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Serviço não encontrado"));
        // Trava a linha do barbeiro até o fim da transação: dois pedidos simultâneos não reservam o mesmo horário.
        Barbeiro barbeiro = barbeiros.buscarComLock(barbeiroId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Barbeiro não encontrado"));

        LocalDateTime fim = inicio.plusMinutes(servico.getDuracaoMinutos());
        validar(barbeiro, inicio, fim, SEM_IGNORAR);

        Agendamento a = new Agendamento();
        a.setCliente(cliente);
        a.setBarbeiro(barbeiro);
        a.setServico(servico);
        a.setInicio(inicio);
        a.setFim(fim);
        a.setValor(servico.getPreco());
        return agendamentos.save(a);
    }

    /** Acha o cliente pelo celular (só dígitos) ou cria um novo cadastro. */
    private Cliente localizarOuCriarCliente(String nome, String telefone) {
        String digitos = telefone == null ? "" : telefone.replaceAll("\\D", "");
        if (digitos.length() < 10 || digitos.length() > 11) {
            throw new NegocioException("Telefone inválido. Informe o DDD e o número");
        }
        Optional<Cliente> existente = clientes.findFirstByTelefoneOrderById(digitos);
        if (existente.isPresent()) {
            return existente.get();
        }
        Cliente novo = new Cliente();
        novo.setNome(nome.trim());
        novo.setTelefone(digitos);
        return clientes.save(novo);
    }

    // ---------- regras de negócio ----------

    private void validar(Barbeiro b, LocalDateTime inicio, LocalDateTime fim, Long ignorarId) {
        if (inicio.isBefore(LocalDateTime.now())) {
            throw new NegocioException("Não é possível agendar no passado");
        }
        String motivo = motivoForaDoExpediente(b, inicio, fim);
        if (motivo != null) {
            throw new NegocioException(motivo);
        }
        if (ocupado(b.getId(), inicio, fim, ignorarId)) {
            throw new ConflitoHorarioException("O barbeiro já possui agendamento ou folga nesse horário");
        }
    }

    /** Retorna null se o intervalo está dentro do horário de atendimento do barbeiro. */
    private String motivoForaDoExpediente(Barbeiro b, LocalDateTime inicio, LocalDateTime fim) {
        if (!b.getDiasTrabalho().contains(inicio.getDayOfWeek())) {
            return b.getNome() + " não atende nesse dia da semana";
        }
        boolean mesmoDia = inicio.toLocalDate().equals(fim.toLocalDate());
        boolean dentro = !inicio.toLocalTime().isBefore(b.getInicioExpediente())
                && !fim.toLocalTime().isAfter(b.getFimExpediente());
        if (!mesmoDia || !dentro) {
            return "Horário fora do expediente (" + b.getInicioExpediente()
                    + " às " + b.getFimExpediente() + ")";
        }
        if (b.getInicioAlmoco() != null && b.getFimAlmoco() != null
                && inicio.toLocalTime().isBefore(b.getFimAlmoco())
                && fim.toLocalTime().isAfter(b.getInicioAlmoco())) {
            return "Horário coincide com o intervalo de almoço (" + b.getInicioAlmoco()
                    + " às " + b.getFimAlmoco() + ")";
        }
        return null;
    }

    private boolean ocupado(Long barbeiroId, LocalDateTime inicio, LocalDateTime fim, Long ignorarId) {
        return agendamentos.existeConflito(barbeiroId, inicio, fim, ignorarId)
                || bloqueios.existeConflito(barbeiroId, inicio, fim);
    }

    private void exigirAgendado(Agendamento a) {
        if (a.getStatus() != StatusAgendamento.AGENDADO) {
            throw new NegocioException("Só é possível alterar agendamentos com status AGENDADO");
        }
    }
}
