package com.example.nextour.classes;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Classe que vai ter os dados de um guia, nomeadamente, o seu id, o id do utilizador que a criou,
 * um local, uma lista de imagens, um nome, uma descrição, o url do áudio, a duração e uma lista dos pontos de paragem.
 */
public class GuiaTuristico implements Serializable {

    static final long serialVersionUID = 1;

    private long id;
    private int idUtilizador;
    private Local local;
    private ArrayList<Imagem> listaImagens;
    private String nome;
    private String descricao;
    private String audio;
    private long duracao;
    private ArrayList<PontoParagem> listaPontosParagem;

    public GuiaTuristico(long id, int idUtilizador, Local local, ArrayList<Imagem> listaImagens, String nome,
                         String descricao, String audio, long duracao, ArrayList<PontoParagem> listaPontosParagem) {
        this.id = id;
        this.idUtilizador = idUtilizador;
        this.local = local;
        this.listaImagens = listaImagens;
        this.nome = nome;
        this.descricao = descricao;
        this.audio = audio;
        this.duracao = duracao;
        this.listaPontosParagem = listaPontosParagem;
    }

    public long getId() {
        return id;
    }

    public int getIdUtilizador() {
        return idUtilizador;
    }

    public Local getLocal() {
        return local;
    }

    public ArrayList<Imagem> getListaImagens() {
        return listaImagens;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getAudio() {
        return audio;
    }

    public long getDuracao() {
        return duracao;
    }

    public ArrayList<PontoParagem> getListaPontosParagem() {
        return listaPontosParagem;
    }

    public void setListaPontosParagem(ArrayList<PontoParagem> listaPontosParagem) {
        this.listaPontosParagem = listaPontosParagem;
    }

    public void setListaImagens(ArrayList<Imagem> listaImagens) {
        this.listaImagens = listaImagens;
    }

    public void setAudio(String audio) {
        this.audio = audio;
    }
}
