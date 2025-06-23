package com.universita.segreteria.service;

import com.universita.segreteria.dto.*;
import com.universita.segreteria.mapper.DocenteMapper;
import com.universita.segreteria.mapper.StudentMapper;
import com.universita.segreteria.mapper.VotoMapper;
import com.universita.segreteria.model.*;
import com.universita.segreteria.notifier.AcceptationNotifier;
import com.universita.segreteria.notifier.VotoNotifier;
import com.universita.segreteria.observer.SegreteriaObserver;
import com.universita.segreteria.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SegreteriaService {

    Logger logger = LoggerFactory.getLogger(SegreteriaService.class);

    @Autowired
    private StudenteRepository studenteRepo;
    @Autowired
    private VotoRepository votoRepo;
    @Autowired
    private AcceptationNotifier acceptationNotifier;
    @Autowired
    private PianoStudiService pianoStudiService;
    @Autowired
    private DocenteRepository docenteRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private VotoNotifier votoNotifier;
    @Autowired
    private EsameRepository esameRepo;
    @Autowired
    private SegretarioRepository segretarioRepository;

    public StudenteDTO inserisciStudente(StudenteDTO studenteDTO) {
        Studente studente = studenteRepo.findByMatricola(studenteDTO.getMatricola()).orElseThrow(() -> new RuntimeException("Matricola non valida, studente non trovato"));
        List<Esame> esami = pianoStudiService.getEsamiPerPiano(studente.getPianoDiStudi());

        studente.setEsami(esami);
        studenteRepo.save(studente);
        return StudentMapper.convertiStudenteInDTO(studente);
    }

    public DocenteDTO inserisciDocente(DocenteDTO docenteDTO) {
        Docente docente = DocenteMapper.fromDTO(docenteDTO);
        Docente saved = docenteRepository.save(docente);
        return DocenteMapper.toDTO(saved);
    }

    @Transactional
    public void confermaVoto(Long votoId) {
        Voto voto = votoRepo.findById(votoId)
                .orElseThrow(() -> new RuntimeException("Voto non trovato"));

        if (voto.getStato() == StatoVoto.ATTESA) {
            voto.setStato(StatoVoto.ACCETTATO);
            votoRepo.save(voto);

            try {
                votoNotifier.notifyObservers(voto);
            } catch (Exception e) {
                logger.error("Errore nella notifica allo studente", e);
            }
        }
    }

    @Transactional
    public List<VotoDTO> getVotiInAttesa() {
        List<Voto> voti = votoRepo.findByStato(StatoVoto.ATTESA);

        // Forza il caricamento delle relazioni
        voti.forEach(voto -> {
            if (voto.getStudente() != null) {
                // Accedi a un campo per forzare il caricamento
                voto.getStudente().getNome();
            }
            if (voto.getEsame() != null) {
                voto.getEsame().getNome();
            }
        });

        return VotoMapper.convertListToDTO(voti);
    }

    public List<StudenteDTO> getAllStudenti() {
        List<Studente> studenti = studenteRepo.findAll();
        return StudentMapper.convertListStudentiToDTO(studenti);
    }

    public List<DocenteDTO> getAllDocenti() {
        List<Docente> docenti = docenteRepository.findAll();
        return docenti.stream()
                .map(DocenteMapper::toDTO)  // Usa il metodo di mapping per ogni Docente
                .collect(Collectors.toList());  // Colleziona in una lista di DocenteDTO
    }


    public List<StudenteDTO> cercaStudente(String nome, String cognome) {
        List<Studente> studenti = studenteRepo.findByNomeAndCognome(nome, cognome);
        return StudentMapper.convertListStudentiToDTO(studenti);
    }


    public StudenteDTO cercaStudentePerMatricola(String matricola) {
        Studente studente = studenteRepo.findByMatricola(matricola).orElseThrow(() -> new RuntimeException("Matricola non valida, studente non trovato"));
        return StudentMapper.convertiStudenteInDTO(studente);
    }

    public StudenteDTO cambiaPianoDiStudi(Integer studenteId, String pianoDiStudi) {
        // Verifica che studenteId sia valido
        if (studenteId == null) {
            throw new RuntimeException("ID studente non valido");
        }

        // Converti la stringa in un enum
        PianoDiStudi nuovoPiano = PianoDiStudi.valueOf(pianoDiStudi);

        // Trova lo studente e aggiorna il piano
        Studente studente = studenteRepo.findById(studenteId.longValue()).orElseThrow(() -> new RuntimeException("Studente non trovato"));
        studente.setPianoDiStudi(nuovoPiano);
        studenteRepo.save(studente);

        return StudentMapper.convertiStudenteInDTO(studente);
    }


    public SegretarioDTO getProfilo(String email) {
        logger.info("getProfile({})", email);
        return segretarioRepository.findByEmail(email)
                .map(s -> new SegretarioDTO(s.getNome(), s.getCognome()))
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Segretario non trovato")
                );
    }


    public Segretario initSegretario(RegisterRequest request) {
        return Segretario.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .nome(request.nome())
                .cognome(request.cognome())
                .matricola(request.matricola())
                .dataDiNascita(request.dataDiNascita())
                .residenza(request.residenza())
                .ruolo(TipoUtente.SEGRETARIO)
                .build();
    }


    @Transactional
    public Map<String, Object> creaDocente(CreaDocenteDTO creaDocenteDTO) {
        // Controllo esistenza email
        if (docenteRepository.existsByEmail(creaDocenteDTO.getEmail())) {
            throw new IllegalArgumentException("Un docente con questa email esiste già.");
        }

        // Genera password
        String passwordChiara = generaPassword();

        // Crea e salva il docente **prima**, senza esami
        Docente docente = Docente.builder()
                .nome(creaDocenteDTO.getNome())
                .cognome(creaDocenteDTO.getCognome())
                .email(creaDocenteDTO.getEmail())
                .password(passwordEncoder.encode(passwordChiara))
                .ruolo(TipoUtente.DOCENTE)
                .deveCambiarePassword(true)
                .build();

        docente = docenteRepository.save(docente);  // Ora docente ha un ID valido
        Esame es= esameRepo.findFirstByNome(creaDocenteDTO.getCorso()).orElseThrow(()->new RuntimeException("Esame non trovato"));

        es.setDocente(docente);
        pianoStudiService.save(List.of(es));
        // Non serve risalvare il docente se la relazione è owner sugli esami

        // Risposta
        return Map.of(
                "messaggio", "Docente creato con successo",
                "email", docente.getEmail(),
                "passwordProvvisoria", passwordChiara
        );
    }


    private String generaPassword() {
        int lunghezza = 12;
        String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lower = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String special = "!@#$%&*()_+-=[]|,./?><";
        String all = upper + lower + digits + special;

        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();

        // Garantiamo almeno un carattere di ciascun tipo
        password.append(upper.charAt(random.nextInt(upper.length())));
        password.append(lower.charAt(random.nextInt(lower.length())));
        password.append(digits.charAt(random.nextInt(digits.length())));
        password.append(special.charAt(random.nextInt(special.length())));

        // Completiamo con caratteri casuali
        for (int i = 4; i < lunghezza; i++) {
            password.append(all.charAt(random.nextInt(all.length())));
        }

        // Mischiamo i caratteri
        List<Character> chars = password.chars()
                .mapToObj(c -> (char) c)
                .collect(Collectors.toList());
        Collections.shuffle(chars, random);

        StringBuilder shuffledPassword = new StringBuilder();
        chars.forEach(shuffledPassword::append);

        return shuffledPassword.toString();
    }


    @Transactional
    public List<EsameDTO> getEsamiDisponibiliPerPiano(String piano1) {


        PianoDiStudi piano=PianoDiStudi.valueOf(piano1.toUpperCase());
        // Recupera tutti gli esami associati al piano scelto
        List<Esame> esamiDelPiano = pianoStudiService.getEsamiPerPiano(piano);

        // Filtra esami senza docente e rimuovi duplicati per nome
        return esamiDelPiano.stream()
                .filter(esame -> esame.getDocente() == null)
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(
                                Esame::getNome, // chiave = nome per evitare duplicati
                                e -> e,
                                (e1, e2) -> e1  // in caso di duplicati, tiene il primo
                        ),
                        mappa -> mappa.values().stream()
                                .map(e -> EsameDTO.builder()
                                        .id(e.getId())
                                        .nome(e.getNome())
                                        .cfu(e.getCfu())
                                        .build())
                                .collect(Collectors.toList())
                ));
    }

    @Transactional
    public Map<String, Object> creaStudente(CreaStudenteDTO dto) {
        if (studenteRepo.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Un studente con questa email esiste già.");
        }

        // Genera password
        String passwordChiara = generaPassword();
        List<Esame> esamiDelPiano = pianoStudiService.getEsamiPerPiano(dto.getPianoDiStudi());

        // Crea e salva il studente
        Studente studente = Studente.builder()
                .nome(dto.getNome())
                .cognome(dto.getCognome())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(passwordChiara))
                .ruolo(TipoUtente.STUDENTE)
                .pianoDiStudi(dto.getPianoDiStudi())
                .esami(esamiDelPiano)
                .deveCambiarePassword(true)
                .build();

        studente = studenteRepo.save(studente);

        // Risposta
        return Map.of(
                "messaggio", "Studente creato con successo",
                "email", studente.getEmail(),
                "passwordProvvisoria", passwordChiara
        );
    }
}
