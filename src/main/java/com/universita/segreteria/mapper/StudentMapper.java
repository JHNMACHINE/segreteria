package com.universita.segreteria.mapper;

import com.universita.segreteria.dto.StudenteDTO;
import com.universita.segreteria.model.Studente;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public final class StudentMapper {
    public static StudenteDTO convertiStudenteInDTO(Studente studente) {
        return StudenteDTO.builder().matricola(studente.getMatricola()).nome(studente.getNome()).cognome(studente.getCognome()).pianoDiStudi(studente.getPianoDiStudi()).build();
    }

    public static Studente convertiStudenteDaDTO(StudenteDTO dto) {
        return Studente.builder().matricola(dto.getMatricola()).nome(dto.getNome()).cognome(dto.getCognome()).build();
    }

    public static List<StudenteDTO> convertListStudentiToDTO(List<Studente> studenti) {
        if (studenti == null) return Collections.emptyList();

        return studenti.stream().map(StudentMapper::convertiStudenteInDTO).collect(Collectors.toList());
    }

    public static List<Studente> convertListDTOToStudenti(List<StudenteDTO> dtos) {
        if (dtos == null) return Collections.emptyList();

        return dtos.stream().map(StudentMapper::convertiStudenteDaDTO).collect(Collectors.toList());
    }

}
