package com.universita.segreteria.factory;

import com.universita.segreteria.model.*;

public class UtenteFactory {
    public static Utente creaUtente(TipoUtente tipo) {
        return switch (tipo) {
            case STUDENTE -> new Studente();
            case DOCENTE -> new Docente();
            case SEGRETARIO -> new Segretario();
        };
    }
}
