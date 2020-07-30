package main;

import java.io.Serializable;
import java.util.ArrayList;

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

    public GuiaTuristico(long id, int idUtilizador, Local local, ArrayList<Imagem> listaImagens,
            String nome, String descricao, String audio, long duracao, ArrayList<PontoParagem> listaPontosParagem) {
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

    public void setLocal(Local local) {
        this.local = local;
    }

    public Local getLocal() {
        return local;
    }

    public ArrayList<Imagem> getListaImagens() {
        return listaImagens;
    }

    public void setListaImagens(ArrayList<Imagem> listaImagens) {
        this.listaImagens = listaImagens;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public void setAudio(String audio) {
        this.audio = audio;
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
}
