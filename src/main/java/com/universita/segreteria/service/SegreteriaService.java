package com.universita.segreteria.service;

import com.universita.segreteria.dto.DocenteDTO;
import com.universita.segreteria.dto.SegretarioDTO;
import com.universita.segreteria.dto.StudenteDTO;
import com.universita.segreteria.dto.VotoDTO;
import com.universita.segreteria.mapper.DocenteMapper;
import com.universita.segreteria.mapper.StudentMapper;
import com.universita.segreteria.mapper.VotoMapper;
import com.universita.segreteria.model.*;
import com.universita.segreteria.notifier.AcceptationNotifier;
import com.universita.segreteria.observer.SegreteriaObserver;
import com.universita.segreteria.repository.DocenteRepository;
import com.universita.segreteria.repository.SegretarioRepository;
import com.universita.segreteria.repository.StudenteRepository;
import com.universita.segreteria.repository.VotoRepository;
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

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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


    public VotoDTO confermaVoto(StudenteDTO studenteDTO, Long votoId) {
        Voto voto = votoRepo.findById(votoId).orElseThrow(() -> new RuntimeException("Voto non trovato"));

        if (Objects.isNull(studenteDTO.getMatricola()))
            throw new RuntimeException("Matricola mancate, inserire matricola");

        String matricola = studenteDTO.getMatricola();

        Studente studente = studenteRepo.findByMatricola(matricola).orElseThrow(() -> new RuntimeException("Matricola non valida, studente non trovato"));

        // Verifica che il voto appartenga allo studente
        if (!voto.getStudente().getId().equals(studente.getId())) {
            throw new RuntimeException("Questo voto non appartiene allo studente");
        }

        // Cambia lo stato se necessario
        if (voto.getStato() == StatoVoto.ATTESA) {
            voto.setStato(StatoVoto.ACCETTATO);
            voto = votoRepo.save(voto);
        }

        // Notifica la segreteria
        SegreteriaObserver segreteria = new SegreteriaObserver();
        acceptationNotifier.attach(segreteria);
        acceptationNotifier.notifyObservers(voto);
        acceptationNotifier.detach(segreteria);

        return VotoMapper.convertiInDTO(voto);
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

    public StudenteDTO cambiaPianoDiStudi(String email, Integer studenteId, String pianoDiStudi) {
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


    public Segretario initSegretario(com.universita.segreteria.dto.RegisterRequest request) {
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

}
