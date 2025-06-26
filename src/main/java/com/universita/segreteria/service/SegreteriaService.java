package com.universita.segreteria.service;

import com.universita.segreteria.dto.*;
import com.universita.segreteria.factory.DocenteFactory;
import com.universita.segreteria.factory.StudenteFactory;
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
import java.time.LocalDate;
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

    private String generaMatricolaPerPiano(PianoDiStudi piano) {
        String prefix = switch (piano) {
            case INFORMATICA    -> "IT";
            case MATEMATICA     -> "MT";
            case BIOLOGIA       -> "BG";
            case GIURISPRUDENZA -> "GZ";
            case MEDICINA       -> "MD";
            case INGEGNERIA     -> "IN";
            case GRAFICA        -> "GF";
            default -> throw new RuntimeException("Piano di studi non riconosciuto");
        };

        List<String> matricoleEsistenti = studenteRepo.findAllMatricoleByPrefix(prefix);
        int maxSeq = matricoleEsistenti.stream()
                .map(m -> m.replace(prefix, ""))
                .mapToInt(Integer::parseInt)
                .max()
                .orElse(0);

        return prefix + String.format("%04d", maxSeq + 1);
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
        // Genera matricola automaticamente
        String matricolaPrefix = "SEG";  // Prefisso specifico per il segretario
        List<String> matricoleEsistenti = segretarioRepository.findAllMatricoleByPrefix(matricolaPrefix);

        // Ottieni il numero più alto delle matricole esistenti, estrai il numero e incrementalo
        int maxSeq = matricoleEsistenti.stream()
                .map(m -> m.replace(matricolaPrefix, ""))  // Rimuove il prefisso
                .mapToInt(Integer::parseInt)  // Converte in numero
                .max()  // Ottieni il massimo
                .orElse(0);  // Se non ci sono matricole, inizia da 0

        // Crea una nuova matricola, con un numero progressivo
        String nuovaMatricola = matricolaPrefix + String.format("%04d", maxSeq + 1);  // Esempio: SEG0001

        return Segretario.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .nome(request.nome())
                .cognome(request.cognome())
                .matricola(nuovaMatricola)  // Assegna la matricola generata
                .dataDiNascita(request.dataDiNascita())
                .residenza(request.residenza())
                .ruolo(TipoUtente.SEGRETARIO)
                .build();
    }



    @Transactional
    public Map<String, Object> creaDocente(CreaDocenteDTO creaDocenteDTO) {
        // 1. Verifica email
        if (docenteRepository.existsByEmail(creaDocenteDTO.getEmail())) {
            throw new IllegalArgumentException("Un docente con questa email esiste già.");
        }

        // 2. Genera password
        String passwordChiara = generaPassword();
        String passwordEncoded = passwordEncoder.encode(passwordChiara);

        // 3. Usa la factory
        Docente docente = DocenteFactory.creaDocente(creaDocenteDTO, passwordEncoded);

        // 4. Salva il docente
        docente = docenteRepository.save(docente);

        // 5. Assegna l'esame scelto
        Esame es = esameRepo.findFirstByNome(creaDocenteDTO.getCorso())
                .orElseThrow(() -> new RuntimeException("Esame non trovato"));

        es.setDocente(docente);
        pianoStudiService.save(List.of(es));

        // 6. Risposta
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
    public List<VotoDTO> getVotiAccettatiPerStudente(String matricola) {
        Studente studente = studenteRepo.findByMatricola(matricola)
                .orElseThrow(() -> new RuntimeException("Studente non trovato"));

        List<Voto> voti = votoRepo.findByStudenteAndStato(studente, StatoVoto.ACCETTATO);

        // Forza il caricamento delle relazioni per evitare problemi con lazy loading
        voti.forEach(voto -> {
            if (voto.getEsame() != null) {
                voto.getEsame().getNome();
            }
        });

        return VotoMapper.convertListToDTO(voti);
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
        // 1. Verifica email
        if (studenteRepo.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Un studente con questa email esiste già.");
        }

        // 2. Genera matricola usando il metodo centralizzato
        String matricola = generaMatricolaPerPiano(dto.getPianoDiStudi());

        // 3. Genera password chiara ed encode
        String passwordChiara = generaPassword();
        String passwordEncoded = passwordEncoder.encode(passwordChiara);

        // 4. Recupera esami e tasse
        List<Esame> esami = pianoStudiService.getEsamiPerPiano(dto.getPianoDiStudi());

        List<Tassa> tasse = List.of(
                Tassa.builder().nome("Tassa di Iscrizione").prezzo(100).pagata(false).build(),
                Tassa.builder().nome("Tassa Annuale").prezzo(200).pagata(false).build(),
                Tassa.builder().nome("Tassa Biblioteca").prezzo(50).pagata(false).build()
        );

        // 5. Usa la Factory
        Studente studente = StudenteFactory.creaStudente(dto, matricola, passwordEncoded, esami, tasse);

        // 6. Associa le tasse (riferimento bidirezionale)
        Studente finalStudente = studente;
        tasse.forEach(t -> t.setStudente(finalStudente));

        // 7. Salva
        studente = studenteRepo.save(studente);

        // 8. Risposta
        return Map.of(
                "messaggio", "Studente creato con successo",
                "matricola", studente.getMatricola(),
                "email", studente.getEmail(),
                "passwordProvvisoria", passwordChiara
        );
    }
}