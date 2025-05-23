package com.universita.segreteria.repository;

import com.universita.segreteria.model.Tassa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TassaRepository extends JpaRepository<Tassa, Long> {
    List<Tassa> findByStudenteId(Long studenteId);
}
