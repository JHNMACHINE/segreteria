package com.universita.segreteria.observer;

import com.universita.segreteria.model.Voto;

public class SegreteriaObserver implements Observer {
    @Override
    public void update(Voto voto) {
        String azione = switch (voto.getStato()) {
            case ATTESA -> "richiesto accettazione per";
            case ACCETTATO -> "accettato";
            case RIFIUTATO -> "rifiutato";
        };

        System.out.println("📋 Segreteria notificata: " +
                "lo studente " + voto.getStudente().getNome() +
                " ha " + azione + " il voto " + voto.getVoto() +
                " per l'esame " + voto.getEsame().getNome());
    }
}
