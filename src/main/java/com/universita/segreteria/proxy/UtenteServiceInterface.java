package com.universita.segreteria.proxy;

import com.universita.segreteria.model.Esame;
import com.universita.segreteria.model.PianoDiStudi;
import com.universita.segreteria.model.Studente;
import com.universita.segreteria.model.Voto;

import java.util.List;

public interface UtenteServiceInterface {
    Studente visualizzaStudente(Long id);
    Studente cambiaPianoStudi(Long idStudente, PianoDiStudi nuovoPiano);
    List<Esame> visualizzaPrenotazioniEsame(Long esameId);
    Voto inserisciVoto(Long esameId, Long studenteId, int voto);
}
