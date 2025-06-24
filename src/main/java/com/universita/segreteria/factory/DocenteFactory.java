package com.universita.segreteria.factory;

import com.universita.segreteria.dto.CreaDocenteDTO;
import com.universita.segreteria.model.Docente;
import com.universita.segreteria.model.TipoUtente;

public class DocenteFactory {

    public static Docente creaDocente(CreaDocenteDTO dto, String passwordEncoded) {
        return Docente.builder()
                .nome(dto.getNome())
                .cognome(dto.getCognome())
                .email(dto.getEmail())
                .password(passwordEncoded)
                .ruolo(TipoUtente.DOCENTE)
                .deveCambiarePassword(true)
                .build();
    }
}

