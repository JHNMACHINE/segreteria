package com.universita.segreteria.model;

import java.time.LocalDate;
import java.util.List;

public class Esame
{
    String nome;
    LocalDate date;
    List<Voto> voti;
    StatoEsame statoEsame;


    public StatoEsame getStatoEsame() {
        return statoEsame;
    }

    public void setStatoEsame(StatoEsame statoEsame) {
        this.statoEsame = statoEsame;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public List<Voto> getVoti() {
        return voti;
    }

    public void setVoti(List<Voto> voti) {
        this.voti = voti;
    }
}
