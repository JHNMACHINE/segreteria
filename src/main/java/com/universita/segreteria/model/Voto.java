package com.universita.segreteria.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Voto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "studente_id")
    private Studente studente;

    @ManyToOne
    @JsonBackReference
    private Esame esame;

    private int voto;

    @Enumerated(EnumType.STRING)
    private StatoVoto stato = StatoVoto.IN_ATTESA;
}
