package com.exemplo.agendamento.repository;

import com.exemplo.agendamento.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    /** Primeiro cliente com esse telefone (só dígitos). Seguro mesmo se houver duplicados. */
    Optional<Cliente> findFirstByTelefoneOrderById(String telefone);
}
