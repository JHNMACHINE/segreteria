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
    private Long id;
    private String nome;
    private LocalDate date;
    private StatoEsame statoEsame;
    private Long docenteId;
}
