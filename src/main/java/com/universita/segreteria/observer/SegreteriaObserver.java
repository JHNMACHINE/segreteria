package com.universita.segreteria.observer;

import com.universita.segreteria.model.Voto;

public class SegreteriaObserver implements Observer{
    @Override
    public void update(Voto voto) {
        String azione = switch (voto.getStato()) {
            case ACCETTATO -> "accettato";
            case RIFIUTATO -> "rifiutato";
            default -> "in attesa";
        };

        System.out.println("📋 Segreteria notificata: " +
                "lo studente " + voto.getStudente().getNome() +
                " ha " + azione + " il voto " + voto.getVoto() +
                " per l'esame " + voto.getEsame().getNome());
    }
}
