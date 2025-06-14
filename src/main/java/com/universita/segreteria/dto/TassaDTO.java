package com.universita.segreteria.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TassaDTO {
    private String nome;
    private int prezzo;
    private boolean pagata;
}


