package com.universita.segreteria.model;

import java.util.List;

public class Segretario
{
    private String nome, cognome, matricola;
    private List<Esame> appelli;
    private List<Voto> voti;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCognome() {
        return cognome;
    }

    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    public String getMatricola() {
        return matricola;
    }

    public void setMatricola(String matricola) {
        this.matricola = matricola;
    }

    public List<Esame> getAppelli() {
        return appelli;
    }

    public void setAppelli(List<Esame> appelli) {
        this.appelli = appelli;
    }

    public List<Voto> getVoti() {
        return voti;
    }

    public void setVoti(List<Voto> voti) {
        this.voti = voti;
    }
}
