package com.universita.segreteria.service;

import com.universita.segreteria.dto.*;
import com.universita.segreteria.mapper.EsameMapper;
import com.universita.segreteria.mapper.StudentMapper;
import com.universita.segreteria.mapper.VotoMapper;
import com.universita.segreteria.model.*;
import com.universita.segreteria.notifier.VotoNotifier;
import com.universita.segreteria.observer.StudenteObserver;
import com.universita.segreteria.repository.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocenteService {

    final private EsameRepository esameRepo;
    final private VotoRepository votoRepo;
    final private StudenteRepository studenteRepo;
    final private DocenteRepository docenteRepo;
    final private VotoNotifier votoNotifier;
    final private PrenotazioneRepository prenotazioneRepository;

    private static final Logger logger = LoggerFactory.getLogger(DocenteService.class);


    @Transactional
    public EsameDTO creaEsame(String email, String dataStr, String aulaStr) {
        Docente docente = docenteRepo.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("Docente non trovato"));

        LocalDate dataEsame = parseDataEsame(dataStr);
        Aula aula = parseAula(aulaStr);

        if (esisteEsameNellaData(docente, dataEsame)) {
            throw new IllegalStateException("Il docente ha già un esame previsto in questa data");
        }

        Esame nuovoEsame = Esame.builder().nome("Esame di " + docente.getCognome()) // oppure passare come parametro
                .docente(docente).data(dataEsame).aula(aula).voti(List.of()).statoEsame(StatoEsame.ATTIVO).build();

        Esame salvato = esameRepo.save(nuovoEsame);
        return EsameMapper.toDTO(salvato);
    }


    private LocalDate parseDataEsame(String dataStr) {
        try {
            LocalDate data = LocalDate.parse(dataStr);
            if (!data.isAfter(LocalDate.now())) {
                throw new IllegalArgumentException("La data dell'esame deve essere futura");
            }
            return data;
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Formato data non valido", e);
        }
    }

    private Aula parseAula(String aulaStr) {
        try {
            return Aula.valueOf(aulaStr);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Aula non valida", e);
        }
    }

    private boolean esisteEsameNellaData(Docente docente, LocalDate data) {
        return esameRepo.existsByDocenteAndData(docente, data); // BETTER PERFORMANCE
    }


    public List<Aula> getAuleDisponibili(String dataStr) {
        LocalDate dataEsame = parseDataEsame(dataStr);

        Set<Aula> tutteLeAule = EnumSet.allOf(Aula.class);
        List<Esame> esamiInData = esameRepo.findByData(dataEsame);

        logger.debug("Esami trovati per la data {}: {}", dataEsame, esamiInData);
        logger.info("Numero esami per la data {}: {}", dataEsame, esamiInData.size());


        Set<Aula> auleOccupate = esamiInData.stream().map(Esame::getAula).filter(Objects::nonNull).collect(Collectors.toSet());

        logger.info("Aule occupate il {}: {}", dataEsame, auleOccupate);

        return tutteLeAule.stream().filter(aula -> !auleOccupate.contains(aula)).collect(Collectors.toList());
    }


    @Transactional
    public VotoDTO inserisciVoto(Integer appelloId, String matricola, Integer voto) {
        if (voto == null || voto < 0) {
            throw new IllegalArgumentException("Il voto non può essere negativo o nullo");
        }

        Studente studente = studenteRepo.findByMatricola(matricola).orElseThrow(() -> new IllegalArgumentException("Studente non trovato con matricola: " + matricola));

        Esame esame = esameRepo.findById(appelloId.longValue()).orElseThrow(() -> new IllegalArgumentException("Esame non trovato con ID: " + appelloId));

        if (votoRepo.existsByStudenteAndEsame(studente, esame)) {
            throw new IllegalStateException("Voto già presente per questo studente all’esame");
        }

        StatoVoto stato = voto < 18 ? StatoVoto.RIFIUTATO : StatoVoto.ATTESA;

        Voto votoEntity = Voto.builder().studente(studente).esame(esame).voto(voto).stato(stato).build();

        Voto savedVoto = votoRepo.save(votoEntity);

        notificaStudente(votoEntity, studente);

        return VotoMapper.convertiInDTO(savedVoto);
    }

    private void notificaStudente(Voto voto, Studente studente) {
        StudenteObserver observer = new StudenteObserver(studente);
        votoNotifier.attach(observer);
        try {
            votoNotifier.notifyObservers(voto);
        } finally {
            votoNotifier.detach(observer);
        }
    }

    @Transactional
    public VotoDTO studenteAssente(StudenteDTO studenteDTO, EsameDTO esameDTO) {
        Studente studente = studenteRepo.findByMatricola(studenteDTO.getMatricola()).orElseThrow(() -> new IllegalArgumentException("Studente non trovato con matricola: " + studenteDTO.getMatricola()));

        Esame esame = esameRepo.findFirstByNome(esameDTO.getNome()).orElseThrow(() -> new IllegalArgumentException("Esame non trovato con nome: " + esameDTO.getNome()));

        Voto votoEntity = Voto.builder().studente(studente).esame(esame).voto(0) // indica assenza
                .stato(StatoVoto.RIFIUTATO) // assenza o bocciatura
                .build();

        Voto savedVoto = votoRepo.save(votoEntity);

        notificaStudente(votoEntity, studente);

        return VotoMapper.convertiInDTO(savedVoto);
    }


    public List<EsameDTO> getEsamiByDocente(Long docenteId) {
        Docente docente = docenteRepo.findById(docenteId).orElseThrow(() -> new EntityNotFoundException("Docente non trovato"));
        List<Esame> esami = esameRepo.findByDocente(docente);
        return EsameMapper.convertListEsamiToDTO(esami);
    }

    public EsameDTO getEsameById(Long esameId) {
        Esame esame = esameRepo.findById(esameId).orElseThrow(() -> new EntityNotFoundException("Esame non trovato"));
        return EsameMapper.toDTO(esame);
    }


    public EsameDTO aggiornaEsame(Long esameId, EsameDTO aggiornato) {
        Esame esame = esameRepo.findById(esameId).orElseThrow(() -> new EntityNotFoundException("Esame non trovato"));

        if (aggiornato.getDate() == null || !aggiornato.getDate().isAfter(LocalDate.now())) {
            throw new RuntimeException("La data dell'esame deve essere futura");
        }

        esame.setNome(aggiornato.getNome());
        esame.setData(aggiornato.getDate());
        esame.setStatoEsame(aggiornato.getStatoEsame());
        esameRepo.save(esame);
        return EsameMapper.toDTO(esame);
    }


    @Transactional
    public boolean eliminaEsame(Long esameId) {
        // Verifica che l'esame esista
        Esame esame = esameRepo.findById(esameId).orElseThrow(() -> new EntityNotFoundException("Esame non trovato"));

        studenteRepo.removeEsameFromStudentiPrenotati(esame.getId());

        // Elimina l'esame
        esameRepo.delete(esame);
        return true;
    }


    public List<StudenteDTO> visualizzaPrenotazioniEsame(Long esameId) {
        Esame esame = esameRepo.findById(esameId).orElseThrow(() -> new EntityNotFoundException("Esame non trovato"));

        List<Prenotazione> prenotazioni = prenotazioneRepository.findByEsame(esame);
        List<Studente> studentiPrenotati = prenotazioni.stream().map(Prenotazione::getStudente).toList();

        return StudentMapper.convertListStudentiToDTO(studentiPrenotati);
    }


    public List<VotoDTO> getVotiPerEsame(Long esameId) {
        Esame esame = esameRepo.findById(esameId).orElseThrow(() -> new EntityNotFoundException("Esame non trovato"));
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

    public DocenteDTO getInfoDocente(String email) {
        Docente docente = docenteRepo.findByEmail(email).orElseThrow(() -> new RuntimeException("Docente non trovato"));
        return DocenteDTO.builder().id(docente.getId()).nome(docente.getNome()).cognome(docente.getCognome()).matricola(docente.getMatricola()).email(docente.getEmail()).build();
    }


    @Transactional
    public List<AppelloDTO> getAppelli(String emailDocente) {
        Docente docente = docenteRepo.findByEmail(emailDocente).orElseThrow(() -> new RuntimeException("Docente non trovato"));

        List<Esame> appelli = docente.getAppelli();

        return appelli.stream().filter(esame -> esame.getData() != null).map(esame -> AppelloDTO.builder().id(esame.getId()).nome(esame.getNome()).cfu(esame.getCfu()).data(esame.getData()).build()).collect(Collectors.toList());
    }

    @Transactional
    public List<StudenteDTO> trovaStudentiPerAppello(Long appelloId) {
        Esame esame = esameRepo.findById(appelloId).orElseThrow(() -> new EntityNotFoundException("Esame non trovato"));

        List<Prenotazione> prenotazioni = prenotazioneRepository.findByEsame(esame);

        return prenotazioni.stream().map(p -> {
            Studente s = p.getStudente();
            return StudenteDTO.builder().matricola(s.getMatricola()).nome(s.getNome()).cognome(s.getCognome()).build();
        }).toList();
    }


}
