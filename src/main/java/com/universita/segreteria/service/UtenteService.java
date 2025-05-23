package com.universita.segreteria.service;

import com.universita.segreteria.factory.UtenteFactory;
import com.universita.segreteria.model.*;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@NoArgsConstructor
public class UtenteService {

    public Utente creaUtente(TipoUtente tipo, String nome, String cognome, String matricola) {
        Utente utente = UtenteFactory.creaUtente(tipo);
        utente.setNome(nome);
        utente.setCognome(cognome);
        utente.setMatricola(matricola);

        return utente;
    }
}
