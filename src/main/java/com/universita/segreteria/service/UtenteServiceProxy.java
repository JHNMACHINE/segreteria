package com.universita.segreteria.service;

import com.universita.segreteria.model.Esame;
import com.universita.segreteria.model.PianoDiStudi;
import com.universita.segreteria.model.Studente;
import com.universita.segreteria.model.TipoUtente;
import com.universita.segreteria.proxy.UtenteService;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;

@Service
@NoArgsConstructor
public class UtenteServiceProxy implements UtenteService {
    private  SegreteriaService segreteriaService;
    private  StudenteService studenteService;
    private  DocenteService docenteService;
    @Setter
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
            case "cercaStudente" -> segreteriaService.cercaStudente((String) parametri[0], (String) parametri[1]);
            case "cercaStudentePerMatricola" -> segreteriaService.cercaStudentePerMatricola((String) parametri[0]);
            case "cambiaPianoDiStudi" -> segreteriaService.cambiaPianoDiStudi((Long) parametri[0], (PianoDiStudi) parametri[1]);
            default -> throw new UnsupportedOperationException("Operazione non consentita per la segreteria.");
        };
    }

    private Object operazioneStudente(String operazione, Object... parametri) {
        return switch (operazione) {
            case "aggiornaStatoVoto" -> studenteService.aggiornaStatoVoto((Long) parametri[0], (boolean) parametri[1]);
            case "consultaPianoStudi" -> studenteService.consultaPianoStudi((Long) parametri[0]);
            case "prenotaEsame" -> studenteService.prenotaEsame((Long) parametri[0], (Long) parametri[1]);
            default -> throw new UnsupportedOperationException("Operazione non consentita per lo studente.");
        };
    }

    private Object operazioneDocente(String operazione, Object... parametri) {
        return switch (operazione) {
            case "inserisciVoto" ->
                    docenteService.inserisciVoto((Long) parametri[0], (Long) parametri[1], (int) parametri[2], (boolean) parametri[3]);
            case "creaEsame" -> docenteService.creaEsame((Long) parametri[0], (Esame) parametri[1]);
            case "visualizzaPrenotazioniEsame" -> docenteService.visualizzaPrenotazioniEsame((Long) parametri[0]);
            case "eliminaEsame" -> docenteService.eliminaEsame((Long) parametri[0]);
            case "aggiornaEsame" -> docenteService.aggiornaEsame((Long) parametri[0], (Esame) parametri[1]);
            case "getEsamiByDocente" -> docenteService.getEsamiByDocente((Long) parametri[0]);
            case "inserisciAppello" -> docenteService.inserisciAppello((Esame) parametri[0]);
            case "getEsameById" -> docenteService.getEsameById((Long) parametri[0]);

            default -> throw new UnsupportedOperationException("Operazione non consentita per il docente.");
        };
    }
}
