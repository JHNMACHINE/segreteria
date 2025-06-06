package com.universita.segreteria.observer;

import com.universita.segreteria.model.StatoEsame;
import com.universita.segreteria.model.Studente;
import com.universita.segreteria.model.Voto;

public class StudenteObserver implements Observer {

    private final Studente studente;

    public StudenteObserver(Studente studente) {
        this.studente = studente;
    }

    @Override
    public void update(Voto voto) {
        if (voto.getStudente().getId().equals(studente.getId()) && voto.getEsame().getStatoEsame() != StatoEsame.ASSENTE) {
            System.out.println("🔔 Notifica per " + studente.getNome() +
                    ": è stato inserito il voto " + voto.getVoto() +
                    " per l’esame " + voto.getEsame().getNome());
        }
        else
            System.out.print("Lo studente risulta assente, nella data di esame per cui si è prenotato");
    }
}