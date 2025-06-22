package com.universita.segreteria.repository;

import com.universita.segreteria.model.Docente;
import com.universita.segreteria.model.Esame;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EsameRepository extends JpaRepository<Esame, Long> {
    Optional<Esame> findFirstByNome(String nome);

    List<Esame> findByDocente(Docente docente);

    List<Esame> findByNomeIn(List<String> list);
}
