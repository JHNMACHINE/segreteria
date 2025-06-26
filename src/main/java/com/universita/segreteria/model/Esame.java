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
@SuperBuilder(toBuilder = true)
@ToString(onlyExplicitlyIncluded = true)
@Table(
        name = "esame",
        uniqueConstraints = @UniqueConstraint(columnNames = {"data", "aula"})
)
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

    @OneToMany(mappedBy = "esame", cascade = CascadeType.ALL)
    private List<Prenotazione> prenotazioni;

    @Enumerated(EnumType.STRING)
    @ToString.Include
    private Aula aula;

}

