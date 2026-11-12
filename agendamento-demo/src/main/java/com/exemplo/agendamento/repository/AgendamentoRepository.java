package com.exemplo.agendamento.repository;

import com.exemplo.agendamento.model.Agendamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

    /** Há outro agendamento ATIVO do barbeiro que se sobrepõe ao intervalo? */
    @Query("""
            select count(a) > 0 from Agendamento a
            where a.barbeiro.id = :barbeiroId
              and a.status = com.exemplo.agendamento.model.StatusAgendamento.AGENDADO
              and a.id <> :ignorarId
              and a.inicio < :fim
              and a.fim > :inicio
            """)
    boolean existeConflito(@Param("barbeiroId") Long barbeiroId,
                           @Param("inicio") LocalDateTime inicio,
                           @Param("fim") LocalDateTime fim,
                           @Param("ignorarId") Long ignorarId);

    List<Agendamento> findByBarbeiroIdAndInicioBetweenOrderByInicio(
            Long barbeiroId, LocalDateTime de, LocalDateTime ate);

    List<Agendamento> findByClienteIdOrderByInicioDesc(Long clienteId);
}
