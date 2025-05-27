package com.universita.segreteria.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RichiestaOperazione {
    private String nomeOperazione;
    private Object[] parametri;
}
