package com.universita.segreteria.observer;

import com.universita.segreteria.model.Studente;
import com.universita.segreteria.model.Voto;

public class StudenteObserver implements Observer {

    private final Studente studente;

    public StudenteObserver(Studente studente) {
        this.studente = studente;
    }

    @Override
    public void update(Voto voto) {
        if (voto.getStudente().getId().equals(studente.getId())) {
            System.out.println("🔔 Notifica per " + studente.getNome() +
                    ": è stato inserito il voto " + voto.getVoto() +
                    " per l’esame " + voto.getEsame().getNome());
        }
    }
}
