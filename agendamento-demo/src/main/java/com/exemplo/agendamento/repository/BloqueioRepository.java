package com.exemplo.agendamento.repository;

import com.exemplo.agendamento.model.Bloqueio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BloqueioRepository extends JpaRepository<Bloqueio, Long> {

    @Query("""
            select count(b) > 0 from Bloqueio b
            where b.barbeiro.id = :barbeiroId
              and b.inicio < :fim
              and b.fim > :inicio
            """)
    boolean existeConflito(@Param("barbeiroId") Long barbeiroId,
                           @Param("inicio") LocalDateTime inicio,
                           @Param("fim") LocalDateTime fim);

    List<Bloqueio> findByBarbeiroIdAndFimAfterOrderByInicio(Long barbeiroId, LocalDateTime apartirDe);
}
