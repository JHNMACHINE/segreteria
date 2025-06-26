package com.universita.segreteria.mapper;

import com.universita.segreteria.dto.StudenteDTO;
import com.universita.segreteria.dto.TassaDTO;
import com.universita.segreteria.model.Studente;
import com.universita.segreteria.model.Tassa;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public final class StudentMapper {

    public static TassaDTO convertiTassaInDTO(Tassa tassa) {
        return TassaDTO.builder()
                .nome(tassa.getNome())
                .prezzo(tassa.getPrezzo())
                .pagata(tassa.isPagata())
                .build();
    }

    public static StudenteDTO convertiStudenteInDTO(Studente studente) {
        return StudenteDTO.builder()
                .id(studente.getId())
                .matricola(studente.getMatricola())
                .nome(studente.getNome())
                .cognome(studente.getCognome())
                .pianoDiStudi(studente.getPianoDiStudi())
                .residenza(studente.getResidenza())  // Aggiunto
                .dataDiNascita(studente.getDataDiNascita())  // Aggiunto
                .email(studente.getEmail())
                .tassePagate(
                        Optional.ofNullable(studente.getTassePagate())
                                .orElse(Collections.emptyList())
                                .stream()
                                .map(StudentMapper::convertiTassaInDTO)
                                .collect(Collectors.toList())
                )
                .build();
    }

    public static List<StudenteDTO> convertListStudentiToDTO(List<Studente> studenti) {
        if (studenti == null) return Collections.emptyList();

        return studenti.stream().map(StudentMapper::convertiStudenteInDTO).collect(Collectors.toList());
    }
}


