package main;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/gereguias/d7d7c90837b6ac6f872b22394b575a34")
public class GereGuias {

    public GereGuias() {

    }

    @POST
    @Path("/guia")
    @Produces(MediaType.APPLICATION_JSON)
    public Response editarGuia(@FormParam("guiaTuristico") String aJsonGuiaTuristico, @FormParam("email") String aEmail) {

        eliminarGuia(String.valueOf(new Gson().fromJson(aJsonGuiaTuristico, GuiaTuristico.class).getId()));

        inserirGuia(aJsonGuiaTuristico, aEmail);

        return Response.ok().build();
    }

    @PUT
    @Path("/guia")
    @Produces(MediaType.APPLICATION_JSON)
    public Response inserirGuia(@FormParam("guiaTuristico") String aJsonGuiaTuristico, @FormParam("email") String aEmail) {

        System.out.println("");
        Gson gson = new Gson();
        GuiaTuristico aGuiaTuristico = gson.fromJson(aJsonGuiaTuristico, GuiaTuristico.class);

        if (verificacaoDados(aGuiaTuristico)) {
            BaseDados bd = new BaseDados();
            VariaveisBD variaveisBD = null;

            //inserção da morada principal do guia
            try {
                variaveisBD = bd.abrirConexao();
                PreparedStatement pstmt = variaveisBD.
                        getConnection().
                        prepareStatement(
                                "INSERT INTO local (LO_MORADA, LO_DISTRITO, LO_LONGITUDE, LO_LATITUDE) VALUES (?, ?, ?, ?);"
                        );
                pstmt.setString(1, aGuiaTuristico.getLocal().getMorada());
                pstmt.setString(2, aGuiaTuristico.getLocal().getDistrito());
                pstmt.setDouble(3, aGuiaTuristico.getLocal().getLongitude());
                pstmt.setDouble(4, aGuiaTuristico.getLocal().getLatitude());
                int query = pstmt.executeUpdate();
                if (query == 0) {
                    bd.fecharConexao(variaveisBD);

                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                }
                bd.fecharConexao(variaveisBD);
            } catch (SQLException e) {
                bd.fecharConexao(variaveisBD);

                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }

            //inserção do guiaturistico
            try {
                variaveisBD = bd.abrirConexao();
                PreparedStatement pstmt = variaveisBD.
                        getConnection().
                        prepareStatement(
                                "INSERT INTO guiaturistico (U_ID, LO_ID, GT_NOME, GT_DESCRICAO, GT_DURACAO) "
                                + "VALUES("
                                + "(SELECT U_ID FROM utilizador WHERE U_EMAIL LIKE ?)"
                                + ", (SELECT LO_ID FROM local ORDER BY LO_ID DESC LIMIT 1), ?, ?, ?)"
                        );
                pstmt.setString(1, aEmail);
                pstmt.setString(2, aGuiaTuristico.getNome());
                pstmt.setString(3, aGuiaTuristico.getDescricao());
                pstmt.setLong(4, aGuiaTuristico.getDuracao());
                int query = pstmt.executeUpdate();
                if (query == 0) {
                    bd.fecharConexao(variaveisBD);

                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                }
                bd.fecharConexao(variaveisBD);
            } catch (SQLException e) {
                bd.fecharConexao(variaveisBD);

                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }

            //tiragem do número ID que este guia turistico será
            long idGuia = 0;
            try {
                variaveisBD = bd.abrirConexao();
                PreparedStatement pstmt = variaveisBD.
                        getConnection().
                        prepareStatement(
                                "SELECT GT_ID FROM guiaturistico WHERE U_ID = (SELECT U_ID FROM utilizador WHERE U_EMAIL LIKE ?) ORDER BY GT_ID DESC LIMIT 1;"
                        );
                pstmt.setString(1, aEmail);
                ResultSet rs = pstmt.executeQuery();
                if (rs == null) {
                    bd.fecharConexao(variaveisBD);

                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                } else {
                    while (rs.next()) {
                        idGuia = rs.getLong("GT_ID");
                    }
                }
                bd.fecharConexao(variaveisBD);
            } catch (SQLException e) {
                bd.fecharConexao(variaveisBD);

                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }

            System.out.println("comecou a verificacao");
            if (aGuiaTuristico.getAudio() != null) {
                System.out.println("entrou no nao null");
                if (aGuiaTuristico.getAudio().length() > 0) {
                    System.out.println("entrou no >0");

                    //atribuição do path do audio
                    String pathAudio = new Caminhos().getPathServidorWeb() + "/Nextour/pasta_audios_guias/" + idGuia + ".mp3";
                    String audio = "/Nextour/pasta_audios_guias/" + idGuia + ".mp3";

                    //alteração do audio
                    try {
                        variaveisBD = bd.abrirConexao();
                        PreparedStatement pstmt = variaveisBD.
                                getConnection().
                                prepareStatement(
                                        "UPDATE guiaturistico SET GT_AUDIO = ? WHERE GT_ID = ?"
                                );
                        pstmt.setString(1, audio);
                        pstmt.setLong(2, idGuia);
                        int query = pstmt.executeUpdate();
                        if (query == 0) {
                            eliminarGuia(String.valueOf(idGuia));
                            bd.fecharConexao(variaveisBD);

                            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                        }
                        bd.fecharConexao(variaveisBD);
                    } catch (SQLException e) {
                        eliminarGuia(String.valueOf(idGuia));
                        bd.fecharConexao(variaveisBD);

                        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                    }

                    //inserção do audio do guia para o servidor
                    if (!inserirFicheiroParaServidor(aGuiaTuristico.getAudio(), pathAudio)) {
                        eliminarGuia(String.valueOf(idGuia));

                        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                    }
                }
            }

            int contadorImagensPrincipais = 0;

            //inserção das imagens para a base de dados e para o servidor
            System.out.println("comecou a verificacao imagens");
            if (aGuiaTuristico.getListaImagens() != null) {
                System.out.println("entrou nas imagens");
                for (Imagem imagem : aGuiaTuristico.getListaImagens()) {
                    String pathImagem = new Caminhos().getPathServidorWeb() + "/Nextour/pasta_fotografias_guias/"
                            + idGuia + "_" + contadorImagensPrincipais + ".jpg";
                    String urlImagem = "/Nextour/pasta_fotografias_guias/" + idGuia + "_" + contadorImagensPrincipais + ".jpg";

                    try {
                        variaveisBD = bd.abrirConexao();
                        PreparedStatement pstmt = variaveisBD.
                                getConnection().
                                prepareStatement(
                                        "INSERT INTO imagem (GT_ID, IM_URL) VALUES (?, ?)"
                                );
                        pstmt.setDouble(1, idGuia);
                        pstmt.setString(2, urlImagem);
                        int query = pstmt.executeUpdate();
                        if (query == 0) {
                            eliminarGuia(String.valueOf(idGuia));
                            bd.fecharConexao(variaveisBD);

                            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                        }
                        bd.fecharConexao(variaveisBD);
                    } catch (SQLException e) {
                        eliminarGuia(String.valueOf(idGuia));
                        bd.fecharConexao(variaveisBD);

                        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                    }
                    if (!inserirFicheiroParaServidor(imagem.getUrl(), pathImagem)) {
                        eliminarGuia(String.valueOf(idGuia));

                        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                    }
                    contadorImagensPrincipais++;
                }
            }

            int contadorPontosParagem = 0;

            if (aGuiaTuristico.getListaPontosParagem() != null) {

                for (PontoParagem pontoParagem : aGuiaTuristico.getListaPontosParagem()) {
                    System.out.println("entrou " + contadorPontosParagem);
                    try {
                        variaveisBD = bd.abrirConexao();
                        PreparedStatement pstmt = variaveisBD.
                                getConnection().
                                prepareStatement(
                                        "INSERT INTO local (LO_MORADA, LO_DISTRITO, LO_LONGITUDE, LO_LATITUDE) VALUES (?, ?, ?, ?);"
                                );
                        pstmt.setString(1, pontoParagem.getLocal().getMorada());
                        pstmt.setString(2, pontoParagem.getLocal().getDistrito());
                        pstmt.setDouble(3, pontoParagem.getLocal().getLongitude());
                        pstmt.setDouble(4, pontoParagem.getLocal().getLatitude());
                        int query = pstmt.executeUpdate();
                        if (query == 0) {
                            eliminarGuia(String.valueOf(idGuia));
                            bd.fecharConexao(variaveisBD);

                            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();

                        }
                        bd.fecharConexao(variaveisBD);

                        String pathAudio = new Caminhos().getPathServidorWeb() + "/Nextour/pasta_audios_guias/"
                                + idGuia + "_" + contadorPontosParagem + ".mp3";
                        String audio = null;
                        if (pontoParagem.getAudio() != null) {
                            if (pontoParagem.getAudio().length() > 0) {
                                audio = "/Nextour/pasta_audios_guias/" + idGuia + "_" + contadorPontosParagem + ".mp3";
                            }
                        }

                        variaveisBD = bd.abrirConexao();
                        pstmt = variaveisBD.
                                getConnection().
                                prepareStatement(
                                        "INSERT INTO pontoparagem (GT_ID, LO_ID, PP_NOME, PP_DESCRICAO, PP_AUDIO) "
                                        + "VALUES((SELECT GT_ID  FROM guiaturistico ORDER BY GT_ID DESC LIMIT 1),"
                                        + "(SELECT LO_ID FROM local ORDER BY LO_ID DESC LIMIT 1), ?, ?, ?)"
                                );
                        pstmt.setString(1, pontoParagem.getNome());
                        pstmt.setString(2, pontoParagem.getDescricao());
                        pstmt.setString(3, audio);
                        query = pstmt.executeUpdate();
                        if (query == 0) {
                            eliminarGuia(String.valueOf(idGuia));
                            bd.fecharConexao(variaveisBD);

                            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                        }
                        bd.fecharConexao(variaveisBD);
                        if (audio != null) {
                            if (!inserirFicheiroParaServidor(pontoParagem.getAudio(), pathAudio)) {
                                eliminarGuia(String.valueOf(idGuia));

                                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                            }
                        }
                    } catch (SQLException e) {
                        eliminarGuia(String.valueOf(idGuia));
                        bd.fecharConexao(variaveisBD);

                        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                    }

                    if (pontoParagem.getListaImagens() != null) {
                        int imagemID = 0;
                        for (Imagem imagem : pontoParagem.getListaImagens()) {
                            String pathImagem = new Caminhos().getPathServidorWeb() + "/Nextour/pasta_fotografias_guias/" + idGuia + "_" + contadorPontosParagem + "_" + imagemID + ".jpg";
                            String urlImagem = "/Nextour/pasta_fotografias_guias/" + idGuia + "_" + contadorPontosParagem + "_" + imagemID + ".jpg";
                            try {
                                variaveisBD = bd.abrirConexao();
                                PreparedStatement pstmt = variaveisBD.
                                        getConnection().
                                        prepareStatement(
                                                "INSERT INTO imagem (GT_ID, PP_ID, IM_URL) VALUES (?, (SELECT PP_ID FROM pontoparagem ORDER BY PP_ID DESC LIMIT 1), ?)"
                                        );
                                pstmt.setDouble(1, idGuia);
                                pstmt.setString(2, urlImagem);
                                int query = pstmt.executeUpdate();
                                if (query == 0) {
                                    eliminarGuia(String.valueOf(idGuia));
                                    bd.fecharConexao(variaveisBD);

                                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                                }
                                bd.fecharConexao(variaveisBD);
                                if (!inserirFicheiroParaServidor(imagem.getUrl(), pathImagem)) {
                                    eliminarGuia(String.valueOf(idGuia));

                                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                                }
                            } catch (SQLException e) {
                                eliminarGuia(String.valueOf(idGuia));
                                bd.fecharConexao(variaveisBD);

                                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                            }
                            imagemID++;
                        }
                    }
                    contadorPontosParagem++;
                }
            }
            return Response.ok().build();
        } else {

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("/guias")
    @Produces(MediaType.APPLICATION_JSON)
    public String obterMeusGuias(@QueryParam("email") String aEmail, @DefaultValue("0") @QueryParam("escolha") String aEscolha) {
        ArrayList<GuiaTuristico> listaGuiasTuristicos = new ArrayList<>();

        BaseDados bd = new BaseDados();

        boolean bo;

        try {
            VariaveisBD variaveisBD = bd.abrirConexao();
            String query;
            if (aEscolha.equals("1")) {
                query = "SELECT GT_ID, GT_NOME, GT_DURACAO, LO_ID "
                        + "FROM guiaturistico "
                        + "ORDER BY GT_ID DESC";
            } else {
                query = "SELECT GT_ID, GT_NOME, GT_DURACAO, LO_ID "
                        + "FROM guiaturistico "
                        + "WHERE U_ID = (SELECT U_ID FROM utilizador WHERE U_EMAIL LIKE ?) "
                        + "ORDER BY GT_ID DESC";
            }
            PreparedStatement pstmt = variaveisBD.
                    getConnection().
                    prepareStatement(query);
            if (!aEscolha.equals("1")) {
                pstmt.setString(1, aEmail);
            }
            ResultSet rs = pstmt.executeQuery();
            if (rs == null) {
                bo = false;
            } else {
                bo = false;
                while (rs.next()) {
                    bo = true;
                    GuiaTuristico guiaTuristico = new GuiaTuristico(rs.getLong("GT_ID"), 0, new Local(rs.getInt("LO_ID"), null, null, 0, 0),
                            null, rs.getString("GT_NOME"), null, null, rs.getLong("GT_DURACAO"), null);

                    try {
                        BaseDados bd2 = new BaseDados();
                        VariaveisBD variaveisBD2 = bd2.abrirConexao();
                        PreparedStatement pstmt2 = variaveisBD2.
                                getConnection().
                                prepareStatement(
                                        "SELECT LO_DISTRITO "
                                        + "FROM local "
                                        + "WHERE LO_ID = ?"
                                );
                        pstmt2.setInt(1, guiaTuristico.getLocal().getId());
                        ResultSet rs2 = pstmt2.executeQuery();
                        if (rs2 == null) {
                            bo = false;
                        } else {
                            while (rs2.next()) {
                                guiaTuristico.setLocal(new Local(0, null, rs2.getString("LO_DISTRITO"), 0, 0));
                            }
                        }
                        bd2.fecharConexao(variaveisBD2);
                    } catch (SQLException e) {
                        bo = false;
                    }

                    try {
                        BaseDados bd2 = new BaseDados();
                        VariaveisBD variaveisBD2 = bd2.abrirConexao();
                        PreparedStatement pstmt2 = variaveisBD2.
                                getConnection().
                                prepareStatement(
                                        "SELECT IM_URL "
                                        + "FROM imagem "
                                        + "WHERE GT_ID = ? "
                                        + "ORDER BY IM_ID ASC LIMIT 1"
                                );
                        pstmt2.setLong(1, guiaTuristico.getId());
                        ResultSet rs2 = pstmt2.executeQuery();
                        if (rs2 == null) {
                            bo = false;
                        } else {
                            while (rs2.next()) {
                                guiaTuristico.setAudio(rs2.getString("IM_URL"));
                            }
                        }
                        bd2.fecharConexao(variaveisBD2);
                    } catch (SQLException e) {
                        bo = false;
                    }

                    float valor = 0;
                    int contador = 0;
                    try {

                        BaseDados bd2 = new BaseDados();
                        VariaveisBD variaveisBD2 = bd2.abrirConexao();
                        PreparedStatement pstmt2 = variaveisBD2.
                                getConnection().
                                prepareStatement(
                                        "SELECT CA_VALOR "
                                        + "FROM classificacao "
                                        + "WHERE GT_ID = ?"
                                );
                        pstmt2.setLong(1, guiaTuristico.getId());
                        ResultSet rs2 = pstmt2.executeQuery();
                        if (rs2 == null) {
                            bo = false;
                        } else {
                            while (rs2.next()) {
                                contador++;
                                valor += rs2.getInt("CA_VALOR");
                            }
                            if (contador > 1) {
                                valor = valor / contador;
                            }
                            guiaTuristico.setDescricao(String.format("%.2f", valor));

                        }
                        bd2.fecharConexao(variaveisBD2);
                    } catch (SQLException e) {
                        bo = false;
                    }
                    listaGuiasTuristicos.add(guiaTuristico);
                }
            }
            bd.fecharConexao(variaveisBD);
        } catch (SQLException e) {
            System.out.println(e);
            bo = false;
        }
        if (bo) {
            return new Gson().toJson(listaGuiasTuristicos, new TypeToken<ArrayList<GuiaTuristico>>() {
            }.getType());
        } else {
            return new Gson().toJson(null, new TypeToken<ArrayList<GuiaTuristico>>() {
            }.getType());
        }
    }

    @GET
    @Path("/guias/pesquisa")
    @Produces(MediaType.APPLICATION_JSON)
    public String pesquisarGuias(@DefaultValue("") @QueryParam("titulo") String aTitulo, @DefaultValue("") @QueryParam("localidade") String aLocalidade) {
        ArrayList<GuiaTuristico> listaGuiasTuristicos = new ArrayList<>();

        BaseDados bd = new BaseDados();

        boolean bo = true;

        try {
            VariaveisBD variaveisBD = bd.abrirConexao();
            String query;
            if (aTitulo.equals("") || aLocalidade.equals("")) {
                if (aTitulo.equals("")) {
                    query = "SELECT GT_ID, GT_NOME, GT_DURACAO, LO_ID "
                            + "FROM guiaturistico "
                            + "WHERE GT_ID IN ("
                            + "SELECT GT_ID "
                            + "FROM guiaturistico "
                            + "WHERE LO_ID IN ( "
                            + "SELECT LO_ID "
                            + "FROM local "
                            + "WHERE LO_DISTRITO LIKE ? ESCAPE '!'))";
                } else {
                    query = "SELECT GT_ID, GT_NOME, GT_DURACAO, LO_ID "
                            + "FROM guiaturistico "
                            + "WHERE GT_ID IN ("
                            + "SELECT GT_ID "
                            + "FROM guiaturistico "
                            + "WHERE GT_NOME LIKE ? ESCAPE '!')";
                }
            } else {
                query = "SELECT GT_ID, GT_NOME, GT_DURACAO, LO_ID "
                        + "FROM guiaturistico "
                        + "WHERE GT_ID IN ("
                        + "SELECT GT_ID "
                        + "FROM guiaturistico "
                        + "WHERE LO_ID IN ("
                        + "SELECT LO_ID "
                        + "FROM local "
                        + "WHERE LO_DISTRITO LIKE ? ESCAPE '!') "
                        + "UNION "
                        + "SELECT GT_ID "
                        + "FROM guiaturistico "
                        + "WHERE GT_NOME LIKE ? ESCAPE '!'"
                        + ")";
            }
            PreparedStatement pstmt = variaveisBD.
                    getConnection().
                    prepareStatement(query);
            if (aTitulo.equals("") || aLocalidade.equals("")) {
                if (aTitulo.equals("")) {
                    pstmt.setString(1, "%" + aLocalidade + "%");
                } else {
                    pstmt.setString(1, "%" + aTitulo + "%");
                }
            } else {
                pstmt.setString(1, "%" + aLocalidade + "%");
                pstmt.setString(2, "%" + aTitulo + "%");
            }
            ResultSet rs = pstmt.executeQuery();
            if (rs == null) {
                bo = false;
            } else {
                while (rs.next()) {
                    GuiaTuristico guiaTuristico = new GuiaTuristico(rs.getLong("GT_ID"), 0, new Local(rs.getInt("LO_ID"), null, null, 0, 0),
                            null, rs.getString("GT_NOME"), null, null, rs.getLong("GT_DURACAO"), null);

                    try {
                        BaseDados bd2 = new BaseDados();
                        VariaveisBD variaveisBD2 = bd2.abrirConexao();
                        PreparedStatement pstmt2 = variaveisBD2.
                                getConnection().
                                prepareStatement(
                                        "SELECT LO_DISTRITO "
                                        + "FROM local "
                                        + "WHERE LO_ID = ?"
                                );
                        pstmt2.setInt(1, guiaTuristico.getLocal().getId());
                        ResultSet rs2 = pstmt2.executeQuery();
                        if (rs2 == null) {
                            bo = false;
                        } else {
                            while (rs2.next()) {
                                guiaTuristico.setLocal(new Local(0, null, rs2.getString("LO_DISTRITO"), 0, 0));
                            }
                        }
                        bd2.fecharConexao(variaveisBD2);
                    } catch (SQLException e) {
                        bo = false;
                    }

                    try {
                        BaseDados bd2 = new BaseDados();
                        VariaveisBD variaveisBD2 = bd2.abrirConexao();
                        PreparedStatement pstmt2 = variaveisBD2.
                                getConnection().
                                prepareStatement(
                                        "SELECT IM_URL "
                                        + "FROM imagem "
                                        + "WHERE GT_ID = ? "
                                        + "ORDER BY IM_ID ASC LIMIT 1"
                                );
                        pstmt2.setLong(1, guiaTuristico.getId());
                        ResultSet rs2 = pstmt2.executeQuery();
                        if (rs2 == null) {
                            bo = false;
                        } else {
                            while (rs2.next()) {
                                guiaTuristico.setAudio(rs2.getString("IM_URL"));
                            }
                        }
                        bd2.fecharConexao(variaveisBD2);
                    } catch (SQLException e) {
                        bo = false;
                    }

                    float valor = 0;
                    int contador = 0;
                    try {

                        BaseDados bd2 = new BaseDados();
                        VariaveisBD variaveisBD2 = bd2.abrirConexao();
                        PreparedStatement pstmt2 = variaveisBD2.
                                getConnection().
                                prepareStatement(
                                        "SELECT CA_VALOR "
                                        + "FROM classificacao "
                                        + "WHERE GT_ID = ?"
                                );
                        pstmt2.setLong(1, guiaTuristico.getId());
                        ResultSet rs2 = pstmt2.executeQuery();
                        if (rs2 == null) {
                            bo = false;
                        } else {
                            while (rs2.next()) {
                                contador++;
                                valor += rs2.getInt("CA_VALOR");
                            }
                            if (contador > 1) {
                                valor = valor / contador;
                            }
                            guiaTuristico.setDescricao(String.format("%.2f", valor));

                        }
                        bd2.fecharConexao(variaveisBD2);
                    } catch (SQLException e) {
                        bo = false;
                    }
                    listaGuiasTuristicos.add(guiaTuristico);
                }
            }
            bd.fecharConexao(variaveisBD);
        } catch (SQLException e) {
            System.out.println(e);
            bo = false;
        }
        if (bo) {
            return new Gson().toJson(listaGuiasTuristicos, new TypeToken<ArrayList<GuiaTuristico>>() {
            }.getType());
        } else {
            return new Gson().toJson(null, new TypeToken<ArrayList<GuiaTuristico>>() {
            }.getType());
        }
    }

    @GET
    @Path("/guias/favoritos")
    @Produces(MediaType.APPLICATION_JSON)
    public String obterMeusGuiasFavoritos(@DefaultValue("") @QueryParam("email") String aEmail) {
        if (aEmail.equals("")) {
            return new Gson().toJson(null, new TypeToken<ArrayList<GuiaTuristico>>() {
            }.getType());
        }
        ArrayList<GuiaTuristico> listaGuiasTuristicos = new ArrayList<>();

        BaseDados bd = new BaseDados();

        boolean bo = true;

        try {
            VariaveisBD variaveisBD = bd.abrirConexao();
            String query;
            query = "SELECT GT_ID, GT_NOME, GT_DURACAO, LO_ID "
                    + "FROM guiaturistico "
                    + "WHERE GT_ID IN (SELECT GT_ID FROM favorito WHERE U_ID = (SELECT U_ID FROM utilizador WHERE U_EMAIL LIKE ?) ORDER BY FA_ID DESC)"
                    + " ORDER BY GT_ID DESC";
            PreparedStatement pstmt = variaveisBD.
                    getConnection().
                    prepareStatement(query);
            pstmt.setString(1, aEmail);
            ResultSet rs = pstmt.executeQuery();
            if (rs == null) {
                bo = false;
            } else {
                while (rs.next()) {
                    GuiaTuristico guiaTuristico = new GuiaTuristico(rs.getLong("GT_ID"), 0, new Local(rs.getInt("LO_ID"), null, null, 0, 0),
                            null, rs.getString("GT_NOME"), null, null, rs.getLong("GT_DURACAO"), null);

                    try {
                        BaseDados bd2 = new BaseDados();
                        VariaveisBD variaveisBD2 = bd2.abrirConexao();
                        PreparedStatement pstmt2 = variaveisBD2.
                                getConnection().
                                prepareStatement(
                                        "SELECT LO_DISTRITO "
                                        + "FROM local "
                                        + "WHERE LO_ID = ?"
                                );
                        pstmt2.setInt(1, guiaTuristico.getLocal().getId());
                        ResultSet rs2 = pstmt2.executeQuery();
                        if (rs2 == null) {
                            bo = false;
                        } else {
                            while (rs2.next()) {
                                guiaTuristico.setLocal(new Local(0, null, rs2.getString("LO_DISTRITO"), 0, 0));
                            }
                        }
                        bd2.fecharConexao(variaveisBD2);
                    } catch (SQLException e) {
                        bo = false;
                    }

                    try {
                        BaseDados bd2 = new BaseDados();
                        VariaveisBD variaveisBD2 = bd2.abrirConexao();
                        PreparedStatement pstmt2 = variaveisBD2.
                                getConnection().
                                prepareStatement(
                                        "SELECT IM_URL "
                                        + "FROM imagem "
                                        + "WHERE GT_ID = ? "
                                        + "ORDER BY IM_ID ASC LIMIT 1"
                                );
                        pstmt2.setLong(1, guiaTuristico.getId());
                        ResultSet rs2 = pstmt2.executeQuery();
                        if (rs2 == null) {
                            bo = false;
                        } else {
                            while (rs2.next()) {
                                guiaTuristico.setAudio(rs2.getString("IM_URL"));
                            }
                        }
                        bd2.fecharConexao(variaveisBD2);
                    } catch (SQLException e) {
                        bo = false;
                    }
                    float valor = 0;
                    int contador = 0;
                    try {

                        BaseDados bd2 = new BaseDados();
                        VariaveisBD variaveisBD2 = bd2.abrirConexao();
                        PreparedStatement pstmt2 = variaveisBD2.
                                getConnection().
                                prepareStatement(
                                        "SELECT CA_VALOR "
                                        + "FROM classificacao "
                                        + "WHERE GT_ID = ?"
                                );
                        pstmt2.setLong(1, guiaTuristico.getId());
                        ResultSet rs2 = pstmt2.executeQuery();
                        if (rs2 == null) {
                            bo = false;
                        } else {
                            while (rs2.next()) {
                                contador++;
                                valor += rs2.getInt("CA_VALOR");
                            }
                            if (contador > 1) {
                                valor = valor / contador;
                            }
                            guiaTuristico.setDescricao(String.format("%.2f", valor));

                        }
                        bd2.fecharConexao(variaveisBD2);

                    } catch (SQLException e) {
                        bo = false;
                    }
                    listaGuiasTuristicos.add(guiaTuristico);
                }
            }
            bd.fecharConexao(variaveisBD);
        } catch (SQLException e) {
            System.out.println(e);
            bo = false;
        }
        return new Gson().toJson(listaGuiasTuristicos, new TypeToken<ArrayList<GuiaTuristico>>() {
        }.getType());
    }

    @GET
    @Path("/guia")
    @Produces(MediaType.APPLICATION_JSON)
    public String obterMeuGuia(@QueryParam("id") String idGuiaTuristicoString) {

        long idGuiaTuristico = Long.parseLong(idGuiaTuristicoString);

        BaseDados bd = new BaseDados();

        boolean bo = true;

        Local localGuia = null;

        try {
            VariaveisBD variaveisBD = bd.abrirConexao();
            PreparedStatement pstmt = variaveisBD.
                    getConnection().
                    prepareStatement(
                            "SELECT * "
                            + "FROM local "
                            + "WHERE LO_ID = (SELECT LO_ID FROM guiaturistico WHERE GT_ID = ?)"
                    );
            pstmt.setLong(1, idGuiaTuristico);
            ResultSet rs = pstmt.executeQuery();
            if (rs == null) {
                bo = false;
            } else {
                while (rs.next()) {
                    localGuia = new Local(rs.getInt("LO_ID"), rs.getString("LO_MORADA"), rs.getString("LO_DISTRITO"), rs.getDouble("LO_LATITUDE"), rs.getDouble("LO_LONGITUDE"));
                }
            }
            bd.fecharConexao(variaveisBD);
        } catch (SQLException e) {
            System.out.println(e);
            bo = false;
        }

        ArrayList<Imagem> listaImagensGuia = new ArrayList<>();
        try {
            VariaveisBD variaveisBD = bd.abrirConexao();
            PreparedStatement pstmt = variaveisBD.
                    getConnection().
                    prepareStatement(
                            "SELECT * "
                            + "FROM imagem "
                            + "WHERE GT_ID = ?"
                    );
            pstmt.setLong(1, idGuiaTuristico);
            ResultSet rs = pstmt.executeQuery();
            if (rs == null) {
                bo = false;
            } else {
                while (rs.next()) {
                    listaImagensGuia.add(new Imagem(rs.getInt("IM_ID"), rs.getString("IM_URL")));
                }
            }
            bd.fecharConexao(variaveisBD);
        } catch (SQLException e) {
            System.out.println(e);
            bo = false;
        }
        ArrayList<PontoParagem> listaPontosParagem = new ArrayList<>();
        try {
            VariaveisBD variaveisBD = bd.abrirConexao();
            PreparedStatement pstmt = variaveisBD.
                    getConnection().
                    prepareStatement(
                            "SELECT * "
                            + "FROM pontoparagem "
                            + "WHERE GT_ID = ?"
                    );
            pstmt.setLong(1, idGuiaTuristico);
            ResultSet rs = pstmt.executeQuery();
            if (rs == null) {
                bo = false;
            } else {
                while (rs.next()) {
                    BaseDados bd2 = new BaseDados();

                    Local localPontoParagem = null;

                    try {
                        VariaveisBD variaveisBD2 = bd2.abrirConexao();
                        PreparedStatement pstmt2 = variaveisBD2.
                                getConnection().
                                prepareStatement(
                                        "SELECT * "
                                        + "FROM local "
                                        + "WHERE LO_ID = (SELECT LO_ID FROM pontoparagem WHERE PP_ID = ?)"
                                );
                        pstmt2.setLong(1, rs.getLong("PP_ID"));
                        ResultSet rs2 = pstmt2.executeQuery();
                        if (rs2 == null) {
                            bo = false;
                        } else {
                            while (rs2.next()) {
                                localPontoParagem = new Local(rs2.getInt("LO_ID"), rs2.getString("LO_MORADA"), rs2.getString("LO_DISTRITO"), rs2.getDouble("LO_LATITUDE"), rs2.getDouble("LO_LONGITUDE"));
                            }
                        }
                        bd2.fecharConexao(variaveisBD2);
                    } catch (SQLException e) {
                        System.out.println(e);
                        bo = false;
                    }

                    ArrayList<Imagem> listaImagensPontoParagem = new ArrayList<>();
                    try {
                        VariaveisBD variaveisBD2 = bd2.abrirConexao();
                        PreparedStatement pstmt2 = variaveisBD2.
                                getConnection().
                                prepareStatement(
                                        "SELECT * "
                                        + "FROM imagem "
                                        + "WHERE PP_ID = ?"
                                );
                        pstmt2.setLong(1, rs.getLong("PP_ID"));
                        ResultSet rs2 = pstmt2.executeQuery();
                        if (rs2 == null) {
                            bo = false;
                        } else {
                            while (rs2.next()) {
                                listaImagensPontoParagem.add(new Imagem(rs2.getInt("IM_ID"), rs2.getString("IM_URL")));
                            }
                        }
                        bd2.fecharConexao(variaveisBD2);
                    } catch (SQLException e) {
                        System.out.println(e);
                        bo = false;
                    }
                    listaPontosParagem.add(new PontoParagem(rs.getLong("PP_ID"), rs.getString("PP_NOME"), rs.getString("PP_AUDIO"),
                            listaImagensPontoParagem, rs.getString("PP_DESCRICAO"), localPontoParagem));
                }
            }
            bd.fecharConexao(variaveisBD);
        } catch (SQLException e) {
            System.out.println(e);
            bo = false;
        }

        try {
            VariaveisBD variaveisBD = bd.abrirConexao();
            PreparedStatement pstmt = variaveisBD.
                    getConnection().
                    prepareStatement(
                            "SELECT * "
                            + "FROM guiaturistico "
                            + "WHERE GT_ID = ?"
                    );
            pstmt.setLong(1, idGuiaTuristico);
            ResultSet rs = pstmt.executeQuery();
            if (rs == null) {
                bo = false;
            } else {
                while (rs.next()) {
                    return new Gson().toJson(new GuiaTuristico(idGuiaTuristico, rs.getInt("U_ID"), localGuia,
                            listaImagensGuia, rs.getString("GT_NOME"), rs.getString("GT_DESCRICAO"),
                            rs.getString("GT_AUDIO"), rs.getLong("GT_DURACAO"), listaPontosParagem));
                }
            }
            bd.fecharConexao(variaveisBD);
        } catch (SQLException e) {
            System.out.println(e);
            bo = false;
        }

        return null;
    }

    @DELETE
    @Path("/guia")
    @Produces(MediaType.APPLICATION_JSON)
    public Response eliminarGuia(@QueryParam("id") String idGuiaTuristicoString) {

        String pathInicial = new Caminhos().getPathServidorWeb();

        long idGuiaTuristico = Long.parseLong(idGuiaTuristicoString);

        BaseDados bd = new BaseDados();

        try {
            VariaveisBD variaveisBD = bd.abrirConexao();
            PreparedStatement pstmt = variaveisBD.
                    getConnection().
                    prepareStatement(
                            "SELECT PP_AUDIO "
                            + "FROM pontoparagem "
                            + "WHERE GT_ID = ?"
                    );
            pstmt.setLong(1, idGuiaTuristico);
            ResultSet rs = pstmt.executeQuery();
            if (rs == null) {
                bd.fecharConexao(variaveisBD);
            } else {
                while (rs.next()) {
                    apagarFicheiro(pathInicial + rs.getString("PP_AUDIO"));
                }
            }
            bd.fecharConexao(variaveisBD);
        } catch (SQLException e) {
            System.out.println(e);
        }

        try {
            VariaveisBD variaveisBD = bd.abrirConexao();
            PreparedStatement pstmt = variaveisBD.
                    getConnection().
                    prepareStatement(
                            "SELECT GT_AUDIO "
                            + "FROM guiaturistico "
                            + "WHERE GT_ID = ?"
                    );
            pstmt.setLong(1, idGuiaTuristico);
            ResultSet rs = pstmt.executeQuery();
            if (rs == null) {
                bd.fecharConexao(variaveisBD);
            } else {
                while (rs.next()) {
                    apagarFicheiro(pathInicial + rs.getString("GT_AUDIO"));
                }
            }
            bd.fecharConexao(variaveisBD);
        } catch (SQLException e) {
            System.out.println(e);
        }

        try {
            VariaveisBD variaveisBD = bd.abrirConexao();
            PreparedStatement pstmt = variaveisBD.
                    getConnection().
                    prepareStatement(
                            "SELECT IM_URL "
                            + "FROM imagem "
                            + "WHERE GT_ID = ?"
                    );
            pstmt.setLong(1, idGuiaTuristico);
            ResultSet rs = pstmt.executeQuery();
            if (rs == null) {
                bd.fecharConexao(variaveisBD);
            } else {
                while (rs.next()) {
                    apagarFicheiro(pathInicial + rs.getString("IM_URL"));
                }
            }
            bd.fecharConexao(variaveisBD);
        } catch (SQLException e) {
            System.out.println(e);
        }

        try {
            VariaveisBD variaveisBD = bd.abrirConexao();
            PreparedStatement pstmt = variaveisBD.
                    getConnection().
                    prepareStatement(
                            "DELETE FROM local "
                            + "WHERE LO_ID IN (SELECT LO_ID "
                            + "FROM pontoparagem "
                            + "WHERE GT_ID = ?)"
                    );
            pstmt.setLong(1, idGuiaTuristico);
            int i = pstmt.executeUpdate();
            if (i == 0) {
                bd.fecharConexao(variaveisBD);
            } else {

            }
            bd.fecharConexao(variaveisBD);
        } catch (SQLException e) {
            System.out.println(e);
        }

        try {
            VariaveisBD variaveisBD = bd.abrirConexao();
            PreparedStatement pstmt = variaveisBD.
                    getConnection().
                    prepareStatement(
                            "DELETE FROM local "
                            + "WHERE LO_ID = (SELECT LO_ID "
                            + "FROM guiaturistico "
                            + "WHERE GT_ID = ?)"
                    );
            pstmt.setLong(1, idGuiaTuristico);
            int i = pstmt.executeUpdate();
            if (i == 0) {
                bd.fecharConexao(variaveisBD);
            } else {

            }
            bd.fecharConexao(variaveisBD);
        } catch (SQLException e) {
            System.out.println(e);
        }

        return Response.status(Response.Status.OK).build();
    }

    @GET
    @Path("/guia/favorito")
    @Produces(MediaType.APPLICATION_JSON)
    public String favoritoObter(@QueryParam("aEmail") String aEmail, @QueryParam("idGuiaTuristico") String idGuiaTuristico) {

        BaseDados bd = new BaseDados();
        boolean existe = false;

        try {
            VariaveisBD variaveisBD = bd.abrirConexao();
            PreparedStatement pstmt = variaveisBD.
                    getConnection().
                    prepareStatement(
                            "SELECT FA_ID "
                            + "FROM favorito "
                            + "WHERE GT_ID = ? "
                            + "AND U_ID = (SELECT U_ID FROM utilizador WHERE U_EMAIL LIKE ?)"
                    );
            pstmt.setLong(1, Long.parseLong(idGuiaTuristico));
            pstmt.setString(2, aEmail);
            ResultSet rs = pstmt.executeQuery();
            if (rs == null) {
                bd.fecharConexao(variaveisBD);
                return new Gson().toJson("2");
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
            return new Gson().toJson("2");
        }
    }

    @GET
    @Path("/guia/classificacao")
    @Produces(MediaType.APPLICATION_JSON)
    public String minhaClassificacaoObter(@QueryParam("aEmail") String aEmail, @QueryParam("idGuiaTuristico") String idGuiaTuristico) {

        BaseDados bd = new BaseDados();
        int valor = 0;

        try {
            VariaveisBD variaveisBD = bd.abrirConexao();
            PreparedStatement pstmt = variaveisBD.
                    getConnection().
                    prepareStatement(
                            "SELECT CA_VALOR "
                            + "FROM classificacao "
                            + "WHERE GT_ID = ? "
                            + "AND U_ID = (SELECT U_ID FROM utilizador WHERE U_EMAIL LIKE ?)"
                    );
            pstmt.setLong(1, Long.parseLong(idGuiaTuristico));
            pstmt.setString(2, aEmail);
            ResultSet rs = pstmt.executeQuery();
            if (rs == null) {
                bd.fecharConexao(variaveisBD);
                return new Gson().toJson("0");
            } else {
                while (rs.next()) {
                    valor = rs.getInt("CA_VALOR");
                }
                bd.fecharConexao(variaveisBD);
                return new Gson().toJson(String.valueOf(valor));
            }
        } catch (SQLException e) {
            System.out.println(e);
            return new Gson().toJson("0");
        }
    }

    @GET
    @Path("/guia/classificacao/total")
    @Produces(MediaType.APPLICATION_JSON)
    public String classificacaoObter(@QueryParam("idGuiaTuristico") String idGuiaTuristico) {

        BaseDados bd = new BaseDados();
        int contador = 0;
        float valor = 0;

        try {
            VariaveisBD variaveisBD = bd.abrirConexao();
            PreparedStatement pstmt = variaveisBD.
                    getConnection().
                    prepareStatement(
                            "SELECT CA_VALOR "
                            + "FROM classificacao "
                            + "WHERE GT_ID = ? "
                    );
            pstmt.setLong(1, Long.parseLong(idGuiaTuristico));
            ResultSet rs = pstmt.executeQuery();
            if (rs == null) {
                bd.fecharConexao(variaveisBD);
                return new Gson().toJson("0");
            } else {
                while (rs.next()) {
                    valor += rs.getInt("CA_VALOR");
                    contador++;
                }
                bd.fecharConexao(variaveisBD);
                if (contador > 0) {
                    valor = valor / contador;
                    return new Gson().toJson(String.format("%.2f", valor));

                } else {
                    return new Gson().toJson("0");
                }
            }
        } catch (SQLException e) {
            System.out.println(e);
            return new Gson().toJson("0");
        }
    }

    @POST
    @Path("/guia/favorito")
    @Produces(MediaType.APPLICATION_JSON)
    public Response favoritoAdicionar(@FormParam("aEmail") String aEmail, @FormParam("idGuiaTuristico") String idGuiaTuristico) {

        BaseDados bd = new BaseDados();
        long existe = -1;

        try {
            VariaveisBD variaveisBD = bd.abrirConexao();
            PreparedStatement pstmt = variaveisBD.
                    getConnection().
                    prepareStatement(
                            "SELECT FA_ID "
                            + "FROM favorito "
                            + "WHERE GT_ID = ? "
                            + "AND U_ID = (SELECT U_ID FROM utilizador WHERE U_EMAIL LIKE ?)"
                    );
            pstmt.setLong(1, Long.parseLong(idGuiaTuristico));
            pstmt.setString(2, aEmail);
            ResultSet rs = pstmt.executeQuery();
            if (rs == null) {
                bd.fecharConexao(variaveisBD);
            } else {
                while (rs.next()) {
                    existe = rs.getLong("FA_ID");
                }
            }
            bd.fecharConexao(variaveisBD);
        } catch (SQLException e) {
            System.out.println(e);
        }

        if (existe != -1) {//remover
            try {
                VariaveisBD variaveisBD = bd.abrirConexao();
                PreparedStatement pstmt = variaveisBD.
                        getConnection().
                        prepareStatement(
                                "DELETE FROM favorito "
                                + "WHERE FA_ID = ?"
                        );
                pstmt.setLong(1, existe);
                int i = pstmt.executeUpdate();
                if (i == 0) {
                    bd.fecharConexao(variaveisBD);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                }
                bd.fecharConexao(variaveisBD);
                return Response.accepted().build();
            } catch (SQLException e) {
                System.out.println(e);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        } else {//adicionar
            try {
                VariaveisBD variaveisBD = bd.abrirConexao();
                PreparedStatement pstmt = variaveisBD.
                        getConnection().
                        prepareStatement(
                                "INSERT INTO favorito (GT_ID, U_ID) "
                                + "VALUES (?,(SELECT U_ID FROM utilizador WHERE U_EMAIL LIKE ?))"
                        );
                pstmt.setLong(1, Long.parseLong(idGuiaTuristico));
                pstmt.setString(2, aEmail);
                int i = pstmt.executeUpdate();
                if (i == 0) {
                    bd.fecharConexao(variaveisBD);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                }
                bd.fecharConexao(variaveisBD);
                return Response.ok().build();
            } catch (SQLException e) {
                System.out.println(e);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        }
    }

    @POST
    @Path("/guia/classificacao")
    @Produces(MediaType.APPLICATION_JSON)
    public Response classificacaoAdicionar(@FormParam("aEmail") String aEmail, @FormParam("idGuiaTuristico") String idGuiaTuristico, @FormParam("valor") String valor) {

        BaseDados bd = new BaseDados();
        long existe = -1;
        int aValorObtido = 0;

        int valorTransferido = Integer.parseInt(valor);

        try {
            VariaveisBD variaveisBD = bd.abrirConexao();
            PreparedStatement pstmt = variaveisBD.
                    getConnection().
                    prepareStatement(
                            "SELECT CA_ID, CA_VALOR "
                            + "FROM classificacao "
                            + "WHERE GT_ID = ? "
                            + "AND U_ID = (SELECT U_ID FROM utilizador WHERE U_EMAIL LIKE ?)"
                    );
            pstmt.setLong(1, Long.parseLong(idGuiaTuristico));
            pstmt.setString(2, aEmail);
            ResultSet rs = pstmt.executeQuery();
            if (rs == null) {
                bd.fecharConexao(variaveisBD);
            } else {
                while (rs.next()) {
                    existe = rs.getLong("CA_ID");
                    aValorObtido = rs.getInt("CA_VALOR");
                }
            }
            bd.fecharConexao(variaveisBD);
        } catch (SQLException e) {
            System.out.println(e);
        }

        if (existe != -1) {//existe
            if (valorTransferido == aValorObtido) {//remover                
                try {
                    VariaveisBD variaveisBD = bd.abrirConexao();
                    PreparedStatement pstmt = variaveisBD.
                            getConnection().
                            prepareStatement(
                                    "DELETE FROM classificacao "
                                    + "WHERE CA_ID = ?"
                            );
                    pstmt.setLong(1, existe);
                    int i = pstmt.executeUpdate();
                    if (i == 0) {
                        bd.fecharConexao(variaveisBD);
                        System.out.println("oi");
                        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                    }
                    bd.fecharConexao(variaveisBD);
                    return Response.accepted().build();
                } catch (SQLException e) {
                    System.out.println(e);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                }
            } else {//alterar valor
                try {
                    VariaveisBD variaveisBD = bd.abrirConexao();
                    PreparedStatement pstmt = variaveisBD.
                            getConnection().
                            prepareStatement(
                                    "UPDATE classificacao "
                                    + "SET CA_VALOR = ? "
                                    + "WHERE CA_ID = ?"
                            );
                    pstmt.setInt(1, valorTransferido);
                    pstmt.setLong(2, existe);
                    int i = pstmt.executeUpdate();
                    if (i == 0) {
                        bd.fecharConexao(variaveisBD);

                        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                    }
                    bd.fecharConexao(variaveisBD);
                    return Response.ok().build();
                } catch (SQLException e) {
                    System.out.println(e);

                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                }
            }
        } else {//adicionar
            try {
                VariaveisBD variaveisBD = bd.abrirConexao();
                PreparedStatement pstmt = variaveisBD.
                        getConnection().
                        prepareStatement(
                                "INSERT INTO classificacao (GT_ID, U_ID, CA_VALOR) "
                                + "VALUES (?,(SELECT U_ID FROM utilizador WHERE U_EMAIL LIKE ?), ?)"
                        );
                pstmt.setLong(1, Long.parseLong(idGuiaTuristico));
                pstmt.setString(2, aEmail);
                pstmt.setInt(3, valorTransferido);
                int i = pstmt.executeUpdate();
                if (i == 0) {
                    bd.fecharConexao(variaveisBD);

                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                }
                bd.fecharConexao(variaveisBD);
                return Response.ok().build();
            } catch (SQLException e) {
                System.out.println(e);

                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        }
    }

    @GET
    @Path("/guias/mapa")
    @Produces(MediaType.APPLICATION_JSON)
    public String buscarGuiasMapa(@QueryParam("minLat") double aMinLat, @QueryParam("maxLat") double aMaxLat,
            @QueryParam("minLong") double aMinLong, @QueryParam("maxLong") double aMaxLong) {
        ArrayList<GuiaTuristico> listaGuiasTuristicos = new ArrayList<>();

        BaseDados bd = new BaseDados();

        boolean bo = true;
        boolean normal = true;
        try {
            VariaveisBD variaveisBD = bd.abrirConexao();
            String query;
            if (aMaxLong > aMinLong) {//normal
                query = "SELECT * "
                        + "FROM guiaturistico "
                        + "WHERE LO_ID IN ("
                        + "SELECT LO_ID "
                        + "FROM local "
                        + "WHERE LO_LATITUDE > ? "
                        + "AND LO_LATITUDE < ? "
                        + "AND LO_LONGITUDE > ? "
                        + "AND LO_LONGITUDE < ?) "
                        + "ORDER BY GT_ID DESC LIMIT 30";
            } else {//reverso
                normal = false;
                query = "SELECT * "
                        + "FROM guiaturistico "
                        + "WHERE LO_ID IN ("
                        + "SELECT LO_ID "
                        + "FROM local "
                        + "WHERE LO_LATITUDE < ? "
                        + "AND LO_LATITUDE > ? "
                        + "AND LO_LONGITUDE < ? "
                        + "AND LO_LONGITUDE > ?) "
                        + "ORDER BY GT_ID DESC LIMIT 30";
            }
            PreparedStatement pstmt = variaveisBD.
                    getConnection().
                    prepareStatement(query);

            if (normal) {
                pstmt.setDouble(1, aMinLat);
                pstmt.setDouble(2, aMaxLat);
                pstmt.setDouble(3, aMinLong);
                pstmt.setDouble(4, aMaxLong);
            } else {
                pstmt.setDouble(1, aMinLat);
                pstmt.setDouble(2, aMaxLat);
                pstmt.setDouble(3, aMinLong);
                pstmt.setDouble(4, aMaxLong);
            }
            ResultSet rs = pstmt.executeQuery();
            if (rs == null) {
                bo = false;
            } else {
                while (rs.next()) {
                    GuiaTuristico guiaTuristico = new GuiaTuristico(rs.getLong("GT_ID"), 0, new Local(rs.getInt("LO_ID"), null, null, 0, 0),
                            null, rs.getString("GT_NOME"), rs.getString("GT_DESCRICAO"), rs.getString("GT_AUDIO"), rs.getLong("GT_DURACAO"), null);

                    try {
                        BaseDados bd2 = new BaseDados();
                        VariaveisBD variaveisBD2 = bd2.abrirConexao();
                        PreparedStatement pstmt2 = variaveisBD2.
                                getConnection().
                                prepareStatement(
                                        "SELECT * "
                                        + "FROM local "
                                        + "WHERE LO_ID = ?"
                                );
                        pstmt2.setInt(1, guiaTuristico.getLocal().getId());
                        ResultSet rs2 = pstmt2.executeQuery();
                        if (rs2 == null) {
                            bo = false;
                        } else {
                            while (rs2.next()) {
                                guiaTuristico.setLocal(new Local(0,
                                        rs2.getString("LO_MORADA"),
                                        rs2.getString("LO_DISTRITO"),
                                        rs2.getDouble("LO_LATITUDE"),
                                        rs2.getDouble("LO_LONGITUDE")));
                            }
                        }
                        bd2.fecharConexao(variaveisBD2);
                    } catch (SQLException e) {
                        bo = false;
                    }

                    try {
                        BaseDados bd2 = new BaseDados();
                        VariaveisBD variaveisBD2 = bd2.abrirConexao();
                        PreparedStatement pstmt2 = variaveisBD2.
                                getConnection().
                                prepareStatement(
                                        "SELECT IM_URL "
                                        + "FROM imagem "
                                        + "WHERE GT_ID = ? "
                                        + "AND PP_ID IS NULL "
                                        + "ORDER BY IM_ID ASC"
                                );
                        pstmt2.setLong(1, guiaTuristico.getId());
                        ResultSet rs2 = pstmt2.executeQuery();
                        if (rs2 == null) {
                            bo = false;
                        } else {
                            ArrayList<Imagem> listaImagens = new ArrayList<>();
                            while (rs2.next()) {
                                listaImagens.add(new Imagem(0, rs2.getString("IM_URL")));
                            }
                            guiaTuristico.setListaImagens(listaImagens);
                        }
                        bd2.fecharConexao(variaveisBD2);
                    } catch (SQLException e) {
                        bo = false;
                    }
                    listaGuiasTuristicos.add(guiaTuristico);
                }
            }
            bd.fecharConexao(variaveisBD);
        } catch (SQLException e) {
            System.out.println(e);
            bo = false;
        }
        return new Gson().toJson(listaGuiasTuristicos, new TypeToken<ArrayList<GuiaTuristico>>() {
        }.getType());
    }

    /////////////////////////////////FUNÇÕES DE AUXILIO/////////////////////////////////
    public boolean inserirFicheiroParaServidor(String ficheiroCodificado, String pathFinal) {
        try {
            byte[] data = Base64.getDecoder().decode(ficheiroCodificado);
            
            FtpClient ftpClient = new FtpClient();
            ftpClient.open();


                System.out.println(data.length);
            
            InputStream myInputStream = new ByteArrayInputStream(data);
            

            System.out.println(pathFinal);

            if (ftpClient.getFtp().storeFile(pathFinal, myInputStream)) {
                System.out.println("certo");
            } else {
                System.out.println("errado");
            }

            ftpClient.close();
        } catch (IOException ex) {
            System.out.println(ex);
            return false;
        }
        return true;
    }

    private void apagarFicheiro(String aPath) {
        File file = new File(aPath);
        if (file.exists()) {
            file.delete();
        }
    }

    private boolean verificacaoDados(GuiaTuristico guiaTuristico) {
        if (guiaTuristico != null) {
            if (guiaTuristico.getNome() != null) {
                if (guiaTuristico.getNome().length() <= 0) {
                    return false;
                }
            } else {
                return false;
            }
            if (guiaTuristico.getDescricao() != null) {
                if (guiaTuristico.getDescricao().length() <= 0) {
                    return false;
                }
            } else {
                return false;
            }
            if (guiaTuristico.getDuracao() < 0 || guiaTuristico.getDuracao() >= 6000) {
                return false;
            }
            if (guiaTuristico.getLocal() != null) {
                if (guiaTuristico.getLocal().getDistrito() != null) {
                    if (guiaTuristico.getLocal().getDistrito().length() <= 0) {
                        return false;
                    }
                } else {
                    return false;
                }
                if (guiaTuristico.getLocal().getMorada() != null) {
                    if (guiaTuristico.getLocal().getMorada().length() <= 0) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
            if (guiaTuristico.getListaPontosParagem() != null) {
                for (PontoParagem pontoParagem : guiaTuristico.getListaPontosParagem()) {

                    if (pontoParagem.getNome() != null) {
                        if (pontoParagem.getNome().length() <= 0) {
                            return false;
                        }
                    } else {
                        return false;
                    }
                    if (pontoParagem.getDescricao() != null) {
                        if (pontoParagem.getDescricao().length() <= 0) {
                            return false;
                        }
                    } else {
                        return false;
                    }
                    if (pontoParagem.getLocal() != null) {
                        if (pontoParagem.getLocal().getDistrito() != null) {
                            if (pontoParagem.getLocal().getDistrito().length() <= 0) {
                                return false;
                            }
                        } else {
                            return false;
                        }
                        if (pontoParagem.getLocal().getMorada() != null) {
                            if (pontoParagem.getLocal().getMorada().length() <= 0) {
                                return false;
                            }
                        } else {
                            return false;
                        }
                    }
                }
            }
        } else {
            return false;
        }
        return true;
    }

}
