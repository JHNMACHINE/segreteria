package com.universita.segreteria.service;

import com.universita.segreteria.model.Esame;
import com.universita.segreteria.model.PianoDiStudi;
import com.universita.segreteria.repository.EsameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PianoStudiService {

    @Autowired
    private EsameRepository esameRepo;

    // Metodo che restituisce gli esami associati a un piano di studi
    public List<Esame> getEsamiPerPiano(PianoDiStudi piano) {
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