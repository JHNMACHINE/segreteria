package com.universita.segreteria.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InserimentoVotoDTO {
    private Integer appelloId;
    private String matricolaStudente;
    private Integer voto;
}

