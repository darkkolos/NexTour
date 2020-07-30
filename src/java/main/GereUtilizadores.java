/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import com.google.gson.Gson;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/gereutilizadores/d7d7c90837b6ac6f872b22394b575a34")
public class GereUtilizadores {

    public GereUtilizadores() {

    }

    @POST
    @Path("/utilizador/password")
    @Produces(MediaType.APPLICATION_JSON)
    public Response alterarPasswordUtilizador(@FormParam("email") String email, @FormParam("password") String password) {
        BaseDados bd = new BaseDados();

        try {
            VariaveisBD variaveisBD = bd.abrirConexao();
            PreparedStatement pstmt = variaveisBD.
                    getConnection().
                    prepareStatement(
                            "UPDATE utilizador SET U_PASSWORD = ? WHERE U_EMAIL = ?;"
                    );
            pstmt.setString(1, password);
            pstmt.setString(2, email);
            int query = pstmt.executeUpdate();
            if (query == 0) {
                bd.fecharConexao(variaveisBD);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
            bd.fecharConexao(variaveisBD);
        } catch (SQLException e) {
            System.out.println(e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        return Response.ok().build();
    }

    @POST
    @Path("/utilizador/nome")
    @Produces(MediaType.APPLICATION_JSON)
    public Response alterarNomeUtilizador(@FormParam("email") String email, @FormParam("nome") String nome) {
        BaseDados bd = new BaseDados();

        try {
            VariaveisBD variaveisBD = bd.abrirConexao();
            PreparedStatement pstmt = variaveisBD.
                    getConnection().
                    prepareStatement(
                            "UPDATE utilizador SET U_NOME = ? WHERE U_EMAIL = ?;"
                    );
            pstmt.setString(1, nome);
            pstmt.setString(2, email);
            int query = pstmt.executeUpdate();
            if (query == 0) {
                bd.fecharConexao(variaveisBD);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
            bd.fecharConexao(variaveisBD);
        } catch (SQLException e) {
            System.out.println(e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        return Response.ok().build();
    }

    @POST
    @Path("/utilizador")
    @Produces(MediaType.APPLICATION_JSON)
    public Response adicionarUtilizador(@FormParam("email") String email, @FormParam("nome") String nome, @FormParam("password") String password) {
        BaseDados bd = new BaseDados();

        try {
            VariaveisBD variaveisBD = bd.abrirConexao();
            PreparedStatement pstmt = variaveisBD.
                    getConnection().
                    prepareStatement(
                            "INSERT INTO utilizador (U_NOME, U_EMAIL, U_PASSWORD) values (?, ?, ?)"
                    );
            pstmt.setString(1, nome);
            pstmt.setString(2, email);
            pstmt.setString(3, password);
            int query = pstmt.executeUpdate();
            if (query == 0) {
                bd.fecharConexao(variaveisBD);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
            bd.fecharConexao(variaveisBD);
        } catch (SQLException e) {
            System.out.println(e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        return Response.ok().build();
    }

    @GET
    @Path("/utilizador")
    @Produces(MediaType.APPLICATION_JSON)
    public String retornarUtilizador(@QueryParam("email") String email) {
        BaseDados bd = new BaseDados();
        ArrayList<String> conteudo = new ArrayList<>();
        try {
            VariaveisBD variaveisBD = bd.abrirConexao();
            PreparedStatement pstmt = variaveisBD.
                    getConnection().
                    prepareStatement(
                            "SELECT * FROM utilizador WHERE U_EMAIL = ?"
                    );
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs == null) {
                return null;
            } else {
                while (rs.next()) {
                    conteudo.add(rs.getString("U_NOME"));
                    conteudo.add(rs.getString("U_EMAIL"));
                    conteudo.add(rs.getString("U_FOTOGRAFIA"));
                }
            }
            bd.fecharConexao(variaveisBD);
        } catch (SQLException e) {
            System.out.println(e);
            return "2";
        }
        return new Gson().toJson(conteudo);
    }

    @GET
    @Path("/autenticacao")
    @Produces(MediaType.APPLICATION_JSON)
    public String autenticarUtilizador(@QueryParam("email") String email, @QueryParam("password") String password) {

        BaseDados bd = new BaseDados();
        try {
            VariaveisBD variaveisBD = bd.abrirConexao();
            PreparedStatement pstmt = variaveisBD.
                    getConnection().
                    prepareStatement(
                            "SELECT U_ID "
                            + "FROM utilizador "
                            + "WHERE U_EMAIL = ? "
                            + "AND U_PASSWORD = ?"
                    );
            pstmt.setString(1, email);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            if (rs == null) {
                bd.fecharConexao(variaveisBD);
                return new Gson().toJson("2");
            } else {
                if (rs.next()) {
                    bd.fecharConexao(variaveisBD);
                    return new Gson().toJson("1");
                }
                bd.fecharConexao(variaveisBD);
                return new Gson().toJson("2");
            }
        } catch (SQLException e) {
            System.out.println(e);
            return new Gson().toJson("2");
        }
    }

    @GET
    @Path("/email/unique")
    @Produces(MediaType.APPLICATION_JSON)
    public String verificarEmail(@QueryParam("email") String email) {

        BaseDados bd = new BaseDados();
        try {
            VariaveisBD variaveisBD = bd.abrirConexao();
            PreparedStatement pstmt = variaveisBD.
                    getConnection().
                    prepareStatement(
                            "SELECT U_ID "
                            + "FROM utilizador "
                            + "WHERE U_EMAIL = ?"
                    );
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs == null) {
                bd.fecharConexao(variaveisBD);
                return new Gson().toJson("1");
            } else {
                while (rs.next()) {
                    bd.fecharConexao(variaveisBD);
                    return new Gson().toJson("1");
                }
                bd.fecharConexao(variaveisBD);
                return new Gson().toJson("2");
            }
        } catch (SQLException e) {
            System.out.println(e);
            return new Gson().toJson("3");
        }
    }

    @POST
    @Path("/utilizador/fotografia")
    @Produces(MediaType.APPLICATION_JSON)
    public Response alterarFotoPerfil(@FormParam("email") String email, @FormParam("pathFotografia") String pathFotografia) {
        BaseDados bd = new BaseDados();

        long idUtilizador = 0;
        try {
            VariaveisBD variaveisBD = bd.abrirConexao();
            PreparedStatement pstmt = variaveisBD.
                    getConnection().
                    prepareStatement(
                            "SELECT U_ID "
                            + "FROM utilizador "
                            + "WHERE U_EMAIL = ?"
                    );
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs == null) {
                bd.fecharConexao(variaveisBD);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            } else {
                while (rs.next()) {
                    idUtilizador = rs.getLong("U_ID");
                }
                bd.fecharConexao(variaveisBD);
            }
        } catch (SQLException e) {
            System.out.println(e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        String ftp_path = new Caminhos().getPathServidorWeb() + "\\htdocs\\Nextour\\pasta_fotografias_perfil\\" + idUtilizador + ".jpg";

        inserirFicheiroParaServidor(pathFotografia, ftp_path);

        String pathAlterarBd = "/Nextour/pasta_fotografias_perfil/" + idUtilizador + ".jpg";

        try {
            VariaveisBD variaveisBD = bd.abrirConexao();
            PreparedStatement pstmt = variaveisBD.
                    getConnection().
                    prepareStatement(
                            "UPDATE utilizador SET U_FOTOGRAFIA = ? WHERE U_EMAIL = ?"
                    );
            pstmt.setString(1, pathAlterarBd);
            pstmt.setString(2, email);
            int query = pstmt.executeUpdate();
            if (query == 0) {
                bd.fecharConexao(variaveisBD);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
            bd.fecharConexao(variaveisBD);
        } catch (SQLException e) {
            System.out.println(e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        return Response.ok().build();
    }

    /////////////////////////////////FUNÇÕES DE AUXILIO/////////////////////////////////
    public boolean inserirFicheiroParaServidor(String ficheiroCodificado, String pathFinal) {
        byte[] data = Base64.getDecoder().decode(ficheiroCodificado);
        try (OutputStream stream = new FileOutputStream(pathFinal)) {
            stream.write(data);
            return true;
        } catch (IOException ioe) {
            return false;
        }
    }
}
