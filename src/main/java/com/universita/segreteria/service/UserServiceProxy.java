package com.universita.segreteria.service;

import com.universita.segreteria.dto.EsameDTO;
import com.universita.segreteria.dto.StudenteDTO;
import com.universita.segreteria.model.Esame;
import com.universita.segreteria.model.PianoDiStudi;
import com.universita.segreteria.model.Studente;
import com.universita.segreteria.model.TipoUtente;
import com.universita.segreteria.proxy.UtenteService;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@NoArgsConstructor
public class UserServiceProxy implements UtenteService {
    @Autowired
    private SegreteriaService segreteriaService;
    @Autowired
    private StudenteService studenteService;
    @Autowired
    private DocenteService docenteService;
    @Setter
    private TipoUtente ruolo;

    @Override
    public Object eseguiOperazione(String operazione, Object... parametri) {
        return switch (ruolo) {
            case SEGRETARIO -> operazioneSegreteria(operazione, parametri);
            case STUDENTE -> operazioneStudente(operazione, parametri);
            case DOCENTE -> operazioneDocente(operazione, parametri);
        };
    }

    private Object operazioneSegreteria(String operazione, Object... parametri) {
        return switch (operazione) {
            case "inserisciStudente" -> segreteriaService.inserisciStudente((StudenteDTO) parametri[0]);
            case "confermaVoto" -> segreteriaService.confermaVoto((StudenteDTO) parametri[0], (Long) parametri[1]);
            case "cercaStudente" -> segreteriaService.cercaStudente((String) parametri[0], (String) parametri[1]);
            case "cercaStudentePerMatricola" -> segreteriaService.cercaStudentePerMatricola((String) parametri[0]);
            case "cambiaPianoDiStudi" ->
                    segreteriaService.cambiaPianoDiStudi((Long) parametri[0], (PianoDiStudi) parametri[1]);
            default -> throw new UnsupportedOperationException("Operazione non consentita per la segreteria.");
        };
    }

    private Object operazioneStudente(String operazione, Object... parametri) {
        return switch (operazione) {
            case "aggiornaStatoVoto" -> studenteService.aggiornaStatoVoto((Long) parametri[0], (boolean) parametri[1]);
            case "consultaPianoStudi" -> studenteService.consultaPianoStudi((Long) parametri[0]);
            case "prenotaEsame" -> studenteService.prenotaEsame((Long) parametri[0], (Long) parametri[1]);
            case "esamiSuperati" -> studenteService.esamiSuperati((StudenteDTO) parametri[0]);
            case "getEsamiDaSostenere" -> studenteService.getEsamiDaSostenere((StudenteDTO) parametri[0]);
            case "esamiPrenotabili" -> studenteService.esamiPrenotabili((StudenteDTO) parametri[0]);
            default -> throw new UnsupportedOperationException("Operazione non consentita per lo studente.");
        };
    }

    private Object operazioneDocente(String operazione, Object... parametri) {
        return switch (operazione) {
            case "inserisciVoto" ->
                    docenteService.inserisciVoto((StudenteDTO) parametri[0], (EsameDTO) parametri[1], (int) parametri[2]);
            case "creaEsame" -> docenteService.creaEsame((Long) parametri[0], (EsameDTO) parametri[1]);
            case "visualizzaPrenotazioniEsame" -> docenteService.visualizzaPrenotazioniEsame((Long) parametri[0]);
            case "eliminaEsame" -> docenteService.eliminaEsame((Long) parametri[0]);
            case "aggiornaEsame" -> docenteService.aggiornaEsame((Long) parametri[0], (EsameDTO) parametri[1]);
            case "getEsamiByDocente" -> docenteService.getEsamiByDocente((Long) parametri[0]);
            case "getEsameById" -> docenteService.getEsameById((Long) parametri[0]);
            case "modificaVoto" -> docenteService.modificaVoto((Long) parametri[0], (Integer) parametri[1]);
            case "getVotiPerEsame" -> docenteService.getVotiPerEsame((Long) parametri[0]);
            case "eliminaVoto" -> docenteService.eliminaVoto((Long) parametri[0]);
            default -> throw new UnsupportedOperationException("Operazione non consentita per il docente.");
        };
    }
}
