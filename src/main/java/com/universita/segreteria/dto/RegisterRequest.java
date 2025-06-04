package com.universita.segreteria.dto;

import com.universita.segreteria.model.TipoUtente;

public record RegisterRequest(String email, String password, TipoUtente ruolo, String nome, String cognome,
                              String matricola) {
}
