package com.universita.segreteria.repository;

import com.universita.segreteria.model.Docente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocenteRepository extends JpaRepository<Docente, Long> {
    Optional<Docente> findByMatricola(String matricola);

    List<Docente> findByNomeAndCognome(String nome, String cognome);

    Optional<Docente> findByEmail(String email);

    boolean existsByEmail(String email);
}