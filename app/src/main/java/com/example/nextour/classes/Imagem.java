package com.example.nextour.classes;

import java.io.Serializable;

/**
 * Classe que vai ter o url de uma imagem e o seu id na base de dados.
 */
public class Imagem implements Serializable {

    static final long serialVersionUID = 1;

    private long id;
    private String url;

    public Imagem(long aId, String url) {
        this.id = aId;
        this.url = url;
    }

    public long getId() {
        return this.id;
    }

    public String getUrl() {
        return this.url;
    }
}
