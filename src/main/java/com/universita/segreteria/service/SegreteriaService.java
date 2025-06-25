package com.universita.segreteria.service;

import com.universita.segreteria.dto.*;
import com.universita.segreteria.factory.DocenteFactory;
import com.universita.segreteria.factory.StudenteFactory;
import com.universita.segreteria.mapper.DocenteMapper;
import com.universita.segreteria.mapper.StudentMapper;
import com.universita.segreteria.mapper.VotoMapper;
import com.universita.segreteria.model.*;
import com.universita.segreteria.notifier.VotoNotifier;
import com.universita.segreteria.repository.*;
import com.universita.segreteria.util.MatricolaGenerator;
import com.universita.segreteria.util.PasswordGenerator;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SegreteriaService {

    Logger logger = LoggerFactory.getLogger(SegreteriaService.class);

    final private StudenteRepository studenteRepo;
    final private VotoRepository votoRepo;
    final private PianoStudioService pianoStudioService;
    final private DocenteRepository docenteRepository;
    final private PasswordEncoder passwordEncoder;
    final private VotoNotifier votoNotifier;
    final private EsameRepository esameRepo;
    final private SegretarioRepository segretarioRepository;

    public StudenteDTO inserisciStudente(StudenteDTO studenteDTO) {
        Studente studente = studenteRepo.findByMatricola(studenteDTO.getMatricola()).orElseThrow(() -> new EntityNotFoundException("Matricola non valida, studente non trovato"));
        List<Esame> esami = pianoStudioService.getEsamiPerPiano(studente.getPianoDiStudi());

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
    public boolean confermaVoto(Long votoId) {
        Voto voto = votoRepo.findById(votoId)
                .orElseThrow(() -> new EntityNotFoundException("Voto non trovato"));

        if (voto.getStato() != StatoVoto.ATTESA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Voto non in stato di attesa");
        }

        voto.setStato(StatoVoto.ACCETTATO);
        votoRepo.save(voto);

        try {
            votoNotifier.notifyObservers(voto);
        } catch (Exception e) {
            logger.error("Errore nella notifica allo studente", e);
            return false;
        }
        return true;
    }


    @Transactional
    public List<VotoDTO> getVotiInAttesa() {
        List<Voto> voti = votoRepo.findByStatoWithDetails(StatoVoto.ATTESA);
        return VotoMapper.convertListToDTO(voti);
    }


    public List<StudenteDTO> getAllStudenti() {
        List<Studente> studenti = studenteRepo.findAll();
        return StudentMapper.convertListStudentiToDTO(studenti);
    }

    public List<DocenteDTO> getAllDocenti() {
        List<Docente> docenti = docenteRepository.findAll();
        return docenti.stream().map(DocenteMapper::toDTO)  // Usa il metodo di mapping per ogni Docente
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
        Studente studente = studenteRepo.findById(studenteId.longValue()).orElseThrow(() -> new EntityNotFoundException("Studente non trovato"));
        studente.setPianoDiStudi(nuovoPiano);
        studenteRepo.save(studente);

        return StudentMapper.convertiStudenteInDTO(studente);
    }


    public SegretarioDTO getProfilo(String email) {
        logger.info("getProfile({})", email);
        return segretarioRepository.findByEmail(email).map(s -> new SegretarioDTO(s.getNome(), s.getCognome())).orElseThrow(() -> new EntityNotFoundException("Segretario non trovato"));
    }


    public Segretario initSegretario(RegisterRequest request) {
        if (request.email() == null || request.email().isBlank()) {
            throw new IllegalArgumentException("Email mancante per il segretario");
        }

        // Genera matricola automaticamente
        String matricolaPrefix = "SEG";  // Prefisso specifico per il segretario
        List<String> matricoleEsistenti = segretarioRepository.findAllMatricoleByPrefix(matricolaPrefix);

        String matricola = MatricolaGenerator.generaMatricola(matricolaPrefix, matricoleEsistenti);

        return Segretario.builder().email(request.email()).password(passwordEncoder.encode(request.password())).nome(request.nome()).cognome(request.cognome()).matricola(matricola)
                .dataDiNascita(request.dataDiNascita()).residenza(request.residenza()).ruolo(TipoUtente.SEGRETARIO).build();
    }


    @Transactional
    public Map<String, Object> creaDocente(CreaDocenteDTO creaDocenteDTO) {
        // 1. Verifica email
        if (docenteRepository.existsByEmail(creaDocenteDTO.getEmail())) {
            throw new IllegalArgumentException("Un docente con questa email esiste già.");
        }

        // 2. Genera password
        String passwordChiara = PasswordGenerator.genera();
        String passwordEncoded = passwordEncoder.encode(passwordChiara);

        // 3. Usa la factory
        Docente docente = DocenteFactory.creaDocente(creaDocenteDTO, passwordEncoded);

        // 4. Salva il docente
        docente = docenteRepository.save(docente);

        // 5. Assegna l'esame scelto
        Esame es = esameRepo.findFirstByNome(creaDocenteDTO.getCorso()).orElseThrow(() -> new RuntimeException("Esame non trovato"));

        es.setDocente(docente);
        pianoStudioService.save(List.of(es));

        // 6. Risposta
        return Map.of("messaggio", "Docente creato con successo", "email", docente.getEmail(), "passwordProvvisoria", passwordChiara);
    }


    @Transactional
    public List<EsameDTO> getEsamiDisponibiliPerPiano(String piano1) {
        PianoDiStudi piano;
        try {
            piano = PianoDiStudi.valueOf(piano1.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Piano di studi non valido");
        }

        // Recupera tutti gli esami associati al piano scelto
        List<Esame> esamiDelPiano = pianoStudioService.getEsamiPerPiano(piano);

        // Filtra esami senza docente e rimuovi duplicati per nome
        return esamiDelPiano.stream().filter(esame -> esame.getDocente() == null).collect(Collectors.collectingAndThen(Collectors.toMap(Esame::getNome, // chiave = nome per evitare duplicati
                e -> e, (e1, _) -> e1  // in caso di duplicati, tiene il primo
        ), mappa -> mappa.values().stream().map(e -> EsameDTO.builder().id(e.getId()).nome(e.getNome()).cfu(e.getCfu()).build()).collect(Collectors.toList())));
    }

    @Transactional
    public Map<String, Object> creaStudente(CreaStudenteDTO dto) {
        logger.info("Creazione studente con email: {}", dto.getEmail());
        // 1. Verifica email
        if (studenteRepo.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Un studente con questa email esiste già.");
        }
        String matricola = MatricolaGenerator.generaMatricolaStudente(dto.getPianoDiStudi());

        // 3. Genera password chiara ed encode
        String passwordChiara = PasswordGenerator.genera();
        String passwordEncoded = passwordEncoder.encode(passwordChiara);

        // 4. Recupera esami e tasse
        List<Esame> esami = pianoStudioService.getEsamiPerPiano(dto.getPianoDiStudi());

        List<Tassa> tasse = List.of(Tassa.builder().nome("Tassa di Iscrizione").prezzo(100).pagata(false).build(), Tassa.builder().nome("Tassa Annuale").prezzo(200).pagata(false).build(), Tassa.builder().nome("Tassa Biblioteca").prezzo(50).pagata(false).build());

        // 5. Usa la Factory
        Studente studente = StudenteFactory.creaStudente(dto, matricola, passwordEncoded, esami, tasse);

        // 6. Associa le tasse (riferimento bidirezionale)
        Studente finalStudente = studente;
        tasse.forEach(t -> t.setStudente(finalStudente));

        // 7. Salva
        studente = studenteRepo.save(studente);

        // 8. Risposta
        return Map.of("messaggio", "Studente creato con successo", "matricola", studente.getMatricola(), "email", studente.getEmail(), "passwordProvvisoria", passwordChiara);
    }


}
