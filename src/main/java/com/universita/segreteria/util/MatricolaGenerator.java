package com.universita.segreteria.util;

import com.universita.segreteria.model.PianoDiStudi;
import com.universita.segreteria.repository.StudenteRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class MatricolaGenerator {
    @Autowired
    static private StudenteRepository studenteRepo;

    /**
     * Genera una matricola sequenziale dato un prefisso e la lista delle matricole esistenti.
     * Es. prefix = "SEG", esistenti = ["SEG0001", "SEG0002"] => restituisce "SEG0003"
     *
     * @param prefix             prefisso della matricola (es. "SEG", "IT")
     * @param matricoleEsistenti lista matricole già presenti
     * @return nuova matricola generata
     */
    public static String generaMatricola(String prefix, List<String> matricoleEsistenti) {
        int maxSeq = matricoleEsistenti.stream()
                .map(m -> m.replace(prefix, ""))
                .mapToInt(seq -> {
                    try {
                        return Integer.parseInt(seq);
                    } catch (NumberFormatException e) {
                        return 0; // Ignora valori non conformi
                    }
                })
                .max()
                .orElse(0);

        return prefix + String.format("%04d", maxSeq + 1);
    }

    public static String generaMatricolaStudente(PianoDiStudi piano) {
        String prefix = switch (piano) {
            case INFORMATICA -> "IT";
            case MATEMATICA -> "MT";
            case BIOLOGIA -> "BG";
            case GIURISPRUDENZA -> "GZ";
            case MEDICINA -> "MD";
            case INGEGNERIA -> "IN";
            case GRAFICA -> "GF";
        };
        List<String> matricoleEsistenti = studenteRepo.findAllMatricoleByPrefix(prefix);
        int maxSeq = matricoleEsistenti.stream().map(m -> m.replace(prefix, "")).mapToInt(Integer::parseInt).max().orElse(0);
        String nuovaSeq = String.format("%04d", maxSeq + 1);
        return prefix + nuovaSeq;
    }


}
