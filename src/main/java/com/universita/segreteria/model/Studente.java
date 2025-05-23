package com.universita.segreteria.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Studente
{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome, cognome, matricola, residenza;
    private LocalDate dataDiNascita;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "piano_id")
    private PianoDiStudi pianoDiStudi;

    @OneToMany(mappedBy = "studente", cascade = CascadeType.ALL)
    private List<Tassa> tassePagate;

    @OneToMany(mappedBy = "studente", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Voto> voti;

    @OneToMany(mappedBy = "studente", cascade = CascadeType.ALL)
    private List<Esame> esami;

}
