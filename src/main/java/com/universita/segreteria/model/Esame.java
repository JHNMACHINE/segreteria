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
public class Esame {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;
    private LocalDate data;
    private int cfu;

    @Enumerated(EnumType.STRING)
    private StatoEsame statoEsame;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "docente_id")
    @JsonBackReference
    private Docente docente;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "studente_id")
    private Studente studente;

    @OneToMany(mappedBy = "esame", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Voto> voti;

    @ManyToMany
    @JoinTable(name = "esame_prenotazioni", joinColumns = @JoinColumn(name = "esame_id"), inverseJoinColumns = @JoinColumn(name = "studente_id"))
    private List<Studente> studentiPrenotati;
}
