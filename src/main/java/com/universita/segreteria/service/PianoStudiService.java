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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class PianoStudiService {
    private static final Logger logger = LoggerFactory.getLogger(PianoStudiService.class);


    @Autowired
    private EsameRepository esameRepo;

    @PostConstruct
    public void initEsami() {
        if (esameRepo.count() > 0) {
            System.out.println("Esami già presenti a DB.");
            return;
        }
        List<Esame> esami = List.of(
                new Esame(null, "Programmazione", LocalDate.now().plusWeeks(3), 10, StatoEsame.ATTIVO,  null, new ArrayList<>(), new ArrayList<>()),
                new Esame(null, "Algoritmi", LocalDate.now().plusWeeks(4),10, StatoEsame.ATTIVO,  null, new ArrayList<>(), new ArrayList<>()),
                new Esame(null, "Basi di dati", LocalDate.now().plusWeeks(5),10, StatoEsame.ATTIVO,  null, new ArrayList<>(), new ArrayList<>()),
                new Esame(null, "Analisi", LocalDate.now().plusWeeks(3),10, StatoEsame.ATTIVO, null, new ArrayList<>(), new ArrayList<>()),
                new Esame(null, "Algebra", LocalDate.now().plusWeeks(4), 10,StatoEsame.ATTIVO,  null, new ArrayList<>(), new ArrayList<>()),
                new Esame(null, "Geometria", LocalDate.now().plusWeeks(5),10, StatoEsame.ATTIVO,  null, new ArrayList<>(), new ArrayList<>()),
                new Esame(null, "Genetica", LocalDate.now().plusWeeks(3), 10,StatoEsame.ATTIVO,  null, new ArrayList<>(), new ArrayList<>()),
                new Esame(null, "Biochimica", LocalDate.now().plusWeeks(4),10, StatoEsame.ATTIVO,  null, new ArrayList<>(), new ArrayList<>()),
                new Esame(null, "Zoologia", LocalDate.now().plusWeeks(5),10, StatoEsame.ATTIVO,  null, new ArrayList<>(), new ArrayList<>()),
                new Esame(null, "Design", LocalDate.now().plusWeeks(3), 10,StatoEsame.ATTIVO,  null, new ArrayList<>(), new ArrayList<>()),
                new Esame(null, "Colori", LocalDate.now().plusWeeks(4),10, StatoEsame.ATTIVO, null, new ArrayList<>(), new ArrayList<>()),
                new Esame(null, "Tipografia", LocalDate.now().plusWeeks(5), 10,StatoEsame.ATTIVO,  null, new ArrayList<>(), new ArrayList<>()),
                new Esame(null, "Fisica", LocalDate.now().plusWeeks(3),10, StatoEsame.ATTIVO,  null, new ArrayList<>(), new ArrayList<>()),
                new Esame(null, "Chimica", LocalDate.now().plusWeeks(4), 10,StatoEsame.ATTIVO,  null, new ArrayList<>(), new ArrayList<>()),
                new Esame(null, "Matematica I", LocalDate.now().plusWeeks(5), 10,StatoEsame.ATTIVO,  null, new ArrayList<>(), new ArrayList<>()),
                new Esame(null, "Biologia", LocalDate.now().plusWeeks(3),10, StatoEsame.ATTIVO, null, new ArrayList<>(), new ArrayList<>()),
                new Esame(null, "Farmacologia", LocalDate.now().plusWeeks(4), 10,StatoEsame.ATTIVO,  null, new ArrayList<>(), new ArrayList<>()),
                new Esame(null, "Diritto Privato", LocalDate.now().plusWeeks(3), 10,StatoEsame.ATTIVO,  null, new ArrayList<>(), new ArrayList<>()),
                new Esame(null, "Procedura Civile", LocalDate.now().plusWeeks(4), 10,StatoEsame.ATTIVO,  null, new ArrayList<>(), new ArrayList<>()),
                new Esame(null, "Economia Politica", LocalDate.now().plusWeeks(5),10, StatoEsame.ATTIVO,  null, new ArrayList<>(), new ArrayList<>())
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
            case GIURISPRUDENZA -> esameRepo.findByNomeIn(List.of("Diritto Privato", "Procedura Civile", "Economia Politica"));
        };
    }
}