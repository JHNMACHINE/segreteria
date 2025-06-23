package com.universita.segreteria.observer;

import com.universita.segreteria.model.StatoEsame;
import com.universita.segreteria.model.StatoVoto;
import com.universita.segreteria.model.Studente;
import com.universita.segreteria.model.Voto;

public class StudenteObserver implements Observer {
    private final Studente studente;

    public StudenteObserver(Studente studente) {
        this.studente = studente;
    }

    @Override
    public void update(Voto voto) {
        // Notifica solo se il voto è per questo studente e stato ACCETTATO
        if (voto.getStudente().getId().equals(studente.getId()) &&
                voto.getStato() == StatoVoto.ACCETTATO) {

            System.out.println("🔔 Notifica per " + studente.getNome() +
                    ": il voto " + voto.getVoto() +
                    " per l’esame " + voto.getEsame().getNome() +
                    " è stato confermato dalla segreteria");
        }
    }
}