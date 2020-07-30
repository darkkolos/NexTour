package main;

import java.io.Serializable;

/**
 *
 * @author sergy_000
 */
public class Utilizador implements Serializable {

  static final long serialVersionUID = 1;
    
  private long utilizadorId;
  private String utilizadorNome;
  private String utilizadorEmail;
  private String utilizadorPassword;
  private String utilizadorFoto;
  private int utilizadorFotoEstado;
  private int utilizadorEstado;

  public Utilizador(long utilizadorId, String utilizadorNome, String utilizadorEmail, 
          String utilizadorPassword, String utilizadorFoto, int utilizadorFotoEstado, int utilizadorEstado){
    this.utilizadorId = utilizadorId;
    this.utilizadorNome = utilizadorNome;
    this.utilizadorEmail = utilizadorEmail;
    this.utilizadorPassword = utilizadorPassword;
    this.utilizadorFoto = utilizadorFoto;
    this.utilizadorFotoEstado = utilizadorFotoEstado;
    this.utilizadorEstado = utilizadorEstado;
  }
  
  public Utilizador(){};

  //-----------------------------------------------------------------------GETS----------------------------------------------------------------------------------------//
  //Permite obter dados de utilizadores carregados
  public long getId(){
    return this.utilizadorId;
  }
  public String getNome(){
    return this.utilizadorNome;
  }
  public String getEmail(){
    return this.utilizadorEmail;
  }
  public String getPassword(){
    return this.utilizadorPassword;
  }
  public String getFoto(){
    return this.utilizadorFoto;
  }
  public int getFotoEstado(){
    return this.utilizadorFotoEstado;
  }
  public int getEstado(){
    return this.utilizadorEstado;
  }
}