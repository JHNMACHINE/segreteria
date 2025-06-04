package com.universita.segreteria.repository;


import com.universita.segreteria.model.Segretario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SegretarioRepository extends JpaRepository<Segretario, Long> {
    Optional<Segretario> findByMatricola(String matricola);

    List<Segretario> findByNomeAndCognome(String nome, String cognome);
}
