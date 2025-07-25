package com.universita.segreteria.dto;

import com.universita.segreteria.model.PianoDiStudi;
import lombok.Data;

@Data
public class CreaDocenteDTO {
    private String nome;
    private String cognome;
    private String email;
    private PianoDiStudi pianoDiStudi;
    private String corso;
}