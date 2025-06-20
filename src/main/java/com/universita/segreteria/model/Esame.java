package com.universita.segreteria.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(onlyExplicitlyIncluded = true)
public class Esame {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    private Long id;

    @ToString.Include
    private String nome;

    @ToString.Include
    private LocalDate data;

    @ToString.Include
    private int cfu;

    @Enumerated(EnumType.STRING)
    @ToString.Include
    private StatoEsame statoEsame;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "docente_id")
    @JsonBackReference
    @ToString.Exclude
    private Docente docente;


    @OneToMany(mappedBy = "esame", cascade = CascadeType.ALL)
    @JsonManagedReference
    @ToString.Exclude
    private List<Voto> voti;

    @ManyToMany
    @JoinTable(
            name = "esame_prenotazioni",
            joinColumns = @JoinColumn(name = "esame_id"),
            inverseJoinColumns = @JoinColumn(name = "studente_id")
    )
    @ToString.Exclude
    private List<Studente> studentiPrenotati;

}

