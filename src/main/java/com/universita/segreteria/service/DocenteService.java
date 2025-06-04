package com.universita.segreteria.service;

import com.universita.segreteria.dto.EsameDTO;
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
        Docente docente = docenteRepo.findById(docenteId)
                .orElseThrow(() -> new RuntimeException("Docente non trovato"));

        // Controllo sulla data
        if (esameDTO.getData() == null || !esameDTO.getData().isAfter(LocalDate.now())) {
            throw new RuntimeException("La data dell'esame deve essere futura");
        }

        Esame esame = convertiDaDTO(esameDTO);
        esame.setDocente(docente);
        esameRepo.save(esame);
        return convertiInDTO(esame);
    }


    private EsameDTO convertiInDTO(Esame esame) {
        return EsameDTO.builder()
                .nome(esame.getNome())
                .data(esame.getDate())
                .statoEsame(esame.getStatoEsame())
                .build();
    }

    private Esame convertiDaDTO(EsameDTO dto) {
        return Esame.builder()
                .nome(dto.getNome())
                .date(dto.getData())
                .statoEsame(dto.getStatoEsame())
                .build();
    }

    @Transactional
    public Voto inserisciVoto(Long studenteId, Long esameId, Integer voto) {
        Studente studente = studenteRepo.findById(studenteId)
                .orElseThrow(() -> new RuntimeException("Studente non trovato"));
        Esame esame = esameRepo.findById(esameId)
                .orElseThrow(() -> new RuntimeException("Esame non trovato"));

        if (votoRepo.existsByStudenteAndEsame(studente, esame)) {
            throw new RuntimeException("Il voto per questo studente è già stato assegnato");
        }

        Voto votoEntity = new Voto();
        votoEntity.setStudente(studente);
        votoEntity.setEsame(esame);
        votoEntity.setVoto(voto);
        votoEntity.setStato(StatoVoto.IN_ATTESA);

        Voto savedVoto = votoRepo.save(votoEntity);

        StudenteObserver observer = new StudenteObserver(studente);
        votoNotifier.attach(observer);
        votoNotifier.notifyObservers(savedVoto);
        votoNotifier.detach(observer);

        return savedVoto;
    }

    public List<EsameDTO> getEsamiByDocente(Long docenteId) {
        Docente docente = docenteRepo.findById(docenteId)
                .orElseThrow(() -> new RuntimeException("Docente non trovato"));

        return esameRepo.findByDocente(docente).stream()
                .map(this::convertiInDTO)
                .toList();
    }

    public EsameDTO getEsameById(Long esameId) {
        Esame esame = esameRepo.findById(esameId)
                .orElseThrow(() -> new RuntimeException("Esame non trovato"));
        return convertiInDTO(esame);
    }


    public EsameDTO aggiornaEsame(Long esameId, EsameDTO aggiornato) {
        Esame esame = esameRepo.findById(esameId)
                .orElseThrow(() -> new RuntimeException("Esame non trovato"));

        if (aggiornato.getData() == null || !aggiornato.getData().isAfter(LocalDate.now())) {
            throw new RuntimeException("La data dell'esame deve essere futura");
        }

        esame.setNome(aggiornato.getNome());
        esame.setDate(aggiornato.getData());
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

    public List<Studente> visualizzaPrenotazioniEsame(Long esameId) {
        Esame esame = esameRepo.findById(esameId)
                .orElseThrow(() -> new RuntimeException("Esame non trovato"));
        return esame.getStudentiPrenotati();
    }

    public List<Voto> getVotiPerEsame(Long esameId) {
        Esame esame = esameRepo.findById(esameId)
                .orElseThrow(() -> new RuntimeException("Esame non trovato"));
        return votoRepo.findByEsame(esame);
    }

    public Voto modificaVoto(Long votoId, Integer nuovoVoto) {
        Voto voto = votoRepo.findById(votoId)
                .orElseThrow(() -> new RuntimeException("Voto non trovato"));

        if (voto.getStato() != StatoVoto.IN_ATTESA) {
            throw new RuntimeException("Impossibile modificare: il voto è già stato accettato o rifiutato");
        }

        voto.setVoto(nuovoVoto);
        return votoRepo.save(voto);
    }

    public boolean eliminaVoto(Long votoId) {
        if (!votoRepo.existsById(votoId)) {
            throw new RuntimeException("Voto non trovato");
        }
        votoRepo.deleteById(votoId);
        return true;
    }
}
