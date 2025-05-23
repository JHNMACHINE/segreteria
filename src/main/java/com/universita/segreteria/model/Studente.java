package com.universita.segreteria.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Studente
{
    private String nome, cognome, matricola, residenza;
    private LocalDate dataDiNascita;
    private PianoDiStudi pianoDiStudi;
    private List<Tassa> tassePagate;
    private List<Voto> voti;
    private List<Esame> esami;

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

    public String getResidenza() {
        return residenza;
    }

    public void setResidenza(String residenza) {
        this.residenza = residenza;
    }

    public LocalDate getDataDiNascita() {
        return dataDiNascita;
    }

    public void setDataDiNascita(LocalDate dataDiNascita) {
        this.dataDiNascita = dataDiNascita;
    }

    public PianoDiStudi getPianoDiStudi() {
        return pianoDiStudi;
    }

    public void setPianoDiStudi(PianoDiStudi pianoDiStudi) {
        this.pianoDiStudi = pianoDiStudi;
    }

    public List<Voto> getVoti() {
        return voti;
    }

    public void setVoti(List<Voto> voti) {
        this.voti = voti;
    }

    public List<Tassa> getTassePagate() {
        return tassePagate;
    }

    public void setTassePagate(List<Tassa> tassePagate) {
        this.tassePagate = tassePagate;
    }

    public List<Esame> getEsami() {
        return esami;
    }

    public void setEsami(List<Esame> esami) {
        this.esami = esami;
    }
}
