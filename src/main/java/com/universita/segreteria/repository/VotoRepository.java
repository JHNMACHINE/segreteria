package com.universita.segreteria.repository;

import com.universita.segreteria.model.Esame;
import com.universita.segreteria.model.StatoVoto;
import com.universita.segreteria.model.Studente;
import com.universita.segreteria.model.Voto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VotoRepository extends JpaRepository<Voto, Long> {
    List<Voto> findByStudenteId(Long studenteId);

    List<Voto> findByEsame(Esame esame);

    boolean existsByStudenteAndEsame(Studente studente, Esame esame);
    List<Voto> findByStato(StatoVoto stato);

    @Query("SELECT v FROM Voto v " +
            "JOIN FETCH v.studente " +
            "JOIN FETCH v.esame " +
            "WHERE v.stato = :stato")
    List<Voto> findByStatoWithDetails(@Param("stato") StatoVoto stato);

}