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
        } catch (ResponseStatusException ex) {
            log.info("Eccezione gestita durante '{}': {} [{}]", operazione, ex.getReason(), ex.getMessage());
            throw ex;
        } catch (RuntimeException ex) {
            // gestisci le eccezioni note dal service: restituisci 400 con il messaggio originale
            log.info("Errore di business durante '{}': {}", operazione, ex.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        } catch (Exception ex) {
            log.error("Errore interno durante l’operazione '{}', ruolo {}: {}", operazione, ruolo, ex.getMessage(), ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Errore interno durante l’operazione \"" + operazione + "\"");
        }
    }

    private Object operazioneSegreteria(String operazione, String subject, Object... parametri) {
        log.info("Operazione SEGRETERIA richiesta: '{}', parametri: {}", operazione, Arrays.toString(parametri));
        ObjectMapper mapper = new ObjectMapper();
        return switch (operazione) {
            case "creaDocente" -> {
                if (parametri.length < 1) throw new IllegalArgumentException("Serve un oggetto CreaDocenteDTO");
                CreaDocenteDTO dto = mapper.convertValue(parametri[0], CreaDocenteDTO.class);
                yield segreteriaService.creaDocente(dto);
            }
            case "creaStudente" -> {
                if (parametri.length < 1) throw new IllegalArgumentException("Serve un oggetto CreaStudenteDTO");
                CreaStudenteDTO dto = mapper.convertValue(parametri[0], CreaStudenteDTO.class);
                yield segreteriaService.creaStudente(dto);
            }
            case "getProfilo" -> segreteriaService.getProfilo(subject);
            case "inserisciStudente" -> {
                if (parametri.length < 1 || !(parametri[0] instanceof StudenteDTO dto)) {
                    throw new IllegalArgumentException("Serve un oggetto StudenteDTO");
                }
                yield segreteriaService.inserisciStudente(dto);
            }
            case "confermaVoto" -> {
                if (parametri.length < 1 || !(parametri[0] instanceof Number id)) {
                    throw new IllegalArgumentException("Serve un ID voto numerico");
                }
                yield segreteriaService.confermaVoto(id.longValue());
            }
            case "getVotiInAttesa" -> segreteriaService.getVotiInAttesa();
            case "cercaStudente" -> {
                if (parametri.length < 2 || !(parametri[0] instanceof String nome) || !(parametri[1] instanceof String cognome)) {
                    throw new IllegalArgumentException("Servono nome e cognome (entrambi stringhe) per cercare lo studente");
                }
                yield segreteriaService.cercaStudente(nome, cognome);
            }
            case "cercaStudentePerMatricola" -> {
                if (parametri.length < 1 || !(parametri[0] instanceof String matricola)) {
                    throw new IllegalArgumentException("Serve la matricola (stringa)");
                }
                yield segreteriaService.cercaStudentePerMatricola(matricola);
            }
            case "cambiaPianoDiStudi" -> {
                if (parametri.length < 2 || !(parametri[0] instanceof Integer studenteId) || !(parametri[1] instanceof String piano)) {
                    throw new IllegalArgumentException("Servono ID studente (intero) e nome piano (stringa)");
                }
                yield segreteriaService.cambiaPianoDiStudi(studenteId, piano);
            }
            case "getAllStudenti" -> segreteriaService.getAllStudenti();
            case "getAllDocenti" -> segreteriaService.getAllDocenti();
            case "getEsamiDisponibiliPerPiano" -> {
                if (parametri.length < 1 || !(parametri[0] instanceof String piano)) {
                    throw new IllegalArgumentException("Serve il nome del piano di studi (stringa)");
                }
                yield segreteriaService.getEsamiDisponibiliPerPiano(piano);
            }
            default -> {
                log.error("Operazione '{}' non consentita per ruolo SEGRETERIA", operazione);
                throw new IllegalArgumentException("Operazione non consentita per la segreteria: " + operazione);
            }
        };
    }

    private Object operazioneStudente(String operazione, String subject, Object... parametri) {
        log.info("Operazione STUDENTE richiesta: '{}', parametri: {}", operazione, Arrays.toString(parametri));
        return switch (operazione) {

            case "aggiornaStatoVoto" -> {
                if (parametri.length < 2) {
                    throw new IllegalArgumentException("Parametri insufficienti per aggiornare lo stato del voto");
                }
                Object idParam = parametri[0];
                Object statoParam = parametri[1];
                if (!(idParam instanceof Number)) {
                    throw new IllegalArgumentException("Il primo parametro deve essere un ID numerico del voto");
                }
                if (!(statoParam instanceof Boolean accettato)) {
                    throw new IllegalArgumentException("Il secondo parametro deve essere un valore booleano (accettato/rifiutato)");
                }
                Long votoId = ((Number) idParam).longValue();
                yield studenteService.aggiornaStatoVoto(votoId, accettato);
            }
            case "prenotaEsame" -> {
                if (parametri.length < 1 || !(parametri[0] instanceof Integer idEsame)) {
                    throw new IllegalArgumentException("Serve un ID esame intero come parametro");
                }
                yield studenteService.prenotaEsame(subject, idEsame);
            }
            case "esamiSuperati" -> {
                if (parametri.length < 1 || !(parametri[0] instanceof StudenteDTO dto)) {
                    throw new IllegalArgumentException("Serve un oggetto StudenteDTO come parametro");
                }
                yield studenteService.esamiSuperati(dto);
            }
            case "getEsamiDaSostenere" -> {
                if (parametri.length < 1 || !(parametri[0] instanceof StudenteDTO dto)) {
                    throw new IllegalArgumentException("Serve un oggetto StudenteDTO come parametro");
                }
                yield studenteService.getEsamiDaSostenere(dto);
            }
            case "getCarriera" -> studenteService.getCarriera(subject);
            case "esamiPrenotabili" -> studenteService.esamiPrenotabili(subject);
            case "getInfoStudente" -> studenteService.getInfoStudente(subject);
            case "getPianoDiStudi" -> studenteService.consultaPianoStudi(subject);
            case "getVotiDaAccettare" -> studenteService.getVotiDaAccettare(subject);
            case "pagaTassa" -> {
                if (parametri.length < 1 || !(parametri[0] instanceof String nomeTassa)) {
                    throw new IllegalArgumentException("Serve il nome della tassa come stringa");
                }
                yield studenteService.pagaTassa(subject, nomeTassa);
            }
            default -> {
                log.error("Operazione '{}' non consentita per ruolo STUDENTE", operazione);
                throw new IllegalArgumentException("Operazione non consentita per lo studente: " + operazione);
            }
        };
    }

    private Object operazioneDocente(String operazione, String subject, Object... parametri) {
        log.info("Operazione DOCENTE richiesta: '{}', parametri: {}", operazione, Arrays.toString(parametri));
        ObjectMapper mapper = new ObjectMapper();
        return switch (operazione) {
            case "getAppelli" -> docenteService.getAppelli(subject);
            case "getInfoDocente" -> docenteService.getInfoDocente(subject);
            case "inserisciVoto" -> {
                if (parametri.length < 1) throw new IllegalArgumentException("Serve un oggetto InserimentoVotoDTO");
                InserimentoVotoDTO dto = mapper.convertValue(parametri[0], InserimentoVotoDTO.class);
                yield docenteService.inserisciVoto(dto.getAppelloId(), dto.getMatricolaStudente(), dto.getVoto());
            }
            case "creaEsame" -> {
                if (parametri.length < 2 || !(parametri[0] instanceof String nome) || !(parametri[1] instanceof String descrizione)) {
                    throw new IllegalArgumentException("Servono nome e descrizione dell'esame come stringhe");
                }
                yield docenteService.creaEsame(subject, nome, descrizione);
            }
            case "getAuleDisponibili" -> {
                if (parametri.length < 1 || !(parametri[0] instanceof String data)) {
                    throw new IllegalArgumentException("Serve una data in formato stringa per cercare le aule disponibili");
                }
                yield docenteService.getAuleDisponibili(data);
            }
            case "visualizzaPrenotazioniEsame" -> {
                if (parametri.length < 1 || !(parametri[0] instanceof Number id)) {
                    throw new IllegalArgumentException("Serve un ID numerico dell'esame");
                }
                yield docenteService.visualizzaPrenotazioniEsame(id.longValue());
            }
            case "eliminaEsame" -> {
                if (parametri.length < 1 || !(parametri[0] instanceof Number id)) {
                    throw new IllegalArgumentException("Serve un ID numerico dell'esame da eliminare");
                }
                yield docenteService.eliminaEsame(id.longValue());
            }
            case "aggiornaEsame" -> {
                if (parametri.length < 2 || !(parametri[0] instanceof Number id) || !(parametri[1] instanceof EsameDTO dto)) {
                    throw new IllegalArgumentException("Serve un ID esame e un oggetto EsameDTO");
                }
                yield docenteService.aggiornaEsame(id.longValue(), dto);
            }
            case "getEsamiByDocente" -> {
                if (parametri.length < 1 || !(parametri[0] instanceof Number id)) {
                    throw new IllegalArgumentException("Serve un ID docente numerico");
                }
                yield docenteService.getEsamiByDocente(id.longValue());
            }
            case "getEsameById" -> {
                if (parametri.length < 1 || !(parametri[0] instanceof Number id)) {
                    throw new IllegalArgumentException("Serve un ID esame numerico");
                }
                yield docenteService.getEsameById(id.longValue());
            }
            case "modificaVoto" -> {
                if (parametri.length < 2 || !(parametri[0] instanceof Number votoId) || !(parametri[1] instanceof Integer voto)) {
                    throw new IllegalArgumentException("Servono ID voto (numerico) e nuovo voto (intero)");
                }
                yield docenteService.modificaVoto(votoId.longValue(), voto);
            }
            case "getVotiPerEsame" -> {
                if (parametri.length < 1 || !(parametri[0] instanceof Number id)) {
                    throw new IllegalArgumentException("Serve un ID esame numerico");
                }
                yield docenteService.getVotiPerEsame(id.longValue());
            }
            case "eliminaVoto" -> {
                if (parametri.length < 1 || !(parametri[0] instanceof Number id)) {
                    throw new IllegalArgumentException("Serve un ID voto numerico");
                }
                yield docenteService.eliminaVoto(id.longValue());
            }
            case "studenteAssente" -> {
                if (parametri.length < 3 || !(parametri[0] instanceof StudenteDTO studente) ||
                        !(parametri[1] instanceof EsameDTO esame)) {
                    throw new IllegalArgumentException("Servono: StudenteDTO, EsameDTO e codice assenza (intero)");
                }
                yield docenteService.studenteAssente(studente, esame);
            }
            case "trovaStudentiPerAppello" -> {
                if (parametri.length < 1 || !(parametri[0] instanceof Number appelloId)) {
                    throw new IllegalArgumentException("Serve un ID numerico dell'appello");
                }
                yield docenteService.trovaStudentiPerAppello(appelloId.longValue());
            }
            default -> {
                log.error("Operazione '{}' non consentita per ruolo DOCENTE", operazione);
                throw new IllegalArgumentException("Operazione non consentita per il docente: " + operazione);
            }
        };
    }
}
