package com.universita.segreteria.repository;

import com.universita.segreteria.model.Esame;
import com.universita.segreteria.model.Prenotazione;
import com.universita.segreteria.model.Studente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrenotazioneRepository extends JpaRepository<Prenotazione, Long> {
    boolean existsByStudenteAndEsame(Studente studente, Esame esame);

    List<Prenotazione> findByEsame(Esame esame);
}
