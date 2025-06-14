package com.universita.segreteria.dto;

import com.universita.segreteria.model.PianoDiStudi;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudenteDTO {
    private String nome;
    private String cognome;
    private String matricola;
    private PianoDiStudi pianoDiStudi;
    private List<TassaDTO> tassePagate;
    private String residenza;
    private LocalDate dataDiNascita;
}
