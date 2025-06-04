package com.universita.segreteria.service;

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
    public Esame creaEsame(Long docenteId, Esame esame) {
        Docente docente = docenteRepo.findById(docenteId)
                .orElseThrow(() -> new RuntimeException("Docente non trovato"));
        esame.setDocente(docente);
        return esameRepo.save(esame);
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

    public List<Esame> getEsamiByDocente(Long docenteId) {
        Docente docente = docenteRepo.findById(docenteId)
                .orElseThrow(() -> new RuntimeException("Docente non trovato"));
        return esameRepo.findByDocente(docente);
    }

    public Esame getEsameById(Long esameId) {
        return esameRepo.findById(esameId)
                .orElseThrow(() -> new RuntimeException("Esame non trovato"));
    }

    public Esame aggiornaEsame(Long esameId, Esame aggiornato) {
        Esame esame = esameRepo.findById(esameId)
                .orElseThrow(() -> new RuntimeException("Esame non trovato"));
        esame.setNome(aggiornato.getNome());
        esame.setDate(aggiornato.getDate());
        esame.setStatoEsame(aggiornato.getStatoEsame());
        return esameRepo.save(esame);
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

    // ✅ Nuovo metodo: visualizza i voti per un esame
    public List<Voto> getVotiPerEsame(Long esameId) {
        Esame esame = esameRepo.findById(esameId)
                .orElseThrow(() -> new RuntimeException("Esame non trovato"));
        return votoRepo.findByEsame(esame);
    }

    // ✅ Nuovo metodo: modifica voto (se non ancora accettato o rifiutato)
    public Voto modificaVoto(Long votoId, Integer nuovoVoto) {
        Voto voto = votoRepo.findById(votoId)
                .orElseThrow(() -> new RuntimeException("Voto non trovato"));

        if (voto.getStato() != StatoVoto.IN_ATTESA) {
            throw new RuntimeException("Impossibile modificare: il voto è già stato accettato o rifiutato");
        }

        voto.setVoto(nuovoVoto);
        return votoRepo.save(voto);
    }

    // ✅ Nuovo metodo: elimina voto (per reset o errore)
    public void eliminaVoto(Long votoId) {
        if (!votoRepo.existsById(votoId)) {
            throw new RuntimeException("Voto non trovato");
        }
        votoRepo.deleteById(votoId);
    }
}
