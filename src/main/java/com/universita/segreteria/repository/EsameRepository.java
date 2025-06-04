package com.universita.segreteria.repository;

import com.universita.segreteria.model.Docente;
import com.universita.segreteria.model.Esame;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EsameRepository extends JpaRepository<Esame, Long> {
    List<Esame> findByNome(String nome);

    List<Esame> findByDocenteId(Long docenteId);

    List<Esame> findByDocente(Docente docente);
}
