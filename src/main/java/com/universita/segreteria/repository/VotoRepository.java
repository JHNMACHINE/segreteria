package com.universita.segreteria.repository;

import com.universita.segreteria.model.Voto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VotoRepository extends JpaRepository<Voto, Long> {
    List<Voto> findByStudenteId(Long studenteId);
}