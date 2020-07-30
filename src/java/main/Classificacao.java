package main;

import java.io.Serializable;

public class Classificacao implements Serializable {

    static final long serialVersionUID = 1;

    private long id;
    private long idGuiaTuristico;
    private int idUtilizador;
    private float valor;

    public Classificacao(long id, long idGuiaTuristico, int idUtilizador, float valor) {
        this.id = id;
        this.idGuiaTuristico = idGuiaTuristico;
        this.idUtilizador = idUtilizador;
        this.valor = valor;
    }

    public long getId() {
        return id;
    }

    public long getIdGuiaTuristico() {
        return idGuiaTuristico;
    }

    public int getIdUtilizador() {
        return idUtilizador;
    }

    public float getValor() {
        return valor;
    }
}
