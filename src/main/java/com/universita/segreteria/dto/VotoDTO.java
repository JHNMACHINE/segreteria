package com.universita.segreteria.dto;

import com.universita.segreteria.model.StatoVoto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VotoDTO {
    private Long id;
    private Long studenteId;
    private String studenteMatricola;
    private String studenteNome;
    private String studenteCognome;
    private Long esameId;
    private String esameNome;
    private int voto;
    private StatoVoto stato;
}
