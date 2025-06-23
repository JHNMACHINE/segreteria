package com.universita.segreteria.repository;

import com.universita.segreteria.model.PianoDiStudi;
import com.universita.segreteria.model.Studente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudenteRepository extends JpaRepository<Studente, Long> {
    Optional<Studente> findByMatricola(String matricola);
    List<Studente> findByNomeAndCognome(String nome, String cognome);

    Optional<Studente> findByEmail(String email);
    List<Studente> findByEsamiNome(String nomeEsame);

    boolean existsByEmail(String email);
}
