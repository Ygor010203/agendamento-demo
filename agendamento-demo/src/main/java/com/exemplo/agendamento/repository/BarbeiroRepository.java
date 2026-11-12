package com.exemplo.agendamento.repository;

import com.exemplo.agendamento.model.Barbeiro;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BarbeiroRepository extends JpaRepository<Barbeiro, Long> {

    /**
     * Bloqueia a linha do barbeiro até o fim da transação.
     * Evita que dois pedidos simultâneos agendem o mesmo horário.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Barbeiro p where p.id = :id")
    Optional<Barbeiro> buscarComLock(@Param("id") Long id);
}
