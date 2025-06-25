package com.universita.segreteria.service;

import com.universita.segreteria.model.Esame;
import com.universita.segreteria.model.PianoDiStudi;
import com.universita.segreteria.model.StatoEsame;
import com.universita.segreteria.repository.EsameRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PianoStudioService {
    private static final Logger logger = LoggerFactory.getLogger(PianoStudioService.class);

    @Autowired
    private EsameRepository esameRepo;

    @PostConstruct
    public void initEsami() {
        if (esameRepo.count() > 0) {
            logger.info("Esami già presenti a DB.");
            return;
        }
        List<Esame> esami = List.of(
                new Esame(null, "Programmazione", null, 10, StatoEsame.ATTIVO, null, new ArrayList<>(), new ArrayList<>(), null),
                new Esame(null, "Algoritmi", null, 10, StatoEsame.ATTIVO, null, new ArrayList<>(), new ArrayList<>(), null),
                new Esame(null, "Basi di dati", null, 10, StatoEsame.ATTIVO, null, new ArrayList<>(), new ArrayList<>(), null),
                new Esame(null, "Analisi", null, 10, StatoEsame.ATTIVO, null, new ArrayList<>(), new ArrayList<>(), null),
                new Esame(null, "Algebra", null, 10, StatoEsame.ATTIVO, null, new ArrayList<>(), new ArrayList<>(), null),
                new Esame(null, "Geometria", null, 10, StatoEsame.ATTIVO, null, new ArrayList<>(), new ArrayList<>(), null),
                new Esame(null, "Genetica", null, 10, StatoEsame.ATTIVO, null, new ArrayList<>(), new ArrayList<>(), null),
                new Esame(null, "Biochimica", null, 10, StatoEsame.ATTIVO, null, new ArrayList<>(), new ArrayList<>(), null),
                new Esame(null, "Zoologia", null, 10, StatoEsame.ATTIVO, null, new ArrayList<>(), new ArrayList<>(), null),
                new Esame(null, "Design", null, 10, StatoEsame.ATTIVO, null, new ArrayList<>(), new ArrayList<>(), null),
                new Esame(null, "Colori", null, 10, StatoEsame.ATTIVO, null, new ArrayList<>(), new ArrayList<>(), null),
                new Esame(null, "Tipografia", null, 10, StatoEsame.ATTIVO, null, new ArrayList<>(), new ArrayList<>(), null),
                new Esame(null, "Fisica", null, 10, StatoEsame.ATTIVO, null, new ArrayList<>(), new ArrayList<>(), null),
                new Esame(null, "Chimica", null, 10, StatoEsame.ATTIVO, null, new ArrayList<>(), new ArrayList<>(), null),
                new Esame(null, "Matematica I", null, 10, StatoEsame.ATTIVO, null, new ArrayList<>(), new ArrayList<>(), null),
                new Esame(null, "Biologia", null, 10, StatoEsame.ATTIVO, null, new ArrayList<>(), new ArrayList<>(), null),
                new Esame(null, "Farmacologia", null, 10, StatoEsame.ATTIVO, null, new ArrayList<>(), new ArrayList<>(), null),
                new Esame(null, "Diritto Privato", null, 10, StatoEsame.ATTIVO, null, new ArrayList<>(), new ArrayList<>(), null),
                new Esame(null, "Procedura Civile", null, 10, StatoEsame.ATTIVO, null, new ArrayList<>(), new ArrayList<>(), null),
                new Esame(null, "Economia Politica", null, 10, StatoEsame.ATTIVO, null, new ArrayList<>(), new ArrayList<>(), null)
        );

        esameRepo.saveAll(esami);
        logger.info("Esami inizializzati e salvati nel database.");
    }

    // Metodo che restituisce gli esami associati a un piano di studi
    public List<Esame> getEsamiPerPiano(PianoDiStudi piano) {
        logger.info("Piano: {}", piano);
        return switch (piano) {
            case INFORMATICA -> esameRepo.findByNomeIn(List.of("Programmazione", "Algoritmi", "Basi di dati"));
            case MATEMATICA -> esameRepo.findByNomeIn(List.of("Analisi", "Algebra", "Geometria"));
            case BIOLOGIA -> esameRepo.findByNomeIn(List.of("Genetica", "Biochimica", "Zoologia"));
            case GRAFICA -> esameRepo.findByNomeIn(List.of("Design", "Colori", "Tipografia"));
            case INGEGNERIA -> esameRepo.findByNomeIn(List.of("Fisica", "Chimica", "Matematica I"));
            case MEDICINA -> esameRepo.findByNomeIn(List.of("Biologia", "Biochimica", "Farmacologia"));
            case GIURISPRUDENZA ->
                    esameRepo.findByNomeIn(List.of("Diritto Privato", "Procedura Civile", "Economia Politica"));
        };
    }

    public void save(List<Esame> esamiDelPiano) {
        esameRepo.saveAll(esamiDelPiano);
    }
}