package com.universita.segreteria.service;

import com.universita.segreteria.model.Esame;
import com.universita.segreteria.model.Studente;
import com.universita.segreteria.model.Voto;
import com.universita.segreteria.repository.EsameRepository;
import com.universita.segreteria.repository.StudenteRepository;
import com.universita.segreteria.repository.VotoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DocenteService {
    private final EsameRepository esameRepo;
    private final VotoRepository votoRepo;
    private final StudenteRepository studenteRepo;

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
}
