package com.universita.segreteria.dto;

import com.universita.segreteria.model.PianoDiStudi;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudenteDTO {
    private String nome;
    private String cognome;
    private String matricola;
    private PianoDiStudi pianoDiStudi;

}
