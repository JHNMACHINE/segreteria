package com.universita.segreteria.dto;

import com.universita.segreteria.model.StatoEsame;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EsameDTO {
    private String nome;
    private LocalDate data;
    private StatoEsame statoEsame;
}