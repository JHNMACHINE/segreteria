package com.universita.segreteria.dto;

import com.universita.segreteria.model.TipoUtente;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RichiestaOperazione {
    private String nomeOperazione;
    private Object[] parametri;
    private TipoUtente ruolo;
}
