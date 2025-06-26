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
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

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
    @Autowired
    private StudenteRepository studenteRepository;

    private static final Logger logger = LoggerFactory.getLogger(DocenteService.class);


    @Transactional
    public EsameDTO creaEsame(String email,String dataStr,String aulaStr ) {
        Docente docente = docenteRepo.findByEmail(email).orElseThrow(() -> new RuntimeException("Docente non trovato"));

        LocalDate data1;
        try {
            data1 = LocalDate.parse(dataStr);
        } catch (DateTimeParseException e) {
            throw new RuntimeException("Formato data non valido");
        }
        if (!data1.isAfter(LocalDate.now())) {
            throw new RuntimeException("La data dell'esame deve essere futura");
        }

        Aula aula;
        try {
            aula = Aula.valueOf(aulaStr);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Aula non valida");
        }


        // il docente ha già un esame nello stesso giorno?
        boolean esisteGiaEsame = esameRepo.findByDocente(docente)
                .stream()
                .anyMatch(e -> Objects.equals(e.getData(), data1));

        if (esisteGiaEsame) {
            throw new RuntimeException("Il docente ha già un esame previsto in questa data");
        }


        Esame es=esameRepo.findFirstByDocente(docente).orElseThrow(()->new RuntimeException("Esame non trovato"));
        Esame esame=es.toBuilder()
                .id(null)
                .docente(docente)
                .data(data1)
                .voti(List.of())
                .studentiPrenotati(List.of())
                .statoEsame(StatoEsame.ATTIVO)
                .aula(aula)
                .build();
        esameRepo.save(esame);
        return EsameMapper.toDTO(esame);
    }
    public List<Aula> getAuleDisponibili(String dataStr) {
        LocalDate data1;
        try {
            data1 = LocalDate.parse(dataStr);
        } catch (DateTimeParseException e) {
            throw new RuntimeException("Formato data non valido");
        }
        if (!data1.isAfter(LocalDate.now())) {
            throw new RuntimeException("La data dell'esame deve essere futura");
        }

        if (data1 == null) {
            throw new RuntimeException("Data non specificata");
        }
        List<Aula> tutte = Arrays.asList(Aula.values());

        List<Esame> esamiInData = esameRepo.findByData(data1);
        logger.info("Esami trovati per data {}: {}", data1, esamiInData);
        List<Aula> occupate = esamiInData.stream()
                .map(Esame::getAula)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        logger.info("Aule occupate in data {}: {}", data1, occupate);

        return tutte.stream()
                .filter(a -> !occupate.contains(a))
                .collect(Collectors.toList());
    }

    @Transactional
    public VotoDTO inserisciVoto(Integer appelloId, String matricola, Integer voto) {
        // check sul voto negativo
        if (voto < 0) throw new RuntimeException("Il voto non può essere negativo");

        // Recupero entità dal database usando gli ID dai DTO
        Studente studente = studenteRepo.findByMatricola(matricola).orElseThrow(() -> new RuntimeException("Studente non trovato"));

        Esame esame = esameRepo.findById(Long.valueOf(appelloId)).orElseThrow(() -> new RuntimeException("Esame non trovato"));

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
        Esame esame = esameRepo.findFirstByNome(esameDTO.getNome()).orElseThrow(() -> new RuntimeException("Esame non trovato"));

        esame.setStatoEsame(StatoEsame.ASSENTE);

        Voto votoEntity = Voto.builder().studente(studente).esame(esame).voto(0).stato(StatoVoto.RIFIUTATO).build();

        Voto savedVoto = votoRepo.save(votoEntity);

        StudenteObserver observer = new StudenteObserver(studente);
        votoNotifier.attach(observer);
        votoNotifier.notifyObservers(savedVoto);
        votoNotifier.detach(observer);

        return VotoMapper.convertiInDTO(savedVoto);

    }


    @Transactional
    public boolean eliminaEsame(Long esameId) {
        // Verifica che l'esame esista
        Esame esame = esameRepo.findById(esameId)
                .orElseThrow(() -> new RuntimeException("Esame non trovato"));

        // Rimuovi l'associazione con gli studenti prenotati
        for (Studente studente : esame.getStudentiPrenotati()) {
            studente.getEsami().remove(esame); // Rimuove l'esame dalla lista degli esami dello studente
        }
        esame.getStudentiPrenotati().clear(); // Rimuove l'esame dalla lista di studenti prenotati nell'esame

        // Elimina l'esame
        esameRepo.delete(esame);
        return true;
    }



    public DocenteDTO getInfoDocente(String email) {
        Docente docente = docenteRepo.findByEmail(email).orElseThrow(() -> new RuntimeException("Docente non trovato"));
        return DocenteDTO.builder().id(docente.getId()).nome(docente.getNome()).cognome(docente.getCognome()).matricola(docente.getMatricola()).email(docente.getEmail()).build();
    }


    @Transactional
    public List<AppelloDTO> getAppelli(String emailDocente) {
        Docente docente = docenteRepo.findByEmail(emailDocente)
                .orElseThrow(() -> new RuntimeException("Docente non trovato"));

        List<Esame> appelli = docente.getAppelli();

        return appelli.stream()
                .filter(esame -> esame.getData()!=null)
                .map(esame -> AppelloDTO.builder()
                        .id(esame.getId())
                        .nome(esame.getNome())
                        .cfu(esame.getCfu())
                        .data(esame.getData())
                        .build()
                ).collect(Collectors.toList());
    }

    @Transactional
    public List<StudenteDTO> trovaStudentiPerAppello(Long appelloId) {
        Esame esame = esameRepo.findById(appelloId)
                .orElseThrow(() -> new RuntimeException("Esame non trovato"));

        return esame.getStudentiPrenotati().stream()
                .map(s -> StudenteDTO.builder()
                        .matricola(s.getMatricola())
                        .nome(s.getNome())
                        .cognome(s.getCognome())
                        .build())
                .toList();
    }
}