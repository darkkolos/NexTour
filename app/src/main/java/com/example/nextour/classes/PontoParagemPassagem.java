package com.example.nextour.classes;

import android.location.Address;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Classe que vai ter os dados de um ponto de paragem, nomeadamente, o seu id,
 * um endereço que contenha os dados de um ponto num mapa, uma lista de imagens, um nome, uma descrição e o url do áudio.
 * Isto servirá apenas para quando um guia estiver a ser criado ou editado.
 */
public class PontoParagemPassagem implements Serializable {

    static final long serialVersionUID = 1;

    private long id;
    private String nome;
    private String audio;
    private ArrayList<Imagem> listaImagens;
    private Address address;
    private String descricao;

    public PontoParagemPassagem(long id, String nome, String audio, ArrayList<Imagem> listaImagens, String descricao, Address address) {
        this.id = id;
        this.nome = nome;
        this.audio = audio;
        this.listaImagens = listaImagens;
        this.descricao = descricao;
        this.address = address;
    }

    public long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getAudio() {
        return audio;
    }

    public void setAudio(String aAudio) {
        this.audio = aAudio;
    }

    public ArrayList<Imagem> getListaImagens() {
        return listaImagens;
    }

    public String getDescricao() {
        return descricao;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }
}
