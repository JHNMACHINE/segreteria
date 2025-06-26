package com.universita.segreteria.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.universita.segreteria.controller.UtenteProxyController;
import com.universita.segreteria.dto.*;
import com.universita.segreteria.model.Aula;
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

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
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
        }catch (ResponseStatusException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            // gestisci le eccezioni note dal service: restituisci 400 con il messaggio originale
            log.warn("Errore di business durante '{}': {}", operazione, ex.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        } catch (Exception ex) {
            log.error("Errore interno durante l’operazione '{}', ruolo {}: {}", operazione, ruolo, ex.getMessage(), ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Errore interno durante l’operazione \"" + operazione + "\"");
        }
    }


    private Object operazioneSegreteria(String operazione, String subject, Object... parametri) {
        ObjectMapper mapper = new ObjectMapper();
        return switch (operazione) {
            case "creaDocente" -> {
                CreaDocenteDTO dto = mapper.convertValue(parametri[0], CreaDocenteDTO.class);
                yield  segreteriaService.creaDocente(dto);
            }
            case "creaStudente" -> {
                CreaStudenteDTO dto = mapper.convertValue(parametri[0], CreaStudenteDTO.class);
                yield  segreteriaService.creaStudente(dto);
            }
            case "getProfilo" -> segreteriaService.getProfilo(subject);
            case "inserisciStudente" -> segreteriaService.inserisciStudente((StudenteDTO) parametri[0]);
            case "confermaVoto" -> {
                if (!(parametri[0] instanceof Number number)) {
                    throw new IllegalArgumentException("ID voto non valido");
                }
                // Esegui davvero la conferma lato service
                segreteriaService.confermaVoto(number.longValue());
                // Puoi restituire un void/null oppure un DTO di conferma
                yield null;
            }
            case "getVotiInAttesa" -> segreteriaService.getVotiInAttesa();
            case "getVotiAccettatiPerStudente" -> {
                String matricola = (String) parametri[0];
                yield segreteriaService.getVotiAccettatiPerStudente(matricola);
            }
            case "cercaStudente" -> segreteriaService.cercaStudente((String) parametri[0], (String) parametri[1]);
            case "cercaStudentePerMatricola" -> segreteriaService.cercaStudentePerMatricola((String) parametri[0]);
            case "cambiaPianoDiStudi" ->
                    segreteriaService.cambiaPianoDiStudi((Integer) parametri[0], (String) parametri[1]);
            case "getAllStudenti" -> segreteriaService.getAllStudenti();
            case "getEsamiDisponibiliPerPiano"->segreteriaService.getEsamiDisponibiliPerPiano((String) parametri[0]);
            default -> throw new RuntimeException("Operazione non consentita per la segreteria.");
        };
    }

    private Object operazioneStudente(String operazione, String subject, Object... parametri) {
        log.info("Operazione STUDENTE richiesta: '{}', parametri: {}", operazione, Arrays.toString(parametri));
        return switch (operazione) {
            case "aggiornaStatoVoto" -> {
                Long votoId = (parametri[0] instanceof Number) ?
                        ((Number) parametri[0]).longValue() :
                        null;

                if (votoId == null) throw new IllegalArgumentException("ID voto non valido");

                studenteService.aggiornaStatoVoto(votoId, (Boolean) parametri[1]);
                yield null;
            }
            case "prenotaEsame" -> studenteService.prenotaEsame(subject, (Integer) parametri[0]);
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
            case "getAppelli" -> docenteService.getAppelli(subject);
            case "getInfoDocente" -> docenteService.getInfoDocente(subject);
            case "inserisciVoto" -> {
                InserimentoVotoDTO dto = mapper.convertValue(parametri[0], InserimentoVotoDTO.class);
                yield docenteService.inserisciVoto(dto.getAppelloId(), dto.getMatricolaStudente(), dto.getVoto());
            }
            case "creaEsame" -> docenteService.creaEsame(subject, (String) parametri[0], (String) parametri[1]);
            case "getAuleDisponibili" -> docenteService.getAuleDisponibili((String) parametri[0]);
            case "eliminaEsame" -> {
                Number num= (Number) parametri[0];
               yield docenteService.eliminaEsame((Long) num.longValue());
            }
            case "studenteAssente" -> {
                // deserializzo correttamente i parametri
                StudenteDTO studenteDTO = mapper.convertValue(parametri[0], StudenteDTO.class);
                EsameDTO    esameDTO    = mapper.convertValue(parametri[1], EsameDTO.class);
                Integer     voto        = (parametri[2] instanceof Number)
                        ? ((Number) parametri[2]).intValue()
                        : null;
                // chiamo infine il service
                yield docenteService.studenteAssente(studenteDTO, esameDTO, voto);
            }
            case "trovaStudentiPerAppello" -> {
                Long appelloId = Long.parseLong(parametri[0].toString());
                yield docenteService.trovaStudentiPerAppello(appelloId);
            }
            default -> throw new RuntimeException("Operazione non consentita per il docente.");
        };
    }
}
