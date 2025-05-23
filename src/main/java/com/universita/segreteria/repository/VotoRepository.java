package com.universita.segreteria.repository;

import com.universita.segreteria.model.Voto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VotoRepository extends JpaRepository<Voto, Long> {
    List<Voto> findByStudenteId(Long studenteId);
}