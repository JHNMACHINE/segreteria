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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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

        List<Voto> voti = studente.getVoti();
        List<Esame> superati = voti.stream()
                .filter(v -> v.getStato() == StatoVoto.ACCETTATO)
                .map(Voto::getEsame)
                .toList();

        List<Esame> giaValutati = voti.stream()
                .map(Voto::getEsame)
                .toList();

        List<Esame> giaPrenotati = studente.getEsami();
        PianoDiStudi piano = studente.getPianoDiStudi();
        LocalDate oggi = LocalDate.now();

        List<Esame> finalList = pianoStudiService.getEsamiPerPiano(piano).stream()
                .filter(e -> !superati.contains(e))
                .filter(e -> !giaValutati.contains(e))
                .filter(e -> !giaPrenotati.contains(e))
                .filter(e -> e.getData().isAfter(oggi))
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
    public Utente initStudente(RegisterRequest request) {
        logger.info("Inizializzazione nuovo studente con email: {}", request.email());

        PianoDiStudi piano = PianoDiStudi.valueOf(request.pianoDiStudi());
        logger.info("Piano di studi selezionato: {}", piano);

        // NON POPOLARE la lista esami: verrà usata solo per le prenotazioni
        Studente studente = Studente.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .ruolo(request.ruolo())
                .nome(request.nome())
                .cognome(request.cognome())
                .matricola(request.matricola())
                .dataDiNascita(request.dataDiNascita())
                .residenza(request.residenza())
                .pianoDiStudi(piano)
                // .esami NON valorizzata all'inizio
                .build();


        studenteRepo.save(studente);

        List<Tassa> tassePredefinite = List.of(
                Tassa.builder().nome("Tassa di iscrizione").prezzo(100).pagata(false).studente(studente).build(),
                Tassa.builder().nome("Tassa di esame").prezzo(50).pagata(false).studente(studente).build(),
                Tassa.builder().nome("Tassa di laboratorio").prezzo(30).pagata(false).studente(studente).build()
        );

        // Associa le tasse allo studente
        studente.setTassePagate(tassePredefinite);

        // Salva le tasse nel database
        tassaRepository.saveAll(tassePredefinite);
        logger.info("Studente '{} {}' inizializzato correttamente con matricola: {}",
                studente.getNome(), studente.getCognome(), studente.getMatricola());

        return studente;
    }


    public List<EsameDTO> getCarriera(String email) {
        Studente studente = studenteRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Studente non trovato"));

        List<Voto> voti = votoRepo.findByStudenteId(studente.getId());

        PianoDiStudi piano = studente.getPianoDiStudi();
        LocalDate oggi = LocalDate.now();

        List<Esame> esami = pianoStudiService.getEsamiPerPiano(piano);

        return esami.stream().map(e -> {
            Optional<Voto> votoEsame = voti.stream()
                    .filter(v -> v.getEsame().equals(e))
                    .findFirst();

            String stato;
            Long idVotoPrenotazione = null;

            if (votoEsame.isPresent()) {
                StatoVoto sv = votoEsame.get().getStato();
                if (sv == StatoVoto.ACCETTATO) {
                    stato = "SUPERATO";
                } else if (sv == StatoVoto.ATTESA) {
                    stato = "PRENOTATO";   // Esame prenotato ma non ancora valutato
                    idVotoPrenotazione = votoEsame.get().getId();
                } else {
                    stato = sv.name();     // Altri stati come ASSENTE, NON_AMMESSO ecc.
                }
            } else {
                // Nessun voto trovato => esame NON prenotato, prenotabile se data futura
                if (e.getData() != null && e.getData().isAfter(oggi)) {
                    stato = "NON_SUPERATO";
                } else {
                    stato = "NON_DISPONIBILE";
                }
            }

            return new EsameDTO(
                    e.getId(),               // id esame
                    e.getNome(),             // nome esame
                    e.getCfu(),              // CFU esame
                    e.getData(),             // data esame (se presente)
                    StatoEsame.valueOf(stato), // lo stato calcolato (es. SUPERATO, NON_SUPERATO, PRENOTATO)
                    idVotoPrenotazione,      // id del voto/prenotazione associata, o null
                    e.getDocente() != null ? e.getDocente().getId() : null // id docente, se presente
            );
        }).toList();
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
