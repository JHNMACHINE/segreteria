package com.universita.segreteria.mapper;

import com.universita.segreteria.dto.DocenteDTO;
import com.universita.segreteria.model.Docente;
import org.springframework.stereotype.Component;

@Component
public final class DocenteMapper {

    public static DocenteDTO toDTO(Docente docente) {
        return DocenteDTO.builder()
                .id(docente.getId())
                .nome(docente.getNome())
                .cognome(docente.getCognome())
                .matricola(docente.getMatricola())
                .email(docente.getEmail())
                .build();
    }

    public static Docente fromDTO(DocenteDTO dto) {
        return Docente.builder()
                .id(dto.getId())
                .nome(dto.getNome())
                .cognome(dto.getCognome())
                .matricola(dto.getMatricola())
                .email(dto.getEmail())
                .build();
    }
}
