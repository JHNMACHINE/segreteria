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
    private Long id;           // id esame
    private String nome;
    private int cfu;           // aggiunto CFU per frontend
    private LocalDate date;
    private StatoEsame statoEsame;  // stato dell'esame (SUPERATO, NON_SUPERATO, PRENOTATO ecc)
    private Long votoId;       // id del Voto se esiste (per azioni tipo prenota o disdici)
    private Long docenteId;
}
