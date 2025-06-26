package com.universita.segreteria.model;

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
public class Prenotazione {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Studente studente;

    @ManyToOne
    private Esame esame;

    @Enumerated(EnumType.STRING)
    private StatoPrenotazione stato; // E.g., PRENOTATO, ANNULLATO
}
