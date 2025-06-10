package com.universita.segreteria.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Studente extends Utente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String residenza;
    private LocalDate dataDiNascita;

    @Enumerated(EnumType.STRING)
    private PianoDiStudi pianoDiStudi;

    @OneToMany(mappedBy = "studente", cascade = CascadeType.ALL)
    private List<Tassa> tassePagate;

    @OneToMany(mappedBy = "studente", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Voto> voti;

    @OneToMany(mappedBy = "studente", cascade = CascadeType.ALL)
    private List<Esame> esami;
}

