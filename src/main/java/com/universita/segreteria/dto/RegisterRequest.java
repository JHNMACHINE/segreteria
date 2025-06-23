package com.universita.segreteria.dto;

import com.universita.segreteria.model.TipoUtente;

import java.time.LocalDate;

public record RegisterRequest(String email, String password, TipoUtente ruolo, String nome, String cognome,
                              String matricola, LocalDate dataDiNascita, String pianoDiStudi, String residenza,String corso) {
}