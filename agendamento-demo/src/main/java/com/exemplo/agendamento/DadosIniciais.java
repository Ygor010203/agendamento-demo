package com.exemplo.agendamento;

import com.exemplo.agendamento.model.Barbeiro;
import com.exemplo.agendamento.model.Bloqueio;
import com.exemplo.agendamento.model.Servico;
import com.exemplo.agendamento.repository.BarbeiroRepository;
import com.exemplo.agendamento.repository.BloqueioRepository;
import com.exemplo.agendamento.repository.ServicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.Set;

/** Cadastra dados fictícios na primeira execução, para a demonstração funcionar sem configuração. */
@Component
@RequiredArgsConstructor
public class DadosIniciais implements CommandLineRunner {

    private final ServicoRepository servicos;
    private final BarbeiroRepository barbeiros;
    private final BloqueioRepository bloqueios;

    @Override
    public void run(String... args) {
        if (servicos.count() == 0) {
            servico("Corte", 30, "40.00");
            servico("Barba", 30, "30.00");
            servico("Corte + Barba", 60, "65.00");
            servico("Sobrancelha", 15, "15.00");
            servico("Pigmentação", 45, "50.00");
        }
        if (barbeiros.count() == 0) {
            barbeiro("Carlos", "09:00", "19:00", "12:00", "13:00", EnumSet.of(
                    DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                    DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY));
            Barbeiro rafael = barbeiro("Rafael", "10:00", "20:00", "13:00", "14:00", EnumSet.of(
                    DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY,
                    DayOfWeek.FRIDAY, DayOfWeek.SATURDAY));

            // Folga de exemplo: o Rafael não atende amanhã das 15h às 17h.
            LocalDate amanha = LocalDate.now().plusDays(1);
            Bloqueio folga = new Bloqueio();
            folga.setBarbeiro(rafael);
            folga.setInicio(amanha.atTime(15, 0));
            folga.setFim(amanha.atTime(17, 0));
            folga.setMotivo("Consulta médica");
            bloqueios.save(folga);
        }
    }

    private void servico(String nome, int duracaoMinutos, String preco) {
        Servico s = new Servico();
        s.setNome(nome);
        s.setDuracaoMinutos(duracaoMinutos);
        s.setPreco(new BigDecimal(preco));
        servicos.save(s);
    }

    private Barbeiro barbeiro(String nome, String inicio, String fim, String almocoInicio, String almocoFim,
                              Set<DayOfWeek> dias) {
        Barbeiro b = new Barbeiro();
        b.setNome(nome);
        b.setInicioExpediente(LocalTime.parse(inicio));
        b.setFimExpediente(LocalTime.parse(fim));
        b.setInicioAlmoco(LocalTime.parse(almocoInicio));
        b.setFimAlmoco(LocalTime.parse(almocoFim));
        b.setDiasTrabalho(dias);
        return barbeiros.save(b);
    }
}
