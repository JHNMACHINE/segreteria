package com.universita.segreteria.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Entity
@Inheritance(strategy = InheritanceType.JOINED) // Se hai sottoclassi tipo Studente, Docente...
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class Utente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoUtente ruolo;

    private String nome;
    private String cognome;
    private String matricola;
    private LocalDate dataDiNascita;
    private String residenza;


    @Column(nullable = false)
    private boolean deveCambiarePassword = false;
}
