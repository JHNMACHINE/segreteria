package com.universita.segreteria.service;

import com.universita.segreteria.dto.*;
import com.universita.segreteria.mapper.EsameMapper;
import com.universita.segreteria.mapper.StudentMapper;
import com.universita.segreteria.mapper.VotoMapper;
import com.universita.segreteria.model.*;
import com.universita.segreteria.notifier.VotoNotifier;
import com.universita.segreteria.observer.StudenteObserver;
import com.universita.segreteria.repository.DocenteRepository;
import com.universita.segreteria.repository.EsameRepository;
import com.universita.segreteria.repository.StudenteRepository;
import com.universita.segreteria.repository.VotoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DocenteService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EsameRepository esameRepo;
    @Autowired
    private VotoRepository votoRepo;
    @Autowired
    private StudenteRepository studenteRepo;
    @Autowired
    private DocenteRepository docenteRepo;
    @Autowired
    private VotoNotifier votoNotifier;
    @Autowired
    private PianoStudiService pianoStudiService;

    private static final Logger logger = LoggerFactory.getLogger(DocenteService.class);


    @Transactional
    public EsameDTO creaEsame(Long docenteId, EsameDTO esameDTO) {
        Docente docente = docenteRepo.findById(docenteId).orElseThrow(() -> new RuntimeException("Docente non trovato"));

        // Controllo sulla data
        if (esameDTO.getDate() == null || !esameDTO.getDate().isAfter(LocalDate.now())) {
            throw new RuntimeException("La data dell'esame deve essere futura");
        }

        // il docente ha già un esame nello stesso giorno?
        boolean esisteGiaEsame = esameRepo.findByDocente(docente).stream().anyMatch(e -> e.getDate().equals(esameDTO.getDate()));

        if (esisteGiaEsame) {
            throw new RuntimeException("Il docente ha già un esame previsto in questa data");
        }

        Esame esame = EsameMapper.fromDTO(esameDTO, docente);
        esame.setDocente(docente);
        esameRepo.save(esame);
        return EsameMapper.toDTO(esame);
    }

    @Transactional
    public VotoDTO inserisciVoto(StudenteDTO studenteDTO, EsameDTO esameDTO, Integer voto) {
        // check sul voto negativo
        if (voto < 0) throw new RuntimeException("Il voto non può essere negativo");

        // Recupero entità dal database usando gli ID dai DTO
        Studente studente = studenteRepo.findByMatricola(studenteDTO.getMatricola()).orElseThrow(() -> new RuntimeException("Studente non trovato"));

        Esame esame = esameRepo.findByNome(esameDTO.getNome()).orElseThrow(() -> new RuntimeException("Esame non trovato"));

        // Controllo esistenza voto duplicato
        if (votoRepo.existsByStudenteAndEsame(studente, esame)) {
            throw new RuntimeException("Il voto per questo studente è già stato assegnato");
        }

        Voto votoEntity;
        if (voto < 18)
            votoEntity = Voto.builder().studente(studente).esame(esame).voto(voto).stato(StatoVoto.RIFIUTATO).build();
        else votoEntity = Voto.builder().studente(studente).esame(esame).voto(voto).stato(StatoVoto.ATTESA).build();

        // Salvataggio e notifica
        Voto savedVoto = votoRepo.save(votoEntity);

        StudenteObserver observer = new StudenteObserver(studente);
        votoNotifier.attach(observer);
        votoNotifier.notifyObservers(savedVoto);
        votoNotifier.detach(observer);

        // Conversione e ritorno del DTO
        return VotoMapper.convertiInDTO(savedVoto);
    }

    public VotoDTO studenteAssente(StudenteDTO studenteDTO, EsameDTO esameDTO, Integer voto) {
        Studente studente = studenteRepo.findByMatricola(studenteDTO.getMatricola()).orElseThrow(() -> new RuntimeException("Studente non trovato"));
        Esame esame = esameRepo.findByNome(esameDTO.getNome()).orElseThrow(() -> new RuntimeException("Esame non trovato"));

        esame.setStatoEsame(StatoEsame.ASSENTE);

        Voto votoEntity = Voto.builder().studente(studente).esame(esame).voto(0).stato(StatoVoto.RIFIUTATO).build();

        Voto savedVoto = votoRepo.save(votoEntity);

        StudenteObserver observer = new StudenteObserver(studente);
        votoNotifier.attach(observer);
        votoNotifier.notifyObservers(savedVoto);
        votoNotifier.detach(observer);

        return VotoMapper.convertiInDTO(savedVoto);

    }

    public List<EsameDTO> getEsamiByDocente(Long docenteId) {
        Docente docente = docenteRepo.findById(docenteId).orElseThrow(() -> new RuntimeException("Docente non trovato"));
        List<Esame> esami = esameRepo.findByDocente(docente);
        return EsameMapper.convertListEsamiToDTO(esami);
    }

    public EsameDTO getEsameById(Long esameId) {
        Esame esame = esameRepo.findById(esameId).orElseThrow(() -> new RuntimeException("Esame non trovato"));
        return EsameMapper.toDTO(esame);
    }


    public EsameDTO aggiornaEsame(Long esameId, EsameDTO aggiornato) {
        Esame esame = esameRepo.findById(esameId).orElseThrow(() -> new RuntimeException("Esame non trovato"));

        if (aggiornato.getDate() == null || !aggiornato.getDate().isAfter(LocalDate.now())) {
            throw new RuntimeException("La data dell'esame deve essere futura");
        }

        esame.setNome(aggiornato.getNome());
        esame.setDate(aggiornato.getDate());
        esame.setStatoEsame(aggiornato.getStatoEsame());
        esameRepo.save(esame);
        return EsameMapper.toDTO(esame);
    }


    public boolean eliminaEsame(Long esameId) {
        if (!esameRepo.existsById(esameId)) {
            throw new RuntimeException("Esame non trovato");
        }
        esameRepo.deleteById(esameId);
        return true;
    }

    public List<StudenteDTO> visualizzaPrenotazioniEsame(Long esameId) {
        Esame esame = esameRepo.findById(esameId).orElseThrow(() -> new RuntimeException("Esame non trovato"));
        List<Studente> studentiPrenotati = esame.getStudentiPrenotati();
        return StudentMapper.convertListStudentiToDTO(studentiPrenotati);
    }


    public List<VotoDTO> getVotiPerEsame(Long esameId) {
        Esame esame = esameRepo.findById(esameId).orElseThrow(() -> new RuntimeException("Esame non trovato"));
        List<Voto> voti = votoRepo.findByEsame(esame);
        return VotoMapper.convertListToDTO(voti);
    }


    public VotoDTO modificaVoto(Long votoId, Integer nuovoVoto) {
        Voto voto = votoRepo.findById(votoId).orElseThrow(() -> new RuntimeException("Voto non trovato"));

        if (voto.getStato() != StatoVoto.ATTESA) {
            throw new RuntimeException("Impossibile modificare: il voto è già stato accettato o rifiutato");
        }

        voto.setVoto(nuovoVoto);
        Voto updated = votoRepo.save(voto);
        return VotoMapper.convertiInDTO(updated);
    }


    public boolean eliminaVoto(Long votoId) {
        if (!votoRepo.existsById(votoId)) {
            throw new RuntimeException("Voto non trovato");
        }
        votoRepo.deleteById(votoId);
        return true;
    }

    public Utente initDocente(RegisterRequest request) {
        logger.info("Inizializzazione nuovo studente con email: {}", request.email());

        PianoDiStudi piano = PianoDiStudi.valueOf(request.pianoDiStudi());
        logger.info("Piano di studi selezionato: {}", piano);

        List<Esame> esamiDelPiano = pianoStudiService.getEsamiPerPiano(piano);
        logger.info("Numero di esami assegnati al piano: {}", esamiDelPiano.size());

        return Docente.builder()
                .nome(request.nome())
                .cognome(request.cognome())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .ruolo(TipoUtente.DOCENTE)
                .appelli(esamiDelPiano)
                .build();
    }

    public DocenteDTO getInfoDocente(String email) {
        Docente docente = docenteRepo.findByEmail(email).orElseThrow(() -> new RuntimeException("Docente non trovato"));
        return DocenteDTO.builder().id(docente.getId()).nome(docente.getNome()).cognome(docente.getCognome()).matricola(docente.getMatricola()).email(docente.getEmail()).build();
    }

}
