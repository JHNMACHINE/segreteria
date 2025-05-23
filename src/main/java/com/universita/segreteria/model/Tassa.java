package com.universita.segreteria.model;

public class Tassa
{
    private String nome;
    private int prezzo;
    private boolean pagata;

    public int getPrezzo() {
        return prezzo;
    }

    public String getNome() {
        return nome;
    }

    public boolean isPagata() {
        return pagata;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setPrezzo(int prezzo) {
        this.prezzo = prezzo;
    }

    public void setPagata(boolean pagata) {
        this.pagata = pagata;
    }
}
