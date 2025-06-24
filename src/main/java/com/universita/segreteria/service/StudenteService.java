package com.universita.segreteria.service;


import com.universita.segreteria.controller.UtenteProxyController;
import com.universita.segreteria.dto.EsameDTO;
import com.universita.segreteria.dto.RegisterRequest;
import com.universita.segreteria.dto.StudenteDTO;
import com.universita.segreteria.dto.TassaDTO;
import com.universita.segreteria.mapper.EsameMapper;
import com.universita.segreteria.model.*;
import com.universita.segreteria.notifier.AcceptationNotifier;
import com.universita.segreteria.repository.EsameRepository;
import com.universita.segreteria.repository.StudenteRepository;
import com.universita.segreteria.repository.TassaRepository;
import com.universita.segreteria.repository.VotoRepository;
import jakarta.transaction.Transactional;
import jdk.jshell.spi.ExecutionControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StudenteService {
    private static final Logger logger = LoggerFactory.getLogger(StudenteService.class);

    @Autowired
    private EsameRepository esameRepo;
    @Autowired
    private StudenteRepository studenteRepo;
    @Autowired
    private VotoRepository votoRepo;
    @Autowired
    private PianoStudiService pianoStudiService;
    @Autowired
    private TassaRepository tassaRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AcceptationNotifier acceptationNotifier; // Aggiungi questa dipendenza

    public List<EsameDTO> esamiPrenotabili(String email) {
        Studente studente = studenteRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Studente non trovato"));

        // Ottieni i nomi dei corsi già superati (indipendentemente dall'appello)
        Set<String> corsiSuperati = studente.getVoti().stream()
                .filter(v -> v.getStato() == StatoVoto.ACCETTATO)
                .map(v -> v.getEsame().getNome())
                .collect(Collectors.toSet());

        // Ottieni gli esami già prenotati
        List<Esame> giaPrenotati = studente.getEsami();

        // Ottieni gli esami già valutati (qualsiasi stato)
        List<Esame> giaValutati = studente.getVoti().stream()
                .map(Voto::getEsame)
                .toList();

        PianoDiStudi piano = studente.getPianoDiStudi();
        LocalDate oggi = LocalDate.now();

        List<Esame> finalList = pianoStudiService.getEsamiPerPiano(piano).stream()
                .filter(e -> !corsiSuperati.contains(e.getNome())) // Escludi corsi già superati
                .filter(e -> !giaValutati.contains(e)) // Escludi esami già valutati
                .filter(e -> !giaPrenotati.contains(e))// Escludi esami già prenotati
                .filter(e->e.getData()!=null)
                .filter(e -> e.getData().isAfter(oggi)) // Solo esami futuri
                .toList();

        return EsameMapper.convertListEsamiToDTO(finalList);
    }


    public List<EsameDTO> esamiSuperati(StudenteDTO studenteDTO) {
        if (Objects.isNull(studenteDTO.getMatricola()))
            throw new RuntimeException("Matricola mancante, inserire matricola");

        String matricola = studenteDTO.getMatricola();

        Studente studente = studenteRepo.findByMatricola(matricola).orElseThrow(() -> new RuntimeException("Matricola non valida, studente non trovato"));
        List<Esame> esami = studente.getVoti().stream().filter(v -> v.getStato() == StatoVoto.ACCETTATO).map(Voto::getEsame).toList();
        return EsameMapper.convertListEsamiToDTO(esami);
    }

    @Transactional
    public EsameDTO prenotaEsame(String emailUtente, Integer esameId) {
        // Trova lo studente per email
        Studente studente = studenteRepo
                .findByEmail(emailUtente)
                .orElseThrow(() -> new RuntimeException("Studente non trovato"));

        // Trova l'esame per ID
        Esame esame = esameRepo
                .findById(esameId.longValue()) // converte l'ID in Long
                .orElseThrow(() -> new RuntimeException("Esame non trovato"));

        // Verifica se lo studente è già prenotato
        if (esame.getStudentiPrenotati().contains(studente)) {
            throw new RuntimeException("Lo studente è già prenotato per questo esame");
        }

        // Aggiungi lo studente alla lista degli studenti prenotati
        esame.getStudentiPrenotati().add(studente);
        // Aggiungi l'esame alla lista degli esami dello studente
        studente.getEsami().add(esame);

        // Modifica lo stato dell'esame a PRENOTATO
        esame.setStatoEsame(StatoEsame.PRENOTATO);

        // Salva le modifiche
        esameRepo.save(esame);
        studenteRepo.save(studente);

        // Restituisce l'esame aggiornato come DTO
        return EsameMapper.toDTO(esame);
    }



    public PianoDiStudi consultaPianoStudi(String email) {
        Studente studente = studenteRepo.findByEmail(email).orElseThrow(() -> new RuntimeException("Studente non trovato"));
        return studente.getPianoDiStudi();
    }

    public void aggiornaStatoVoto(Long votoId, boolean accetta) {
        Voto voto = votoRepo.findById(votoId).orElseThrow(() -> new RuntimeException("Voto non assegnato"));
        if (voto.getStato() != StatoVoto.ATTESA) {
            throw new RuntimeException("Il voto è già stato accettato o rifiutato");
        }

        if (accetta) {
            // Notifica la segreteria senza cambiare stato
            acceptationNotifier.notifyObservers(voto);
        } else {
            voto.setStato(StatoVoto.RIFIUTATO);
            votoRepo.save(voto);
            // Notifica la segreteria del rifiuto
            acceptationNotifier.notifyObservers(voto);
        }
    }

    public List<Esame> getEsamiDaSostenere(StudenteDTO studenteDTO) {
        String matricola = studenteDTO.getMatricola();

        Studente studente = studenteRepo.findByMatricola(matricola).orElseThrow(() -> new RuntimeException("Matricola non valida, studente non trovato"));

        return studente.getVoti().stream().filter(v -> v.getStato() != StatoVoto.ACCETTATO).map(Voto::getEsame).collect(Collectors.toList());
    }

    @Transactional  // <-- LAZY LOADING NON RIMUOVERE
    public StudenteDTO getInfoStudente(String email) {
        Studente studente = studenteRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Studente non trovato"));

        List<TassaDTO> tasse = studente.getTassePagate().stream()
                .map(t -> TassaDTO.builder()
                        .nome(t.getNome())
                        .prezzo(t.getPrezzo())
                        .pagata(t.isPagata())
                        .build())
                .toList();

        return StudenteDTO.builder()
                .nome(studente.getNome())
                .cognome(studente.getCognome())
                .matricola(studente.getMatricola())
                .pianoDiStudi(studente.getPianoDiStudi())
                .tassePagate(tasse)
                .build();
    }


    public Object getVotiDaAccettare(String mail) {
        Studente studente = studenteRepo.findByEmail(mail)
                .orElseThrow(() -> new RuntimeException("Studente non trovato"));

        return studente.getVoti().stream()
                .filter(v -> v.getStato() == StatoVoto.ATTESA)
                .map(v -> Map.of(
                        "id", v.getId(),
                        "voto", v.getVoto(),
                        "esame", Map.of(
                                "id", v.getEsame().getId(),
                                "nome", v.getEsame().getNome()
                        )
                ))
                .toList();
    }


    @Transactional
    public List<EsameDTO> getCarriera(String email) {
        Studente studente = studenteRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Studente non trovato"));

        List<Voto> voti = votoRepo.findByStudenteId(studente.getId());
        Map<String, EsameDTO> esamiUnici = new LinkedHashMap<>();

        PianoDiStudi piano = studente.getPianoDiStudi();
        List<Esame> esamiPiano = pianoStudiService.getEsamiPerPiano(piano);

        for (Esame esame : esamiPiano) {
            String nomeEsame = esame.getNome();

            // Trovo tutti i voti per questo esame
            List<Voto> votiEsame = voti.stream()
                    .filter(v -> v.getEsame().getNome().equals(nomeEsame))
                    .toList();

            StatoEsame statoAggiornato = StatoEsame.NON_SUPERATO;
            Long votoId        = null;
            Integer valoreVoto = null;
            LocalDate dataEsame = null;

            for (Voto vObj : votiEsame) {
                if (vObj.getStato() == StatoVoto.ACCETTATO) {
                    statoAggiornato = StatoEsame.SUPERATO;
                    votoId          = vObj.getId();
                    valoreVoto      = vObj.getVoto();           // ← qui prendo il voto numerico
                    dataEsame       = vObj.getEsame().getData();
                    break;
                } else if (vObj.getStato() == StatoVoto.ATTESA) {
                    statoAggiornato = StatoEsame.PRENOTATO;
                    votoId          = vObj.getId();
                    // valoreVoto rimane null
                    dataEsame       = vObj.getEsame().getData();
                    // non interrompo: potrei trovare un ACCETTATO dopo
                }
            }

            // Inserisco o aggiorno il DTO (tenendo priorità al SUPERATO)
            if (!esamiUnici.containsKey(nomeEsame)
                    || esamiUnici.get(nomeEsame).getStatoEsame() != StatoEsame.SUPERATO) {

                EsameDTO dto = EsameDTO.builder()
                        .id(esame.getId())
                        .nome(nomeEsame)
                        .cfu(esame.getCfu())
                        .date(dataEsame)
                        .statoEsame(statoAggiornato)
                        .votoId(votoId)
                        .voto(valoreVoto)               // ← passo qui il voto
                        .docenteId(esame.getDocente()!=null ? esame.getDocente().getId() : null)
                        .aula(esame.getAula())
                        .build();

                esamiUnici.put(nomeEsame, dto);
            }
        }

        return new ArrayList<>(esamiUnici.values());
    }


    @Transactional
    public void pagaTassa(String email, String nomeTassa) {
        Studente studente = studenteRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Studente non trovato"));

        studente.getTassePagate().stream()
                .filter(t -> t.getNome().equalsIgnoreCase(nomeTassa))
                .findFirst()
                .ifPresentOrElse(t -> t.setPagata(true),
                        () -> { throw new RuntimeException("Tassa non trovata"); });

        studenteRepo.save(studente);
    }



}
