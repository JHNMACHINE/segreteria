package com.universita.segreteria.repository;


import com.universita.segreteria.model.Segretario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SegretarioRepository extends JpaRepository<Segretario, Long> {
    Optional<Segretario> findByMatricola(String matricola);

    List<Segretario> findByNomeAndCognome(String nome, String cognome);

    Optional<Segretario> findByEmail(String email);

    @Query("SELECT s.matricola FROM Segretario s WHERE s.matricola LIKE ?1%")
    List<String> findAllMatricoleByPrefix(String matricolaPrefix);
}
