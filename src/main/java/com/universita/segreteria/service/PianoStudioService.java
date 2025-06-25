package com.universita.segreteria.service;

import com.universita.segreteria.model.Esame;
import com.universita.segreteria.model.PianoDiStudi;
import com.universita.segreteria.model.StatoEsame;
import com.universita.segreteria.repository.EsameRepository;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class PianoStudioService {

    private static final Logger logger = LoggerFactory.getLogger(PianoStudioService.class);

    private final EsameRepository esameRepo;

    // Mappa di PianoDiStudi -> lista nomi esami, così eviti ripetizioni
    private static final Map<PianoDiStudi, List<String>> ESAMI_PER_PIANO = Map.of(
            PianoDiStudi.INFORMATICA, List.of("Programmazione", "Algoritmi", "Basi di dati"),
            PianoDiStudi.MATEMATICA, List.of("Analisi", "Algebra", "Geometria"),
            PianoDiStudi.BIOLOGIA, List.of("Genetica", "Biochimica", "Zoologia"),
            PianoDiStudi.GRAFICA, List.of("Design", "Colori", "Tipografia"),
            PianoDiStudi.INGEGNERIA, List.of("Fisica", "Chimica", "Matematica I"),
            PianoDiStudi.MEDICINA, List.of("Biologia", "Biochimica", "Farmacologia"),
            PianoDiStudi.GIURISPRUDENZA, List.of("Diritto Privato", "Procedura Civile", "Economia Politica")
    );

    @PostConstruct
    @Transactional
    public void initEsami() {
        if (esameRepo.count() > 0) {
            logger.info("Esami già presenti a DB.");
            return;
        }
        logger.info("Inizializzo esami di default nel database...");

        var esami = ESAMI_PER_PIANO.values().stream()
                .flatMap(List::stream)
                .distinct()
                .map(nome -> Esame.builder()
                        .nome(nome)
                        .cfu(10)
                        .statoEsame(StatoEsame.ATTIVO)
                        .studentiPrenotati(List.of())
                        .voti(List.of())
                        .build())
                .toList();

        esameRepo.saveAll(esami);
        logger.info("Esami inizializzati e salvati nel database.");
    }

    /**
     * Restituisce gli esami associati a un piano di studi,
     * tramite la mappa statica e un singolo chiamata a DB.
     */
    public List<Esame> getEsamiPerPiano(PianoDiStudi piano) {
        List<String> nomiEsami = ESAMI_PER_PIANO.get(piano);
        if (nomiEsami == null) {
            logger.warn("Piano di studi non riconosciuto: {}", piano);
            return List.of();
        }
        return esameRepo.findByNomeIn(nomiEsami);
    }

    @Transactional
    public void save(List<Esame> esamiDelPiano) {
        esameRepo.saveAll(esamiDelPiano);
    }
}
