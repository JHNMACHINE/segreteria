package com.universita.segreteria.factory;


import com.universita.segreteria.dto.CreaStudenteDTO;
import com.universita.segreteria.model.*;

import java.time.LocalDate;
import java.util.List;

public class StudenteFactory {
    public static Studente creaStudente(CreaStudenteDTO dto, String matricola, String passwordEncoded, List<Esame> esami, List<Tassa> tasse) {
        return Studente.builder()
                .matricola(matricola)
                .nome(dto.getNome())
                .cognome(dto.getCognome())
                .email(dto.getEmail())
                .password(passwordEncoded)
                .ruolo(TipoUtente.STUDENTE)
                .pianoDiStudi(dto.getPianoDiStudi())
                .dataDiNascita(LocalDate.parse(dto.getDataDiNascita()))
                .residenza(dto.getResidenza())
                .esami(esami)
                .tassePagate(tasse)
                .deveCambiarePassword(true)
                .build();
    }
}

