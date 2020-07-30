package main;

import java.io.Serializable;

public class Comentario implements Serializable {

    static final long serialVersionUID = 1;

    private long id;
    private String descricao;

    public Comentario(long id, String descricao) {
        this.id = id;
        this.descricao = descricao;
    }

    public long getId() {
        return id;
    }

    public String getDescricao() {
        return descricao;
    }
}
