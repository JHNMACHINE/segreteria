package com.universita.segreteria.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppelloDTO {
    private Long id;
    private String nome;       // nome corso o esame
    private int cfu;
    private LocalDate data;    // data dell’appello
    private String aula;
}
