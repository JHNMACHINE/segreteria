package com.universita.segreteria.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocenteDTO {
    private Long id;
    private String nome;
    private String cognome;
    private String matricola;  // se usi matricola anche per docenti
    private String email;
}
