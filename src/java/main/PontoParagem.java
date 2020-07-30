package main;

import java.io.Serializable;
import java.util.ArrayList;

public class PontoParagem implements Serializable {

    static final long serialVersionUID = 1;

    private long id;
    private String nome;
    private String audio;
    private ArrayList<Imagem> listaImagens;
    private Local local;
    private String descricao;

    public PontoParagem(long id, String nome, String audio, ArrayList<Imagem> listaImagens, String descricao, Local local) {
        this.id = id;
        this.nome = nome;
        this.audio = audio;
        this.listaImagens = listaImagens;
        this.descricao = descricao;
        this.local = local;
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

    public Local getLocal() {
        return local;
    }
}
