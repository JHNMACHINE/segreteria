package com.universita.segreteria.model;

import java.util.List;

public class Docente
{
    private String nome, cognome, matricola;
    private List<Esame> apelli;

    public String getNome() {
        return nome;
    }

    public String getCognome() {
        return cognome;
    }

    public String getMatricola() {
        return matricola;
    }

    public List<Esame> getApelli() {
        return apelli;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    public void setMatricola(String matricola) {
        this.matricola = matricola;
    }

    public void setApelli(List<Esame> apelli) {
        this.apelli = apelli;
    }
}
