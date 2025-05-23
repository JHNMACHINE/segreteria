package com.universita.segreteria.service;


import com.universita.segreteria.model.Docente;
import com.universita.segreteria.model.Esame;
import com.universita.segreteria.model.Studente;
import com.universita.segreteria.model.Voto;
import com.universita.segreteria.repository.DocenteRepository;
import com.universita.segreteria.repository.EsameRepository;
import com.universita.segreteria.repository.StudenteRepository;
import com.universita.segreteria.repository.VotoRepository;
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

    public Esame inserisciAppello(Esame esame) {
        return esameRepo.save(esame);
    }

    public Voto inserisciVoto(Long studenteId, Long esameId, int voto, boolean assente) {
        Studente studente = studenteRepo.findById(studenteId)
                .orElseThrow(() -> new RuntimeException("Studente non trovato"));
        Esame esame = esameRepo.findById(esameId)
                .orElseThrow(() -> new RuntimeException("Esame non trovato"));

        Voto votoEntity = new Voto();
        votoEntity.setStudente(studente);
        votoEntity.setEsame(esame);
        votoEntity.setVoto(voto);

        return votoRepo.save(votoEntity);
    }

    public Esame creaEsame(Long docenteId, Esame esame) {
        Docente docente = docenteRepo.findById(docenteId)
                .orElseThrow(() -> new RuntimeException("Docente non trovato"));
        esame.setDocente(docente);
        return esameRepo.save(esame);
    }


    public Esame getEsameById(Long esameId) {
        return esameRepo.findById(esameId)
                .orElseThrow(() -> new RuntimeException("Esame non trovato"));
    }


    public List<Esame> getEsamiByDocente(Long docenteId) {
        Docente docente = docenteRepo.findById(docenteId)
                .orElseThrow(() -> new RuntimeException("Docente non trovato"));
        return esameRepo.findByDocente(docente);
    }


    public Esame aggiornaEsame(Long esameId, Esame aggiornato) {
        Esame esame = esameRepo.findById(esameId)
                .orElseThrow(() -> new RuntimeException("Esame non trovato"));

        esame.setNome(aggiornato.getNome());
        esame.setDate(aggiornato.getDate());
        esame.setStatoEsame(aggiornato.getStatoEsame());

        return esameRepo.save(esame);
    }


    public void eliminaEsame(Long esameId) {
        if (!esameRepo.existsById(esameId)) {
            throw new RuntimeException("Esame non trovato");
        }
        esameRepo.deleteById(esameId);
    }

    public List<Studente> visualizzaPrenotazioniEsame(Long esameId) {
        Esame esame = esameRepo.findById(esameId)
                .orElseThrow(() -> new RuntimeException("Esame non trovato"));

        return esame.getStudentiPrenotati();
    }

}
