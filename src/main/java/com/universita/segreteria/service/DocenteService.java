package com.universita.segreteria.service;

import com.universita.segreteria.dto.EsameDTO;
import com.universita.segreteria.dto.StudenteDTO;
import com.universita.segreteria.dto.VotoDTO;
import com.universita.segreteria.mapper.StudentMapper;
import com.universita.segreteria.model.*;
import com.universita.segreteria.notifier.VotoNotifier;
import com.universita.segreteria.observer.StudenteObserver;
import com.universita.segreteria.repository.DocenteRepository;
import com.universita.segreteria.repository.EsameRepository;
import com.universita.segreteria.repository.StudenteRepository;
import com.universita.segreteria.repository.VotoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DocenteService {

    private final EsameRepository esameRepo;
    private final VotoRepository votoRepo;
    private final StudenteRepository studenteRepo;
    private final DocenteRepository docenteRepo;
    private final VotoNotifier votoNotifier;

    @Transactional
    public EsameDTO creaEsame(Long docenteId, EsameDTO esameDTO) {
        Docente docente = docenteRepo.findById(docenteId).orElseThrow(() -> new RuntimeException("Docente non trovato"));

        // Controllo sulla data
        if (esameDTO.getDate() == null || !esameDTO.getDate().isAfter(LocalDate.now())) {
            throw new RuntimeException("La data dell'esame deve essere futura");
        }

        Esame esame = convertiDaDTO(esameDTO);
        esame.setDocente(docente);
        esameRepo.save(esame);
        return convertiInDTO(esame);
    }


    private EsameDTO convertiInDTO(Esame esame) {
        return EsameDTO.builder().nome(esame.getNome()).date(esame.getDate()).statoEsame(esame.getStatoEsame()).build();
    }

    private Esame convertiDaDTO(EsameDTO dto) {
        return Esame.builder().nome(dto.getNome()).date(dto.getDate()).statoEsame(dto.getStatoEsame()).build();
    }

    private VotoDTO convertiInDTO(Voto voto) {
        return VotoDTO.builder().id(voto.getId()).studenteId(voto.getStudente().getId()).studenteMatricola(voto.getStudente().getMatricola()).studenteNome(voto.getStudente().getNome()).studenteCognome(voto.getStudente().getCognome()).esameId(voto.getEsame().getId()).esameNome(voto.getEsame().getNome()).voto(voto.getVoto()).stato(voto.getStato()).build();
    }

    private Voto convertiDaDTO(VotoDTO votoDTO, Studente studente, Esame esame) {
        return Voto.builder().id(votoDTO.getId()).studente(studente).esame(esame).voto(votoDTO.getVoto()).stato(votoDTO.getStato()).build();
    }

    @Transactional
    public VotoDTO inserisciVoto(StudenteDTO studenteDTO, EsameDTO esameDTO, Integer voto) {
        // Recupero entità dal database usando gli ID dai DTO
        Studente studente = studenteRepo.findByMatricola(studenteDTO.getMatricola()).orElseThrow(() -> new RuntimeException("Studente non trovato"));

        Esame esame = esameRepo.findByNome(esameDTO.getNome()).orElseThrow(() -> new RuntimeException("Esame non trovato"));

        // Controllo esistenza voto duplicato
        if (votoRepo.existsByStudenteAndEsame(studente, esame)) {
            throw new RuntimeException("Il voto per questo studente è già stato assegnato");
        }

        // Creazione entità Voto
        Voto votoEntity = Voto.builder().studente(studente).esame(esame).voto(voto).stato(StatoVoto.IN_ATTESA).build();

        // Salvataggio e notifica
        Voto savedVoto = votoRepo.save(votoEntity);

        StudenteObserver observer = new StudenteObserver(studente);
        votoNotifier.attach(observer);
        votoNotifier.notifyObservers(savedVoto);
        votoNotifier.detach(observer);

        // Conversione e ritorno del DTO
        return convertiInDTO(savedVoto);
    }


    public List<EsameDTO> getEsamiByDocente(Long docenteId) {
        Docente docente = docenteRepo.findById(docenteId).orElseThrow(() -> new RuntimeException("Docente non trovato"));

        return esameRepo.findByDocente(docente).stream().map(this::convertiInDTO).toList();
    }

    public EsameDTO getEsameById(Long esameId) {
        Esame esame = esameRepo.findById(esameId).orElseThrow(() -> new RuntimeException("Esame non trovato"));
        return convertiInDTO(esame);
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
        return convertiInDTO(esame);
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

        return votoRepo.findByEsame(esame).stream().map(this::convertiInDTO).toList();
    }


    public VotoDTO modificaVoto(Long votoId, Integer nuovoVoto) {
        Voto voto = votoRepo.findById(votoId).orElseThrow(() -> new RuntimeException("Voto non trovato"));

        if (voto.getStato() != StatoVoto.IN_ATTESA) {
            throw new RuntimeException("Impossibile modificare: il voto è già stato accettato o rifiutato");
        }

        voto.setVoto(nuovoVoto);
        Voto updated = votoRepo.save(voto);
        return convertiInDTO(updated);
    }


    public boolean eliminaVoto(Long votoId) {
        if (!votoRepo.existsById(votoId)) {
            throw new RuntimeException("Voto non trovato");
        }
        votoRepo.deleteById(votoId);
        return true;
    }
}
