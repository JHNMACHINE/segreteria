package com.universita.segreteria.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Esame
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    String nome;
    LocalDate date;
    @ManyToOne
    List<Voto> voti;
    StatoEsame statoEsame;



}
