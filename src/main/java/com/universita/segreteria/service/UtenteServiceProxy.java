package com.universita.segreteria.service;

import com.universita.segreteria.model.Esame;
import com.universita.segreteria.model.Studente;
import com.universita.segreteria.model.TipoUtente;
import com.universita.segreteria.proxy.UtenteService;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@NoArgsConstructor
public class UtenteServiceProxy implements UtenteService {
    private  SegreteriaService segreteriaService;
    private  StudenteService studenteService;
    private  DocenteService docenteService;
    private  TipoUtente ruolo;

    @Override
    public Object eseguiOperazione(String operazione, Object... parametri) {
        return switch (ruolo) {
            case SEGRETARIO -> operazioneSegreteria(operazione, parametri);
            case STUDENTE -> operazioneStudente(operazione, parametri);
            case DOCENTE -> operazioneDocente(operazione, parametri);
            default -> throw new SecurityException("Ruolo non riconosciuto.");
        };
    }

    private Object operazioneSegreteria(String operazione, Object... parametri) {
        return switch (operazione) {
            case "inserisciStudente" -> segreteriaService.inserisciStudente((Studente) parametri[0]);
            case "confermaVoto" -> segreteriaService.confermaVoto((Long) parametri[0]);
            default -> throw new UnsupportedOperationException("Operazione non consentita per la segreteria.");
        };
    }

    private Object operazioneStudente(String operazione, Object... parametri) {
        return switch (operazione) {
            case "accettaVoto" -> studenteService.aggiornaStatoVoto((Long) parametri[0], (boolean) parametri[1]);
            case "visualizzaPianoStudi" -> studenteService.consultaPianoStudi((Long) parametri[0]);
            default -> throw new UnsupportedOperationException("Operazione non consentita per lo studente.");
        };
    }

    private Object operazioneDocente(String operazione, Object... parametri) {
        return switch (operazione) {
            case "inserisciVoto" ->
                    docenteService.inserisciVoto((Long) parametri[0], (Long) parametri[1], (int) parametri[2], (boolean) parametri[3]);
            case "creaEsame" -> docenteService.creaEsame((Long) parametri[0], (Esame) parametri[1]);
            default -> throw new UnsupportedOperationException("Operazione non consentita per il docente.");
        };
    }
}
