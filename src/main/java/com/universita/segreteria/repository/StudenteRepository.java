package com.universita.segreteria.repository;

import com.universita.segreteria.model.Studente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudenteRepository extends JpaRepository<Studente, Long> {
    Optional<Studente> findByMatricola(String matricola);
    List<Studente> findByNomeAndCognome(String nome, String cognome);
}
