package com.universita.segreteria.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.universita.segreteria.controller.UtenteProxyController;
import com.universita.segreteria.dto.*;
import com.universita.segreteria.model.PianoDiStudi;
import com.universita.segreteria.model.TipoUtente;
import com.universita.segreteria.proxy.UtenteService;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;

@Service
@NoArgsConstructor
public class UserServiceProxy implements UtenteService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceProxy.class);

    @Autowired
    private SegreteriaService segreteriaService;
    @Autowired
    private StudenteService studenteService;
    @Autowired
    private DocenteService docenteService;
    @Setter
    private TipoUtente ruolo;

    @Override
    public Object eseguiOperazione(String operazione, String subject, Object... parametri) {
        log.info("Eseguo operazione '{}' con parametri {} per ruolo {}", operazione, Arrays.toString(parametri), ruolo);
        log.info("Subject: {}", subject);
        try {
            return switch (ruolo) {
                case SEGRETARIO -> operazioneSegreteria(operazione, subject, parametri);
                case STUDENTE -> operazioneStudente(operazione, subject, parametri);
                case DOCENTE -> operazioneDocente(operazione, subject, parametri);
            };
        } catch (ResponseStatusException ex) {
            // se è già un 404, 403, etc, rilanciamola così com’è:
            throw ex;
        } catch (Exception ex) {
            // logghiamo il problema imprevisto e restituiamo un 500
            log.error("Errore interno durante l’operazione '{}', ruolo {}: {}", operazione, ruolo, ex.getMessage(), ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Errore interno durante l’operazione \"" + operazione + "\"");
        }
    }


    private Object operazioneSegreteria(String operazione, String subject, Object... parametri) {
        ObjectMapper mapper = new ObjectMapper();
        return switch (operazione) {
            case "creaDocente" -> {
                CreaDocenteDTO dto = mapper.convertValue(parametri[0], CreaDocenteDTO.class);
                yield  segreteriaService.creaDocente(dto);
            }
            case "getProfilo" -> segreteriaService.getProfilo(subject);
            case "inserisciStudente" -> segreteriaService.inserisciStudente((StudenteDTO) parametri[0]);
            case "confermaVoto" -> segreteriaService.confermaVoto((StudenteDTO) parametri[0], (Long) parametri[1]);
            case "cercaStudente" -> segreteriaService.cercaStudente((String) parametri[0], (String) parametri[1]);
            case "cercaStudentePerMatricola" -> segreteriaService.cercaStudentePerMatricola((String) parametri[0]);
            case "cambiaPianoDiStudi" ->
                    segreteriaService.cambiaPianoDiStudi((Integer) parametri[0], (String) parametri[1]);
            case "getAllStudenti" -> segreteriaService.getAllStudenti();
            case "getAllDocenti" -> segreteriaService.getAllDocenti();
            default -> throw new RuntimeException("Operazione non consentita per la segreteria.");
        };
    }

    private Object operazioneStudente(String operazione, String subject, Object... parametri) {
        log.info("Operazione STUDENTE richiesta: '{}', parametri: {}", operazione, Arrays.toString(parametri));
        return switch (operazione) {
            case "aggiornaStatoVoto" -> {
                Number numero = (Number) parametri[0];
                yield studenteService.aggiornaStatoVoto(numero.longValue(), (boolean) parametri[1]);
            }
            case "prenotaEsame" -> studenteService.prenotaEsame(subject, (Integer) parametri[0]);
            case "esamiSuperati" -> studenteService.esamiSuperati((StudenteDTO) parametri[0]);
            case "getEsamiDaSostenere" -> studenteService.getEsamiDaSostenere((StudenteDTO) parametri[0]);
            case "getCarriera" -> studenteService.getCarriera(subject );
            case "esamiPrenotabili" -> studenteService.esamiPrenotabili(subject);
            case "getInfoStudente" -> studenteService.getInfoStudente(subject);
            case "getPianoDiStudi" -> studenteService.consultaPianoStudi(subject);
            case "getVotiDaAccettare" -> studenteService.getVotiDaAccettare(subject);
            case "pagaTassa" -> {
                String nomeTassa = (String) parametri[0];
                studenteService.pagaTassa(subject, nomeTassa);
                yield null;
            }
            default -> {
                log.error("Operazione '{}' non consentita per ruolo STUDENTE", operazione);
                throw new RuntimeException("Operazione non consentita per lo studente.");
            }
        };
    }

    private Object operazioneDocente(String operazione, String subject, Object... parametri) {
        ObjectMapper mapper = new ObjectMapper();
        return switch (operazione) {
            case "trovaStudentiPerEsame" -> docenteService.trovaStudentiPerEsame((String) parametri[0]);
            case "getAppelli" -> docenteService.getAppelli(subject);
            case "getInfoDocente" -> docenteService.getInfoDocente(subject);
            case "inserisciVoto" -> {
                InserimentoVotoDTO dto = mapper.convertValue(parametri[0], InserimentoVotoDTO.class);
                yield docenteService.inserisciVoto(dto.getAppelloId(), dto.getMatricolaStudente(), dto.getVoto());
            }
            case "creaEsame" -> docenteService.creaEsame((Long) parametri[0], (EsameDTO) parametri[1]);
            case "visualizzaPrenotazioniEsame" -> docenteService.visualizzaPrenotazioniEsame((Long) parametri[0]);
            case "eliminaEsame" -> docenteService.eliminaEsame((Long) parametri[0]);
            case "aggiornaEsame" -> docenteService.aggiornaEsame((Long) parametri[0], (EsameDTO) parametri[1]);
            case "getEsamiByDocente" -> docenteService.getEsamiByDocente((Long) parametri[0]);
            case "getEsameById" -> docenteService.getEsameById((Long) parametri[0]);
            case "modificaVoto" -> docenteService.modificaVoto((Long) parametri[0], (Integer) parametri[1]);
            case "getVotiPerEsame" -> docenteService.getVotiPerEsame((Long) parametri[0]);
            case "eliminaVoto" -> docenteService.eliminaVoto((Long) parametri[0]);
            case "studenteAssente" ->
                    docenteService.studenteAssente((StudenteDTO) parametri[0], (EsameDTO) parametri[1], (Integer) parametri[2]);
            default -> throw new RuntimeException("Operazione non consentita per il docente.");
        };
    }
}
